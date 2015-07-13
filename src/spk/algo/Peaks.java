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

//AW:IMPORTS
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Over the desired interval, get all of the peak points.
 * 
 *
 * @author L2EDDMAN
 */
//AW:JAVADOC_END
public class Peaks
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS        
	public double input;	//AW:TYPECODE=i
	String _inputNames[] = { "input" };
//AW:INPUTS_END

//AW:LOCALVARS
	private ArrayList<Double> data;
        private ArrayList<Date> dates;
	
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable peaks = new NamedVariable("peaks", 0);	
	String _outputNames[] = { "peaks" };
//AW:OUTPUTS_END

//AW:PROPERTIES
	public double minSamples = 0;
        
	String _propertyNames[] = { "minSamples"  };
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
		_aggPeriodVarRoleName = "peaks";                
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
		data = new ArrayList<Double>();
                dates = new ArrayList<Date>();
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
		data.add( input );
                dates.add( _timeSliceBaseTime );
//AW:TIMESLICE_END
	}

	/**
	 * This method is called once after iterating all time slices.
	 */
	protected void afterTimeSlices()
		throws DbCompException
	{
//AW:AFTER_TIMESLICES
		debug3( "We have " + data.size() + " records" );
                
                
                double values[] = new double[data.size()];//
                double slopes[] = new double[data.size()];
                double times[]  = new double[data.size()]; // get times in hours
                for( int i = 0; i < data.size(); i++ ){
                    times[i] = dates.get(i).getTime()/1000.0/60/60;
                }
                for( int i = 0; i < data.size(); i++ ){
                    values[i] = data.get(i);
                }
                /*
                 * Now is the time to be drastic. The peak will be pulled from the data array (the original data)
                 * But we really need to clean up the time series to find the peaks.
                 * As long as it's filtered in such a way as to not move the data whatever
                 * is done here should be fine.
                 * 
                 */                                
                
                
                
                for( int i = 1; i < data.size(); i++ ){
                    slopes[i] = ( values[i] - values[i-1] )/ ( times[i] - times[i-1]);                    
                }
                
                debug3("slopes calculated");
                
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
