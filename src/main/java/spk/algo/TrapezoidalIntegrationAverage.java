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
import spk.algo.support.AlgBaseNew;
import ilex.util.Logger;
import ilex.util.FileLogger;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * TrapezoidalIntegrationAverage implements the trapezoidal integration method
 * of average current used in DSS for instantaneous data. It makes sense but
 * the only reference I've found is a statement in a USGS manual that it will
 * be used.
 * 
 * See any calculus text section on trapezoidal integration for more detail.
 * 
 * @author L2EDDMAN
 *
 */
//AW:JAVADOC_END
public class TrapezoidalIntegrationAverage
	//extends decodes.tsdb.algo.AW_AlgorithmBase
        extends AlgBaseNew
{
//AW:INPUTS
	public double input;	//AW:TYPECODE=i
	String _inputNames[] = { "input" };
//AW:INPUTS_END

//AW:LOCALVARS
	int count;
	Date previous_time;
	Date first_time;
	double previous_value;
	double tally;
        long start_t;
        long end_t;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable average = new NamedVariable("average", 0);
	String _outputNames[] = { "average" };
//AW:OUTPUTS_END

//AW:PROPERTIES
	public long minSamplesNeeded = 1;
	String _propertyNames[] = { "minSamplesNeeded" };
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
		_aggPeriodVarRoleName = "average";
//AW:INIT_END

//AW:USERINIT
		// No one-time init required.
                try{
                    FileLogger fl = (FileLogger)Logger.instance();
                    fl.setMaxLength( Integer.MAX_VALUE);
                    debug3("Extended Log");
                }
                catch( Exception e)
                {

                }
//AW:USERINIT_END
	}
	
	/**
	 * This method is called once before iterating all time slices.
	 */
	protected void beforeTimeSlices()
		throws DbCompException
	{
//AW:BEFORE_TIMESLICES
		// Zero out the tally & count for this agg period.
		count = 0;
		tally = 0.0;
		// Normally for average, output units will be the same as input.
		String inUnits = getInputUnitsAbbr("input");
		if (inUnits != null && inUnits.length() > 0)
			setOutputUnitsAbbr("average", inUnits);
                start_t = java.lang.System.nanoTime();
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
		double average_height;
		double time_diff;
		double volume;
		debug2("TrapezoidalIntegrationAverage:doAWTimeSlice, input=" + input);
		if (!isMissing(input))
		{
			if( count == 0 )
			{
				debug3( "First valid value of time slice, setting initial parameters" );
				//this is the first point in the time slice
				previous_time = _timeSliceBaseTime;
				first_time = _timeSliceBaseTime;
				previous_value = input;
			}
			else
			{
				debug3( "Next valid value of the time window, finding new volume" );
				// perform the section of trapezoidal integration
				time_diff = (_timeSliceBaseTime.getTime() - previous_time.getTime())/1000.0;
				average_height = 0.5*(input+previous_value);
				volume = average_height*time_diff;
				debug3( "Additional Volume: " + volume );
				tally += average_height*time_diff; // increase volume by this sets ammount
				debug3( "New Total Value: " + tally );
				previous_time = _timeSliceBaseTime;
				previous_value = input;
			}
			count++;
			
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
		debug2("AverageAlgorithm:afterTimeSlices, count=" + count);
		//debug1("AverageAlgorithm:afterTimeSlices, per begin="
		//+ debugSdf.format(_aggregatePeriodBegin) + ", end=" + debugSdf.format(_aggregatePeriodEnd));
		if (count >= minSamplesNeeded)
		{
			double time_diff = (previous_time.getTime() - first_time.getTime())/1000.0;
			debug3( "Final Volume is " + tally );
			debug3( "Averaging over " + time_diff + " seconds");
			setOutput( average,  tally/time_diff );	
		}
		else 
		{
			warning("Do not have minimum # samples (" + minSamplesNeeded
				+ ") -- not producing an average.");
			if (_aggInputsDeleted)
				deleteOutput(average);
		}
                end_t = java.lang.System.nanoTime();
                long diff = end_t - start_t;
                double diff_ms = diff/1000000.0;
                info(" Elapsed Time (ns) " + diff );
                info("              (ms) " + diff_ms);
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
