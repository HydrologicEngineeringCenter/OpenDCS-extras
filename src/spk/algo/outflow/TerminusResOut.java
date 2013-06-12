package spk.algo.outflow;

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
import decodes.cwms.CwmsFlags;
//AW:IMPORTS_END

//AW:JAVADOC
/**<pre>
 *
 *
 * Calculates total reservoir outflow for Terminus dam.
 * total out = {
 *          TRMQ + Lemon Cove ( LCV) + Hawkeye if TRMQ > 0cfs
 *          Foothill( FTH ) + Lemon Cove ( LCV) + Hawkeye otherwise
 * }
 *
 * </pre>
 * @author L2EDDMAN
 */
//AW:JAVADOC_END
public class TerminusResOut
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double Q;	//AW:TYPECODE=i
	public double LMN;	//AW:TYPECODE=i
	public double FTH;	//AW:TYPECODE=i
        public double HWK;    //AW:TYPECODE=i
	String _inputNames[] = { "Q", "LMN", "FTH", "HWK" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.

//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable ResOut = new NamedVariable("ResOut", 0);
	String _outputNames[] = { "ResOut" };
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

//AW:USERINIT
		// Code here will be run once, after the algorithm object is created.
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
                if( !isMissing(Q) && !isMissing(LMN) )
                {
                        double out = Double.NEGATIVE_INFINITY;
                        if( Q <= 0.0)
                        {
                            if(!isMissing(FTH ) )
                            {
                                out = FTH+LMN;
                                debug3("outflow using FTH and LMN = " + out );
                            }
                        }
                        else
                        {
                            out = Q + LMN;
                            debug3("outflow using TRMQ and LMN = " + out);
                        }
                	if( !isMissing( HWK ) && HWK >= 0.0 )
                	{
                                
                		out += HWK; // we will allow this one value to be missing as it is a small value usually <= 8
                	}
                	else
                        {       // however, it should be pointed out that the value is not complete
                                debug3("no hawkeye ditch");
        			setFlagBits( ResOut, CwmsFlags.VALIDITY_QUESTIONABLE );
                	}
        		setOutput( ResOut, out );
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
}
