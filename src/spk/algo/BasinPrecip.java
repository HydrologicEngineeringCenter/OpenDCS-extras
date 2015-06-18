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
import decodes.cwms.CwmsFlags;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Computes the Basin Precip with up to 6 stations and the Station Normal Annual
 * Precipitation and Basin Normal Annual Precipitation. To have an input value
 * ignored, set the nap value to 0 and delete that time series from the list.
 * If 1 or more stations are missing, that time slice will be marked
 * questionable. All input time series should have missing values ignored,
 * the algorithm designed for those cases.
 *
 * @author L2EDDMAN

 */
//AW:JAVADOC_END
public class BasinPrecip
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double rain1;	//AW:TYPECODE=i
	public double rain2;	//AW:TYPECODE=i
	public double rain3;	//AW:TYPECODE=i
	public double rain4;	//AW:TYPECODE=i
	public double rain5;	//AW:TYPECODE=i
	public double rain6;	//AW:TYPECODE=i
        public double rain7;    //AW:TYPECODE=i
        public double rain8;    //AW:TYPECODE=i
	String _inputNames[] = { "rain1", "rain2", "rain3", "rain4", "rain5", "rain6", "rain7", "rain8" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	double station_precip; // all availbe incremental precips added together
	double stations_normal_precip; // this time slices nap total. If a station is missing it is not factored into the calculation in anyway.

//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable BasinPrecip = new NamedVariable("BasinPrecip", 0);
	String _outputNames[] = { "BasinPrecip" };
//AW:OUTPUTS_END

//AW:PROPERTIES
	public double bnap = 0.0;
	public double nap1 = 0.0;
	public double nap2 = 0.0;
	public double nap3 = 0.0;
	public double nap4 = 0.0;
	public double nap5 = 0.0;
	public double nap6 = 0.0;
        public double nap7 = 0.0;
        public double nap8 = 0.0;
	String _propertyNames[] = { "bnap", "nap1", "nap2", "nap3", "nap4", "nap5", "nap6", "nap7", "nap8" };
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
		if( bnap == 0.0 )
		{
			throw new DbCompException( "You must specify a non zero bnap value." );
		}
		if( nap1 == 0.0 && nap2 == 0.0 && nap3 == 0.0 && nap4 == 0.0 && nap5 == 0.0 && nap6 == 0.0 && nap7 == 0.0 && nap8 == 0.0 )
		{
			throw new DbCompException( "You must specifiy at least 1 non zero nap value" );
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
		double bp;
		station_precip = 0.0;
		stations_normal_precip = 0.0;
		if( isMissing( rain1 ) &&
		    isMissing( rain2 ) &&
		    isMissing( rain3 ) &&
		    isMissing( rain4 ) &&
		    isMissing( rain5 ) &&
		    isMissing( rain6 ) &&
                    isMissing( rain7 ) &&
                    isMissing( rain8 ))
		    {
		    	// if all of the data are missing there is no point in running the calculation
			warning( "All rain inputs are missing or all input data was deleted" );
			deleteOutput( BasinPrecip );
			return;
		    }
		// run down the possible inputs
		// we only care about the non zero nap values
		if( nap1 != 0.0 )
		{
			if( !isMissing( rain1 ) )
			{
				station_precip += rain1;
				stations_normal_precip += nap1;
			}
			else
			{
				debug3("rain 1 is missing for this timeslice" );
				setFlagBits( BasinPrecip, CwmsFlags.VALIDITY_QUESTIONABLE );
			}
		}

		if( nap2 != 0.0 )
		{
			if( !isMissing( rain2 ) )
			{
				station_precip += rain2;
				stations_normal_precip += nap2;
			}
			else
			{
				debug3("rain 2 is missing for this timeslice" );
				setFlagBits( BasinPrecip, CwmsFlags.VALIDITY_QUESTIONABLE );
			}
		}
		if( nap3 != 0.0 )
		{
			if( !isMissing( rain3 ) )
			{
				station_precip += rain3;
				stations_normal_precip += nap3;
			}
			else
			{
				debug3("rain 3 is missing for this timeslice" );
				setFlagBits( BasinPrecip, CwmsFlags.VALIDITY_QUESTIONABLE );
			}
		}
		if( nap4 != 0.0 )
		{
			if( !isMissing( rain4 ) )
			{
				station_precip += rain4;
				stations_normal_precip += nap4;
			}
			else
			{
				debug3("rain 4 is missing for this timeslice" );
				setFlagBits( BasinPrecip, CwmsFlags.VALIDITY_QUESTIONABLE );
			}
		}
		if( nap5 != 0.0 )
		{
			if( !isMissing( rain5 ) )
			{
				station_precip += rain5;
				stations_normal_precip += nap5;
			}
			else
			{
				debug3("rain 5 is missing for this timeslice" );
				setFlagBits( BasinPrecip, CwmsFlags.VALIDITY_QUESTIONABLE );
			}
		}
		if( nap6 != 0.0 )
		{
			if( !isMissing( rain6 ) )
			{
				station_precip += rain6;
				stations_normal_precip += nap6;
			}
			else
			{
				debug3("rain 6 is missing for this timeslice" );
				setFlagBits( BasinPrecip, CwmsFlags.VALIDITY_QUESTIONABLE );
			}
		}
                if( nap7 != 0.0 )
		{
			if( !isMissing( rain7 ) )
			{
				station_precip += rain7;
				stations_normal_precip += nap7;
			}
			else
			{
				debug3("rain 7 is missing for this timeslice" );
				setFlagBits( BasinPrecip, CwmsFlags.VALIDITY_QUESTIONABLE );
			}
		}
                if( nap8 != 0.0 )
		{
			if( !isMissing( rain8 ) )
			{
				station_precip += rain8;
				stations_normal_precip += nap8;
			}
			else
			{
				debug3("rain 8 is missing for this timeslice" );
				setFlagBits( BasinPrecip, CwmsFlags.VALIDITY_QUESTIONABLE );
			}
		}
		bp = ( bnap/stations_normal_precip)*station_precip;
		debug3(" Basin Precip = " + bp + " = (" + bnap + "/" + stations_normal_precip + ")*"+station_precip );
		setOutput( BasinPrecip, bp );
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
