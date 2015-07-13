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
import decodes.db.EngineeringUnit;
import decodes.db.UnitConverter;
import decodes.tsdb.ParmRef;
import decodes.tsdb.algo.AggregatePeriod;
import decodes.util.DecodesException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
//AW:IMPORTS_END

//AW:JAVADOC
/**
   converters a volume to a flow for a given interval.
 * @author L2EDDMAN
 *
 */
//AW:JAVADOC_END
public class VolumeToFlow
	//extends decodes.tsdb.algo.AW_AlgorithmBase
        extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS	
	public double stor;	//AW:TYPECODE=id
	String _inputNames[] = { "stor" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        UnitConverter storConv;
        UnitConverter evapConv;
        UnitConverter flowConv;
        UnitConverter outputConv;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable flow = new NamedVariable("flow", 0);
	String _outputNames[] = { "flow" };
//AW:OUTPUTS_END

//AW:PROPERTIES
        
	String _propertyNames[] = { };
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
                EngineeringUnit storUnits = EngineeringUnit.getEngineeringUnit(getParmRef("stor").timeSeries.getUnitsAbbr());                
                EngineeringUnit outputUnits = EngineeringUnit.getEngineeringUnit(getParmRef("flow").timeSeries.getUnitsAbbr());

                
                storConv = decodes.db.CompositeConverter.build(storUnits, EngineeringUnit.getEngineeringUnit("ac-ft"));                
                outputConv = decodes.db.CompositeConverter.build(EngineeringUnit.getEngineeringUnit("cfs"), outputUnits );
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
		double in = 0;
                double conversion = 1.0;
                String interval = this.getInterval("flow").toLowerCase();
                debug3( "Finding conversion for internval: " + interval );
                if( interval.equals( "15minutes"))
                {
                    // y ac-ft = x cfs * 900 sec * 2.29568411*10^-5 ac-ft/ft^3
                    // 900 seconds = 15 minutes
                    // we are converting to cfs from ac-ft so divided by this factor
                    conversion = 1/.020618557;
                }
                else if( interval.equals( "1hour"))
                {
                    conversion = 12.1;
                }
                else if( interval.equals( "1day") || interval.equals( "~1day") )
                {
                    
                    //long time_difference = (_aggregatePeriodEnd.getTime() - _aggregatePeriodBegin.getTime())/(1000*60*60);
                    //long time_difference = 24;
                    GregorianCalendar t1 = new GregorianCalendar();
                    GregorianCalendar t2 = new GregorianCalendar();
                    t1.setTimeZone(aggTZ);
                    t2.setTimeZone(aggTZ);
                    t1.setTime(_timeSliceBaseTime);
                    t2.setTime(_timeSliceBaseTime);
                    // this might need to be -1 and on T1, investigate monday with PH1 and PH2 of englebright
                    t2.add(Calendar.DAY_OF_MONTH, 1);
                    
                    // since these are the UTC values we'll get the right time difference
                    long time_difference = ( t2.getTimeInMillis() - t1.getTimeInMillis() )/ (1000*60*60);
                    
                    if( time_difference == 24 )
                    {
                        conversion = 0.50417;
                    }
                    else if( time_difference == 23){
                        conversion = 0.52609;
                    }
                    else if( time_difference == 25){
                        conversion = 0.48400;
                    }
                    else{
                        throw new DbCompException( "You have entered some weird daylight savings that adjusts by more than an hour...if this is true, 1st: sorry. 2nd: see the programmer about expanding this section of the code" );
                    }
                        
                }
                else{
                    throw new DbCompException("The interval you have used is not supported at this time");
                }
                debug3( "Conversion factor = " + conversion);
                
                try{
                    debug3("Convert the units");
                    stor = storConv.convert(stor);
                    in = stor*conversion;                    
                    in = outputConv.convert(in);                                    
                    setOutput( flow, in );
                                        
                }
                catch( DecodesException ex)
                {
                    throw new DbCompException( "There are no conversion from the units you provided to the needed units");
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
                
                /*
                 * smoothing
                 * 
                 * go through queue, interpolate between data.
                 * 
                 * get the previous value, get the next value interpolate.
                 * ( this works if the value is stored or if we can get it within
                 * the address space of this function.
                 * 
                 * 
                 */                 
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
