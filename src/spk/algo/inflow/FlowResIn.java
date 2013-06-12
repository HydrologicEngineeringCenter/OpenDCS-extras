package spk.algo.inflow;

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
import decodes.util.DecodesException;
import java.util.concurrent.TimeUnit;
//AW:IMPORTS_END

//AW:JAVADOC
/**
    Calculates reservoir input with the equation
 *  inflow = /\Storage + Evap + Outflow
 *  
 *  Outflow should be provided in cfs
 * Storage and Evap should be provided in ac-ft and will be converted to cfs based on the
 * interval of data.
 * ( this comp will need to be created 3 times for each project, 15minutes, 1hour, and 1day )
 * there may be a way to group things
 *
 * NOTE: there are ac-ft to cfs conversions build into this comp, do NOT use metric input, the comp
 *       will provide bogus results.
 *
 * @author L2EDDMAN
 *
 */
//AW:JAVADOC_END
public class FlowResIn
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double ResOut;	//AW:TYPECODE=i
	public double Evap;	//AW:TYPECODE=i
	public double Dstor;	//AW:TYPECODE=id
	String _inputNames[] = { "ResOut", "Evap", "Dstor" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        UnitConverter storConv;
        UnitConverter evapConv;
        UnitConverter flowConv;
        UnitConverter outputConv;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable ResIn = new NamedVariable("ResIn", 0);
	String _outputNames[] = { "ResIn" };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public boolean UseEvap = false;
	String _propertyNames[] = { "UseEvap" };
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
                EngineeringUnit storUnits = EngineeringUnit.getEngineeringUnit(getParmRef("Dstor").timeSeries.getUnitsAbbr());
                EngineeringUnit flowUnits = EngineeringUnit.getEngineeringUnit(getParmRef("ResOut").timeSeries.getUnitsAbbr());
                EngineeringUnit outputUnits = EngineeringUnit.getEngineeringUnit(getParmRef("ResIn").timeSeries.getUnitsAbbr());

                if( UseEvap)
                {
                    EngineeringUnit evapUnits = EngineeringUnit.getEngineeringUnit(getParmRef("Evap").timeSeries.getUnitsAbbr());
                    evapConv = decodes.db.CompositeConverter.build( evapUnits, EngineeringUnit.getEngineeringUnit("ac-ft"));
                }
                storConv = decodes.db.CompositeConverter.build(storUnits, EngineeringUnit.getEngineeringUnit("ac-ft"));
                flowConv = decodes.db.CompositeConverter.build(flowUnits, EngineeringUnit.getEngineeringUnit("cfs"));
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
                String interval = this.getInterval("ResOut").toLowerCase();
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

                    conversion = 0.50417;
                }
                debug3( "Conversion factor = " + conversion);
                
                try{
                    debug3("Convert the units");
                    Dstor = storConv.convert(Dstor);
                    ResOut = flowConv.convert(ResOut);
                    
                    if( UseEvap )
                    {
                        // note there are notes with the WC manuals that indicate
                        // that on the 23 and 25 hours days ( because of daylight savings)
                        // other factors should be used. This is an initial test
                        // so that will be taken into account later, or
                        // i'll keep fighting for doing everything in PST
                        // like the memo that was found says to.
                        // M. Neilson. 2011Nov16.
                        Evap = evapConv.convert(Evap);
                        
                        double flow = (Dstor+Evap)*conversion;
                        debug3("performing calculation with evap");
                        debug3( "Dstor = " + Dstor + ", Evap = " + Evap + ", Outflow = " + ResOut);
                        debug3( " Dstor + Evap = " + (Dstor+Evap) + " ---> " + flow);
                        in = flow + ResOut;
                    }
                    else
                    {
                        debug3("performaing calculation without evap");
                        debug3( "Dstor = " + Dstor + ", Outflow = " + ResOut);
                        debug3( " Dstor  in cfs = " + (Dstor*conversion) );
                        // The 15 minute and hourly calculation do not use evap
                        in = Dstor*conversion + ResOut;
                    }
                    in = outputConv.convert(in);
                
                    if( in >= 0.0)
                    {
                        setOutput( ResIn, in );
                    }
                    else
                    {
                        warning("Inflow set to 0 because the calculation came out to less than 0");
                        setOutput( ResIn, 0 );
                    }
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
