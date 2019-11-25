package spk.algo;

import decodes.tsdb.algo.*;
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
 * This computation includes it's output in its input. This input MUST be specified
 * to be 1 interval previous or you WILL get stuck in and endless loop.
 */
//AW:JAVADOC_END
public class CorrectedRainProcessor
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double orig;	//AW:TYPECODE=id
	public double rev_in;	//AW:TYPECODE=i
	String _inputNames[] = { "orig", "rev_in" };
//AW:INPUTS_END

//AW:LOCALVARS
	double previous;
	

//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable rev_out = new NamedVariable("rev_out", 0);
	String _outputNames[] = { "rev_out" };
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
//AW:INIT_END
                previous = Double.NEGATIVE_INFINITY;
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
		if( !isMissing(orig ) ){
                    // logic to check flags
                    // logic to check for start of water year
                    if( !isMissing(rev_in)){
                        previous = rev_in; // TODO: this should also be a water year start check
                    } else if( isMissing(previous)){
                        previous = 0.0;
                    }
                    
                    previous = previous + orig;
                    
                    setOutput(rev_out,  previous );
                    
                } 
                // else do nothing, we are either and the end or someone needs to get in an edit data.
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
