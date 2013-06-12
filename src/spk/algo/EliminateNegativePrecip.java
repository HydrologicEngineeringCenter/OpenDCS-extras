package spk.algo;

import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;

//AW:IMPORTS
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.lang.Math;
import decodes.cwms.CwmsFlags;
import decodes.tsdb.ParmRef;
import decodes.tsdb.*;
import ilex.var.NoConversionException;
import java.util.GregorianCalendar;
import java.util.Calendar;
import ilex.var.TimedVariable;
import java.util.logging.Level;
import java.util.logging.Logger;

//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Remove the diurnal flucuations in the Rain data caused by temperature variations
 * in the mineral oil used in our Catch Tubes ( some of the catch tubes have been painted white
 * which has eliminated the large +/- .2" swings in precip; there will still be some minor +/-.02 swings
 * due to temperature variation of all involved components )
 * 
 * Currently, this doesn't completely do what we want. The missing chucks aren't
 * actually run through as missing values. If it becomes a problem we may be
 * able to use the version of setOutput with the date. We just have to keep track
 * of each time we find a gap greater than the current interval.
 *
 * @author L2EDDMAN
 */
//AW:JAVADOC_END
public class EliminateNegativePrecip
	extends spk.algo.support.AlgBaseNew
{
    /*
     * TODO: refactor "BadRain" to input or something?
     */
//AW:INPUTS
	public double BadRain;	//AW:TYPECODE=i
	String _inputNames[] = { "BadRain" };
//AW:INPUTS_END

//AW:LOCALVARS
	        private SimpleDateFormat df; // used to dispay dates in the debug output
                private GregorianCalendar _timeSlice;
                private GregorianCalendar calStartOfWaterYear; // we don't care about what year we are in, just that we are at the start time (day/month/hour) of the new water year
		private Date startOfWaterYear; // to see if we should reset our good rain variable
		private double prev_rain; // the previously discovered "good" rain value
                private boolean first_run = true; // if we need to pick the first good value
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable FixedRain = new NamedVariable("FixedRain", 0);
	String _outputNames[] = { "FixedRain" };
//AW:OUTPUTS_END

//AW:PROPERTIES
	String _propertyNames[] = {  };
//AW:PROPERTIES_END

	// Allow javac to generate a no-args constructor.

	/**
	 * Algorithm-specific initialization provided by the subclass.
	 */
	protected void initAWAlgorithm( )
		throws DbCompException
	{
//AW:INIT
		_awAlgoType = AWAlgoType.TIME_SLICE;
                this.noAggregateFill = false;

//AW:INIT_END

//AW:USERINIT
		df = new SimpleDateFormat("ddMMMyyyy HHmm"); // basic standard HEC Format
		// create the start of the water year, current year on 01Oct at 0800 GMT
		// NOTE: this may actually need to be changed to 0000 instead of 0800, during compedit testing this did work
		// correctly though. Or we could set the timezone in this calendar.

                GregorianCalendar pst = new GregorianCalendar( TimeZone.getTimeZone("PST"));
                GregorianCalendar user = new GregorianCalendar( );
                
                pst.set(pst.get( Calendar.YEAR), Calendar.OCTOBER, 1, 0, 0);
                user.setTimeInMillis(pst.getTimeInMillis());
		//startOfWaterYear = (new GregorianCalendar( (new GregorianCalendar()).get(Calendar.YEAR) ,Calendar.OCTOBER, 1, 0, 0 )).getTime();
                startOfWaterYear = user.getTime();
                _timeSlice = new GregorianCalendar();
                TimeZone tz = user.getTimeZone();

                
                String tzname = tz.getDisplayName(tz.inDaylightTime(user.getTime()), TimeZone.SHORT); // the CCP uses the timezone we are currently in to query all data
                debug3( "We are operating ins the timezone: " + tzname );
                int year = user.get( Calendar.YEAR );
                if( new GregorianCalendar().get(Calendar.MONTH) < Calendar.OCTOBER )
                {
                    year = year - 1;
                }
                if( tzname.equals("UTC") || tzname.equals( "GMT") )
                {
                    calStartOfWaterYear = new GregorianCalendar(year, Calendar.OCTOBER, 1, 8, 0 );
                }
                else if( tzname.equals("PST"))
                {
                    calStartOfWaterYear = new GregorianCalendar(year, Calendar.OCTOBER, 1, 0, 0 );
                }

                else if( tzname.equals( "PDT") )
                {
                    calStartOfWaterYear = new GregorianCalendar(year, Calendar.OCTOBER, 1, 1, 0 );
                }
                startOfWaterYear = calStartOfWaterYear.getTime();
		debug1( "Start of water year is: " + df.format( startOfWaterYear ) ); // Sanity Check
//AW:USERINIT_END
	}
	
	/**
	 * This method is called once before iterating all time slices.
	 */
	protected void beforeTimeSlices()
		throws DbCompException
	{
//AW:BEFORE_TIMESLICES
             debug1( "Start of water year is: " + df.format( startOfWaterYear ) + " GMT" ); // Sanity Check
             first_run = true;
//AW:BEFORE_TIMESLICES_END
	}

	/**
	 * Do the algorithm for a single time slice.
	 * AW will fill in user-supplied code here.
	 * Base class will set inputs prior to calling this method.
	 * User code should call one of the setOutput methods for a time-slice
	 * output variable.
	 *
	 * @throws DbCompException (or subclass thereof) if execution of this
	 *        algorithm is to be aborted.
	 */
	protected void doAWTimeSlice()
		throws DbCompException
	{
//AW:TIMESLICE
                if( first_run )
                {
                    debug3("*********************First Run**********************");
                    prev_rain = getPrevValue("FixedRain", _timeSliceBaseTime);
                    first_run = false; // need to find a way to make this whole thing work better
                }

		debug1( "TimeSlice time is: " + df.format(_timeSliceBaseTime) );
                debug2( " prev_rain = " + prev_rain + ", BadRain = " + BadRain );
                clearFlagBits(FixedRain, Integer.MAX_VALUE ); // clear flags
                GregorianCalendar __timeSlice = new GregorianCalendar();
                __timeSlice.setTime( _timeSliceBaseTime );                
                debug3( "begining logic");        
                if( date_equal(_timeSliceBaseTime, calStartOfWaterYear.getTime() ))
		{
			debug1( "Start of water year, reseting" );
                        if( BadRain < 0.0 && !isMissing( BadRain ) )
                        {
                            prev_rain = 0.0;
                        }
                        else
                        {
                            prev_rain=BadRain;
                        }
		}
                else if( isMissing( prev_rain ) && !isMissing( BadRain ))
                {
                        //TODO: this will really cause problems if data starts not being there
                        debug2("This is the first good rain value, starting from here");
                        prev_rain = BadRain;
                }
                else if( isMissing( BadRain ) )
                {
                    debug2( "Input rain is missing, using previous value");
                }
		else if( Math.abs( BadRain-prev_rain ) > 2.0 )
		{
			debug2( "difference between current and previous value too large: |" + BadRain + " - " + prev_rain + "| = " + Math.abs( BadRain-prev_rain ) );
		}
		else if (BadRain > prev_rain )
		{
			debug2( "Current rain value is larger than previous, using current value." );
                        

			prev_rain = BadRain;
		}
		else
		{
			debug2( "not start of wateryear and new value < old value, keep previous value" );
		}
                if( prev_rain < 0.0 )
                {
                    prev_rain = 0.0;
                }
		debug3("Deleted inputs: " + this._sliceInputsDeleted);
                if( !this._sliceInputsDeleted )
                {
                    setFlagBits(FixedRain, CwmsFlags.SCREENED | CwmsFlags.REPLACEMENT_AUTOMATIC );
                    setOutput( FixedRain, prev_rain );
                    /*
                    try {
                        tsdb.commit(); // this is probably really bad to do
                    } catch (DbIoException ex) {
                        Logger.getLogger(EliminateNegativePrecip.class.getName()).log(Level.SEVERE, null, ex);
                    }
                     * didn't help at all
                     */
                }
                else
                {
                    deleteOutput(FixedRain);
                }

//AW:TIMESLICE_END
	}

	/**
	 * This method is called once after iterating all time slices.
	 */
	protected void afterTimeSlices()
		throws DbCompException
	{
//AW:AFTER_TIMESLICES
            
//AW:AFTER_TIMESLICES_END
	}

	/**
	 * Required method returns a list of all input time series names.
	 */
	public String[] getInputNames()
	{
		return _inputNames;
	}

	/**
	 * Required method returns a list of all output time series names.
	 */
	public String[] getOutputNames()
	{
		return _outputNames;
	}

	/**
	 * Required method returns a list of properties that have meaning to
	 * this algorithm.
	 */
	public String[] getPropertyNames()
	{
		return _propertyNames;
	}
}
