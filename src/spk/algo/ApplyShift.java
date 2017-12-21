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
import decodes.tsdb.TimeSeriesIdentifier;
import java.util.TreeMap;
import spk.algo.support.StationData;
import java.util.Map;
import java.util.Map.Entry;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Reads shift information from and file and applies the number to the appropriate time slices.
 * @author L2EDDMAN
 */
//AW:JAVADOC_END
public class ApplyShift
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double input; //AW:TYPECODE=i
	String _inputNames[] = { "input" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	public String location = "";
        TreeMap<Date, StationData> map;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable output = new NamedVariable("output", 0);
	String _outputNames[] = { "output" };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public String ShiftsDir = "/shared/stations/"; // the directory containing the files with shift data.
        public double MinimumValue = -1*Double.MAX_VALUE; // value used to compare
	String _propertyNames[] = { "ShiftsDir", "MinimumValue" };
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

                /* Scan in Shifts
                 *   shift will be in file that is same as Location name, like the rating table files
                 *  the format of the file will be as follows
                 *  date time, shift, date entered, who entered
                 *
                 *  date/timein the data file will ALWAYS be in GMT, as such this comp should always be set to GMT time
                 *
                 *  shift is the actual value
                 *
                 *  date entered and who entered will not be used within this algorthim they will be used
                 *  at the web interface for record keeping.
                 *
                 *
                 *
                 *
                 */
                 TimeSeriesIdentifier tsid = getParmTsId( "input" );
                 location = tsid.getSiteName()+"."+tsid.getDataType().getCode();
                 debug3("getting station information for: " + location);
                 debug3("  in directory " + ShiftsDir );
                 map = StationData.loadData( ShiftsDir + "/" + location+".station");


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
		double shift = this.getShift(_timeSliceBaseTime);
                double pzf = this.getPZF(_timeSliceBaseTime);
                debug3( "The shift for " + _timeSliceBaseTime + " is " + shift);
                double value = Math.max( shift+input, this.MinimumValue );
                if( !isMissing( pzf ) ){
                    value = Math.max( value, pzf );
                }
                
                debug3( String.format( "%.02f + %.02f = %.02f (pzf = %f)", input,shift,value, pzf ) );
                setFlagBits(output, getInputFlagBits("input"));
                setOutput( output, value );
                
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
        /**
         * Figure out what shift we should be using
         * @param sliceTime
         *  time for the current value
         * @return
         *  the shift value appropriate for this time
         */
        public double getShift( Date sliceTime )
        {
            /**
             *  return the nearest previous time shift value
             *  otherwise return 0.0; assuming there is no shift
             *
             *
             */
             // searching backwards find the first date early that date_time
             // floor, find the greatest date less than or equal to the current slice
            if( map == null ) return 0.0; // if there is no config, we assume no shift
            Map.Entry<Date,StationData> d = map.floorEntry(sliceTime);
            if( d != null )
            {
                StationData v = d.getValue();
                debug3( "pulled " + v.shift + " entered by " + v.who );
                return v.shift;
            }

            return 0.0;
        }
        
        public double getPZF( Date sliceTime ){
            if( map == null ) return 0.0; // if there is no config, we assume no shift
            Map.Entry<Date,StationData> d = map.floorEntry(sliceTime);
            if( d != null )
            {
                StationData v = d.getValue();
                debug3( "pulled " + v.point_zero_flow + " entered by " + v.who );
                return v.point_zero_flow;
            }

            return Double.NEGATIVE_INFINITY;
        }

}
