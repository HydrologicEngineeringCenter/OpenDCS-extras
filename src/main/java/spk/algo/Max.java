package spk.algo;

import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.ParmRef;
import ilex.var.TimedVariable;
import decodes.tsdb.TimeSeriesIdentifier;

//AW:IMPORTS
//AW:IMPORTS_END

//AW:JAVADOC
/**
Over the desired interval, computes the maximum of the input timeseries for that range.




 */
//AW:JAVADOC_END
public class Max
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double input;	//AW:TYPECODE=i
	String _inputNames[] = { "input" };
//AW:INPUTS_END

//AW:LOCALVARS
	private double current_max;
	private int    numSamples;

//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable max = new NamedVariable("max", 0);
	String _outputNames[] = { "max" };
//AW:OUTPUTS_END

//AW:PROPERTIES
	public double minSamples = 0;
	String _propertyNames[] = { "minSamples" };
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
		_aggPeriodVarRoleName = "max";
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
		// This will allow the first value to be initially selected		
		debug3( "Initializing Current_max to extreme values" );
		current_max = -999999999.0;
		numSamples = 0;
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
		if( isMissing( input ) )
		{
			debug3( "Value is missing, skipping" );
			return;
		}
		debug3( "Current Value is: " + input );
		debug3( "Current Max is  : " + current_max );
		numSamples++;
		if( input > current_max )
		{
			debug3( "Setting new maximum" );	
			current_max = input;
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
		debug3( "Selected Max is: " + current_max );
		if( numSamples >= minSamples )
		{
			setOutput( max, current_max );
		}
		else
		{
			debug3( "Only " + numSamples + " samples found, minium number specified is " + minSamples );
			debug3( "selected max will not be saved" );
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
