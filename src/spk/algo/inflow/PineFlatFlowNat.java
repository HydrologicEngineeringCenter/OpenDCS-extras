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
import java.util.logging.Level;
import java.util.logging.Logger;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 *  Calculates Natural Inflow for Pine Flat
 *  Natural Flow = (Change in total upstream storage + upstream evap)-> cfs + Pine Flat Inflow
 *  
 *  Inputs are:
 *      changes in the upstream storage ( the algorithms inputs are configured by default to take the difference ).
 *      The upstream evap volumes.
 *      The Pine Flat Reservoir Inflow.
 *    
 * 
 * NOTE: there are ac-ft to cfs conversions build into this comp, do NOT use metric input, the comp
 *       will provide bogus results. I may add further conversions later.
 *
 * @author L2EDDMAN
 *
 */
//AW:JAVADOC_END
public class PineFlatFlowNat
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double ResIn;	//AW:TYPECODE=i
	public double WishonEvap;	//AW:TYPECODE=i
        public double CourtwrightEvap;	//AW:TYPECODE=i
	public double wishon;	//AW:TYPECODE=id
        public double courtwright;  //AW:TYPECODE=id
        public double balch_afterbay;   //AW:TYPECODE=id
        public double balch_forebay;    //AW:TYPECODE=id
	String _inputNames[] = { "ResIn", "WishonEvap", "CourtwrightEvap", "wishon", "courtwright", "balch_afterbay", "balch_forebay" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        UnitConverter storConv;
        UnitConverter evapConv;
        UnitConverter flowConv;
        UnitConverter outputConv;
        UnitConverter acft_cfs;
        // these hold each month of the correction
        double monthly_average_coefficients[];
        double wishon_corrections[];
        double courtwright_corrections[];
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable FlowNat = new NamedVariable("FlowNat", 0);
	String _outputNames[] = { "FlowNat" };
//AW:OUTPUTS_END

//AW:PROPERTIES        
	String _propertyNames[] = {   };
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
                /*
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
                */
                
            
            
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
            double total_upstream_delta = balch_afterbay+balch_forebay+wishon + courtwright;
            double total_upstream_evap  = WishonEvap + CourtwrightEvap;
            debug3(" Change in Upstream Storage: " + total_upstream_delta);
            debug3(" Total Upstream Evap: " + total_upstream_evap);
            debug3("calculating upstream flow");
            double upstream_flow = (total_upstream_delta + total_upstream_evap)/1.9835;
            debug3("Upstream flow: " + upstream_flow);
            double natural_flow = upstream_flow + ResIn;
            debug3("Natrual flow: " + natural_flow );

            setOutput(FlowNat, natural_flow);
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
