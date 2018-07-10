package spk.algo;

import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.ParmRef;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;

//AW:IMPORTS
// Place an import statements you need here.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.lang.Math;
import java.util.Calendar;
import java.util.GregorianCalendar;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Takes the runoff from the CDEC B120 report ( or some other estimate of runoff ) and an Inflow time series and calculates the remaining runoff.
 * The algorithm will query data back to the last snowmelt forecast point for any give day to calculate the remaining runoff.
 * 
 * Expects ac-ft or cfs.
 * 
 * if cfs sum gets converted to Ac-ft, otherwise it gets used as is
 * 
 */
//AW:JAVADOC_END
public class RemainingRunoff
	extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double inflow;	//AW:TYPECODE=i
	String _inputNames[] = { "inflow" };
//AW:INPUTS_END


//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	TreeMap< Date, Double > runoff_data;
        SimpleDateFormat df = null;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable runoff_remaining = new NamedVariable("runoff_remaining", 0.0f );
	String _outputNames[] = { "runoff_remaining" };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public String runoff_dir = "/shared/config/runoff";
	String _propertyNames[] = { "runoff_dir" };
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
            
            TimeSeriesIdentifier tsid = getParmTsId( "inflow" );
            debug3("getting project nmuemonic for: " + tsid.getSiteName());
            String _parts[] = tsid.getSiteName().split("\\s+");
            String proj = _parts[1].split("-")[0];
            String fname = this.runoff_dir + "/"+proj.toUpperCase() + ".runoff";
            debug3( "getting data file: " + fname);
            File f = new File( fname );
            if( !f.exists() )
            {
                throw new DbCompException( "This project doesn't have any runoff data at this time");
            }
            try{
                runoff_data = new TreeMap<Date, Double>();
                BufferedReader reader = new BufferedReader( new FileReader(f)  );
                debug3("file opened");
                String line = null;
                String []parts = null;
                Date date;
                df = new SimpleDateFormat("MM/dd/yyyy");
                while( (line = reader.readLine()) != null )
                {
                    if( line.trim().equalsIgnoreCase("")) continue;
                    parts = line.split(",");
                    debug3("reading line: " + line + " len of parts array: " + parts.length);
                    debug3("parts are: ");
                    for( String p: parts)
                    {
                        debug3( "  " + p);
                    }

                    date = df.parse(parts[1]);
                    runoff_data.put(date, Double.parseDouble( parts[2]));
                }
                reader.close();
            }
            catch( java.lang.Exception e )
            {
                debug3( "could not load  runoff data. Reason: " + e.getMessage() );
                throw new DbCompException("Could not load the runoff data");
            }



            
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
                try{
                    df.setTimeZone(aggTZ);
                    debug3( "Evalutating runoff for " + df.format(_timeSliceBaseTime));
                    double remain = Double.MIN_VALUE;
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTimeZone(aggTZ);
                    cal.setTime(_timeSliceBaseTime);
                    int day = cal.get( Calendar.DAY_OF_MONTH);
                    int month = cal.get( Calendar.MONTH);
                    // Jan 31st at 0800 GMT shows up for Feb1 0000
                    if( month < Calendar.FEBRUARY || month > Calendar.JULY || (month == Calendar.FEBRUARY && day == 1 ) )
                    {
                        debug3("we are not in the runoff season, so not calculating runoff");
                        remain = 0.0;
                    }
                    else
                    {
                        if( !isMissing( inflow ) )
                        {
                            // get the nearest runoff date

                            Date d = this.getRunoffDate( _timeSliceBaseTime );
                            debug3( "Evalutating runoff for " + df.format(_timeSliceBaseTime));
                            debug3( "Nearest forecast point is " + df.format(d) );

                            debug3( "getting the forecasted runoff value for that date");
                            double runoff = this.getRunoff( d );
                            debug3( "aquired runoff value = " + runoff);
                            double data[] = this.getPrevValues("inflow", d, _timeSliceBaseTime);
                            debug3( "Number of flows we will sum: " + data.length);
                            double sum = 0.0;
                            for( double val: data )
                            {
                                sum += Math.max(0, val); // make sure we don't add water to the mountain.
                            }
                            data = null;
                            ParmRef ref = this.getParmRef("inflow");
                            if( ref.timeSeries.getUnitsAbbr().equalsIgnoreCase( "cfs" ) ){
                                sum=sum*1.9835;
                            }// otherwise use sum as is
                            //sum=sum*1.9835;
                            debug3( "sum of flow is " + sum);
                            remain = Math.max( 0, runoff-sum ); // snow melt doesn't keep happening after it's already melted
                            debug3("runoff remaining =" + remain);

                        }
                    }
                    setOutput(runoff_remaining, remain);
                }
                catch( Exception e )
                {
                    debug3("Could no calculate time slice: " + e.getMessage() );
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
		// This code will be executed once after each group of time slices.
		// For TimeSlice algorithms this is done once after all slices.
		// For Aggregating algorithms, this is done after each aggregate
		// period.
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

        public double getRunoff( Date d )
        {
            // go through the list and get the nearest runoff to this date
            return runoff_data.get(d);
        }

        public Date getRunoffDate( Date cur_date)
        {
            return runoff_data.floorKey(cur_date);
        }
}
