/**
 * LOWESS Smoother
 * 
 * Use the apache commons math to perform lowess smoothing on a set of data
 *
 */
package spk.algo;

import java.util.Date;

import ilex.util.EnvExpander;
import ilex.util.Logger;
import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.cwms.CwmsTimeSeriesDb;
import decodes.cwms.rating.CwmsRatingDao;
import decodes.db.Constants;
import decodes.db.SiteName;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
//import decodes.tsdb.T;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.ParmRef;
import ilex.var.TimedVariable;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;

//AW:IMPORTS
import hec.data.RatingException;
import hec.data.cwmsRating.RatingSet;

import java.util.ArrayList;

import decodes.tsdb.TimeSeriesIdentifier;
import decodes.util.TSUtil;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Implements a lowess smoother to get an output value.
*  parameters are fraction (called bandwidth in apache library) and window which is the number of intervals of data to use. 
* 
*/
//AW:JAVADOC_END
public class LowessSmoother
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double rough;	//AW:TYPECODE=i
	String _inputNames[] = { "rough" };
//AW:INPUTS_END

//AW:LOCALVARS
	LoessInterpolator loess = null;
	Date beginTime = null;
	Date endTime = null;
        Date lastTimeSlice = null;
	ArrayList<Long> indepTimes = new ArrayList<Long>();
	ArrayList<Double> indepValues = new ArrayList<Double>();		
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable smooth = new NamedVariable("smooth", 0);
	String _outputNames[] = { "smooth" };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public double fraction = 1.0;       
        public long  iterations = 3;
        public long  minSamplesNeeded = 72;
	public String _propertyNames[] = { "fraction", "iterations", "minSamplesNeeded"  };
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
                _aggPeriodVarRoleName = "smooth";
                loess = new LoessInterpolator(fraction, (int)iterations);
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

		indepTimes.clear();
		indepValues.clear();
//AW:BEFORE_TIMESLICES_END
	}

	/**
	 * Do the algorithm for a single time slice.
	 * AW will fill in user-supplied code here.
	 * Base class will set inputs prior to calling this method.
	 * User code should call one of the setOutput methods for a time-slice
	 * output variable.
	 *
	 * @throw DbCompException (or subclass thereof) if execution of this
	 *        algorithm is to be aborted.
	 */
	protected void doAWTimeSlice()
		throws DbCompException
	{
//AW:TIMESLICE		
            // Just collect the times & values. We do the rating after Time Slices.
            if(!isMissing(rough)){
		indepTimes.add(_timeSliceBaseTime.getTime());
		indepValues.add(rough);
                lastTimeSlice = _timeSliceBaseTime;
            }
//AW:TIMESLICE_END
	}

	/**
	 * This method is called once after iterating all time slices.
	 */
	protected void afterTimeSlices()
	{
//AW:AFTER_TIMESLICES
                if( indepValues.size() >= minSamplesNeeded ){
                    double []times = new double[indepTimes.size()];
                    double []vals = new double[indepTimes.size()];
                    for(int i=0; i<times.length; i++)
                    {
                            times[i] = indepTimes.get(i);
                            vals[i] = indepValues.get(i);
                    }

                    double smoothed_data[] = loess.smooth(times, vals);
                    if( !_aggregatePeriodEnd.after(lastTimeSlice) ){
                        setOutput(smooth, smoothed_data[smoothed_data.length-1]);
                    }
                } else {
                    debug1("Insufficient valid data for this timeslice");
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
