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
// Place an import statements you need here.
import java.util.GregorianCalendar;
import java.util.Locale;
import java.lang.Math;
import java.util.TreeSet;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Calculates incremental precipitation from cumulative precipitation. This one is derived from the AlgBaseNew to avoid
 * DST Issues.
 * 
 * @author L2EDDMAn
 */
//AW:JAVADOC_END
public class IncPrecip
	//extends decodes.tsdb.algo.AW_AlgorithmBase
        extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double input;    //AW:TYPECODE=i
	String _inputNames[] = { "input" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	public double start_val;
        public double end_val;
        public Date end_date;
//AW:LOCALVARS_END

//AW:OUTPUTS
	// created a NameVariable with the name you want, and add the string of that name to the array
        public NamedVariable output = new NamedVariable("output", 0.0 );
	String _outputNames[] = { "output" };
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
		_awAlgoType = AWAlgoType.AGGREGATING;
                // create an output variable and give it's name here
                // this variable will determine the output interval
		_aggPeriodVarRoleName = "output";

//AW:INIT_END

//AW:USERINIT
                
//AW:USERINIT_END
	}
	
	/**
	 * This method is called once before iterating all time slices.
	 */
	protected void beforeTimeSlices()
		throws DbCompException
	{
//AW:BEFORE_TIMESLICES
		// This code will be executed once before each group of time slices.
		// For TimeSlice algorithms this is done once before all slices.
		// For Aggregating algorithms, this is done before each aggregate
		// period.
                start_val = -1.0;
                end_val = -1.0;
                /*
                TreeSet inputBaseTimes = determineInputBaseTimes();
                for( java.lang.Object e : inputBaseTimes )
                {
                    Date d = (Date)e;
                    debug3("Basetimes date =" + d.toString() );
                }
                 * 
                 */

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
		// Enter code to be executed at each time-slice.
                if( date_equal(_timeSliceBaseTime,_aggregatePeriodBegin) )
                {
                    start_val = input;
                }
                else if( date_equal( _timeSliceBaseTime, _aggregatePeriodEnd ) )
                {
                    end_val = input;
                }
                
                /*
		GregorianCalendar cal = new GregorianCalendar( );
                cal.setTime( _timeSliceBaseTime);
                debug3( "Time is: " + cal.toString() );
                debug3( "   hour: " + cal.get( cal.HOUR ) );
                debug3( " minute: " + cal.get( cal.MINUTE) );
                debug3( "Input     = " + input);
                debug3( "start_val = " + start_val);
                debug3( "end_val   = " + end_val );
                boolean isdst = cal.getTimeZone().inDaylightTime(_timeSliceBaseTime);
                int hour = 8;
                if( isdst )
                {
                    hour = 7;
                }
                if( start_val < 0 && cal.get(cal.HOUR )== hour && cal.get(cal.MINUTE) == 0)
                {
                   start_val = input;
                }
                else
                {
                    end_val = input;
                    end_date = _timeSliceBaseTime;
                }
                */
//AW:TIMESLICE_END
	}

	/**
	 * This method is called once after iterating all time slices.
	 */
	protected void afterTimeSlices()
		throws DbCompException
	{
//AW:AFTER_TIMESLICES
		// This code will be executed once after each group of time slices.
		// For TimeSlice algorithms this is done once after all slices.
		// For Aggregating algorithms, this is done after each aggregate
		// period.
                /*
                GregorianCalendar cal = new GregorianCalendar();
                cal.setTime(end_date);
                debug3( "  Hour: " + cal.get( cal.HOUR ) );
                debug3( "Minute: " + cal.get( cal.MINUTE ) );
                
                boolean isdst = cal.getTimeZone().inDaylightTime(_timeSliceBaseTime);
                int hour = 8;                
                if( isdst )
                {
                    hour = 7;
                }
                if( !isMissing( start_val ) && !isMissing(end_val) && start_val >= 0.0 && end_val >= 0.0 && cal.get( cal.HOUR ) == hour && cal.get( cal.MINUTE)  == 0 )
                {
                    setOutput(output, end_val-start_val);
                }
                else
                {
                    info( "there is no data for this slice" );
                }*/
                double delta = end_val-start_val;
                debug3( " delta: " + (delta) );
                if( delta < 0.0 )
                {
                    delta = 0.0;
                }
                if( !isMissing( start_val ) && !isMissing(end_val) && start_val >= 0.0 && end_val >= 0.0)
                {

                    setOutput( output, delta);
                    debug3("data saved");
                }
                else
                {
                    info("no data this period");
                }
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
