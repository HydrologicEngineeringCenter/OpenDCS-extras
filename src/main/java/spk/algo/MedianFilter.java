package spk.algo;

import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import java.util.ArrayList;
import java.util.Collections;

//AW:IMPORTS
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Implements a median filter.
 * selects a window of values, and choose the median value.
 *
 * @author L2EDDMAN
 */
//AW:JAVADOC_END
public class MedianFilter
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double input;	//AW:TYPECODE=i
	String _inputNames[] = { "input" };
//AW:INPUTS_END

//AW:LOCALVARS
	private double current_min;
	private double current_max;
	private int    numSamples;
        private ArrayList<Double> list;

//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable median = new NamedVariable("median", 0);
	String _outputNames[] = { "median" };
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
		_awAlgoType = AWAlgoType.RUNNING_AGGREGATE;
		_aggPeriodVarRoleName = "median";
                
                list = new ArrayList<Double>();
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
                list.clear();
		numSamples = 0;
                debug3("agg per Begin" + _aggregatePeriodBegin);
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
		if( !isMissing(input))
                {
                    list.add(input);
                    debug3("" + _timeSliceBaseTime + "/" +  input + " added to list");
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
		debug3("agg per End" + _aggregatePeriodEnd);
		if( list.size() > 1 ){
                    Collections.sort(list);
                    
                    debug3( "Size of list: " + list.size());
                    double _median;
                    int i = list.size()/2;
                    debug3( "Index =" + i );
                    if( list.size() % 2 == 0){
                        debug3("length of list even");
                        _median = (list.get(i) + list.get(i-1))/2;
                    }
                    else{
                        debug3("length of list uneven");
                        // if it's uneven we already have the right value
                        _median = list.get(i);
                    }                                      
                    setOutput(median, _median);
                }
                else if( list.size() == 1 ){
                    setOutput(median, list.get(0));
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
