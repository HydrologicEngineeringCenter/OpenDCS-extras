package spk.algo.topcon;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import spk.algo.support.WaterControlDiagram;
import spk.algo.support.DateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.lang.Math;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import spk.algo.support.Dates;
import spk.algo.support.IrrigationDemands;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Calculate the Top Con for Friant
 *   Friant has Snowmelt, irrigation demands, and adjustments to upstream storage
 * @author L2EDDMAN
 *
 */
//AW:JAVADOC_END
public class Friant
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double RemainingRunoff;	//AW:TYPECODE=i	      
        public double Mammoth;   //AW:TYPECODE=i
        public double TotalUpstream; //AW:TYPECODE=i
	String _inputNames[] = { "RemainingRunoff", "Mammoth", "TotalUpstream" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        WaterControlDiagram graph = null;
        IrrigationDemands irrigation;
        SimpleDateFormat df;
        Dates dates; // holds important times
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable Adjustment = new NamedVariable("Adjustment", 0);
        public NamedVariable AllowedStorageUnbound = new NamedVariable("AllowedStorageUnbound", 0);
        public NamedVariable AllowedStorage = new NamedVariable("AllowedStorage", 0);
        public NamedVariable AllowedStorageRain = new NamedVariable("AllowedStorageRain", 0);
        public NamedVariable AllowedStorageSnow = new NamedVariable("AllowedStorageSnow", 0);
        public NamedVariable Upstream = new NamedVariable("Upstream", 0);
        public NamedVariable SpaceRequired = new NamedVariable("SpaceRequired", 0);
	String _outputNames[] = { "AllowedStorage", "AllowedStorageUnbound","AllowedStorageRain", "AllowedStorageSnow", "Adjustment", "Upstream", "SpaceRequired"  };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public String graph_file = "";
        public boolean StorAll = true;
        public String irrigation_demand_file = "";
        public double max_credit = 100000.0;
        public double credit_decay = -0.15;   
        public double decay_offset = 304.5;
        public double mammoth_gross_pool = 122700;
        public double total_gross_pool = 628500; 
	String _propertyNames[] = { "graph_file", "StorAll", "irrigation_demand_file", "max_credit", "credit_decay", "decay_offset", "mammoth_gross_pool", "total_gross_pool" };
//AW:PROPERTIES_END
        // total_gross_pool should probably be 622530, if you add all of the reservoirs on the daily reports. 
        
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
                try{
                    debug3( "loading graph");
                    graph = new WaterControlDiagram( graph_file );

                    irrigation = WaterControlDiagram.get_irrigation_data(irrigation_demand_file);
                }
                catch( Exception e)
                {
                    debug3( "Failed to initialize comp: \n" + e.getMessage() );
                    debug3( "Aborting computation");
                    throw new DbCompException("Fail to initialize comp");
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
	 * Sections in comments refer to the Water Control Diagram.
	 *
	 * @throws DbCompException (or subclass thereof) if execution of this
	 *        algorithm is to be aborted.
	 */
	protected void doAWTimeSlice()
		throws DbCompException
	{
//AW:TIMESLICE                
                
                double upstream = 0.0;
                double adjustment = 0.0;
                double allowed_storage_unbounded = 0.0;
                double allowed_storage = 0.0;
                double tcs_rain = Double.NEGATIVE_INFINITY;
                double space_required = 0.0;
                dates = new Dates(_timeSliceBaseTime);
		// Enter code to be executed at each time-slice.
                // per PNF WC Diagram (USE OF DIAGRAM) paragraph 2
                //upstream = (Wishon + Courtright) - 20000; 
                
                
                int wy_day = DateTime.to_wy_julian(_timeSliceBaseTime);
                
                // per graph at top right of water control diagram
                // and section 3 of the water control manaul.
                debug3( "Calculating available upstream storage");
                if( wy_day >= dates.February01 && wy_day < dates.July31){
                    upstream = total_gross_pool - (TotalUpstream - max_credit*(1-Math.exp( credit_decay*(decay_offset - (double)wy_day))));
                }
                
                else{
                    // per "Use of Diagram, section 2" this is the in excess of 85000 bit.
                    // Talking to Kevin richardson, the bounding lines in the diagram allow for Top Con
                    // during the rain flood season to go 85000 higher than the line on the diagram.
                    upstream = mammoth_gross_pool- Mammoth;//TotalUpstream - max_credit;
                }
                
                debug3( "******************************");
                debug3( "******************************");
                debug3( "Calculating TCS for date(julian):" + _timeSliceBaseTime + " ( " + wy_day + " )" );
                
                try{
                    // get the initial top con value
                    debug2( "Getting Base TCS value" );
                    tcs_rain = graph.get_allowed_storage(wy_day - 1, 0.0);// all of the graphs are zero based
                    debug3("*************************");
                    debug3("*************************");
                    debug3( " TCS for rain is " + tcs_rain );
                    
                    //double tcs_snow = graph.get_allowed_storage(wy_day - 1, RemainingRunoff, true );
                    double tcs_snow = graph.get_allowed_storage_equation( wy_day - 1, RemainingRunoff);
                    
                    debug3( " TCS for snow is " + tcs_snow );
                    
                    
                    adjustment = this.calculate_irrigation(); 

                    if( wy_day >= dates.February01 && wy_day <= dates.July31){
                        debug3( "Calculating Irrigation Demand Adjustment" );
                        
                        space_required = Math.max( 0.0, tcs_snow - adjustment - upstream );
                        
                        allowed_storage_unbounded = graph.get_max_snowspace()-space_required;
                    }
                    else{                        
                        allowed_storage_unbounded = tcs_rain + upstream;                        
                    }
                    
                    debug3( " unbounded storage is " + allowed_storage_unbounded );
                    debug3 ( "Applying bounds to storage" );
                                       
                    allowed_storage = graph.bound(wy_day-1, allowed_storage_unbounded );
                    debug3( " bounded storage is " + allowed_storage );
                    
                    setOutput(AllowedStorage, allowed_storage);
                    if( StorAll == true )
                    { // we may not always want to record these values
                        setOutput(Adjustment, adjustment);                        
                        setOutput(AllowedStorageRain, tcs_rain);
                        setOutput(AllowedStorageSnow, tcs_snow);
                        setOutput(AllowedStorageUnbound, allowed_storage_unbounded);
                        setOutput(Upstream, upstream);
                        setOutput(SpaceRequired, space_required);
                    }

                }
                catch( Exception e )
                {
                      warning("Failed to calculated this time slice's top con due to Exception");
                      warning("Reason was\n" + e.getMessage() );
                      warning("Continuing to next time slice");
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
        
        /*
         * Cacluate the irritation to 10th of June until 26th of May.
         * After the 26th of May sum the sort of 15days or until the 31st of July
         * See "Use of Diagram Section 3" for more information
        */
        public double calculate_irrigation()throws Exception{
            
            int wy_julian_day = DateTime.to_wy_julian(_timeSliceBaseTime);
            double demands[] = irrigation.getDemands(_timeSliceBaseTime);
            double demand = 0.0;
            int end_time = -1;
            if( wy_julian_day >= dates.July31 - 15){
                end_time = dates.July31;
            }
            else if( wy_julian_day >= dates.May26 ){
                end_time = wy_julian_day+15; // next 15 days
            }
            else{
                end_time = dates.June10;
            }
                
            for( int i = wy_julian_day; i <= end_time; i++){
                demand += demands[i];
            }
            
            return demand*1.9835;
        }
        
        /*
         * @deprecated
         */
        public double calculate_irrigation(TreeMap< Date, ArrayList< Double > > irrigation )
        {
            /* 
             * All per paragraph 3 of (USE OF DIAGRAM) Pine Flat Water Control Diagram
             * 
             */
            /*
             * Dates for use in all of the logic
             * we you the timeslice generated dates
             */
            /*
            int july31 = 304;
            int july16 = 289;
            int june15 = 258;
            int june10 = 253;
            int may26  = 238;       
            int aug31 = 335;
            int aug16 = 320;
            int may31
            */
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(_timeSliceBaseTime);
            int julian_day = DateTime.to_wy_julian(_timeSliceBaseTime);
            int month = cal.get( Calendar.MONTH);
            int cur_day = cal.get(Calendar.DAY_OF_MONTH);
            int year = cal.get(Calendar.YEAR);
            /*
            if( cal.isLeapYear(year) )
            {
                aug31 +=1;
                aug16 +=1;
                july31 +=1;
                july16 +=1;
                june15 +=1;
                june10 +=1;
                may26  +=1;
            } */           
            double demand = 0.0;
            if( month < Calendar.FEBRUARY || month > Calendar.JULY )
                return 0.0; // we don't worry about irrigation at this time
            Date nearest = irrigation.floorKey(_timeSliceBaseTime);
            // demands array starts with 0=January, matches with java time
            ArrayList<Double> demands = (ArrayList<Double>)irrigation.get(nearest);
            int day_remaining = cal.getActualMaximum(cal.DAY_OF_MONTH) - cur_day + 1; // we include the current day            
            /* check date, then do every thing */
            if( julian_day >= dates.July16 && julian_day <= dates.August01) // it is now closer to 31July than 15 days
            {                               
                demand = day_remaining*demands.get(month)*1.9835;
            }
            else if( julian_day < dates.July16 && month == Calendar.JULY)
            {
                demand = demands.get(month)*15*1.9835; // just grab the next fifteen days
            }
            else if( julian_day >= dates.May31) // after 31 may it is just the next fiften days, which will never cross more than one month
            {
                demand = day_remaining*demands.get(month)*1.9835;
                demand += (15-day_remaining)*demands.get(month+1)*1.9835;                
            }
            else if( julian_day < dates.May31 )
            {
                demand = day_remaining*demands.get(month)*1.9835; // get the rest of this month
                for( int i = month+1; i <= Calendar.JUNE; i++ ) // go until june
                {
                    if( i != Calendar.JUNE)
                    {
                        demand =+ demands.get(i)*cal.getActualMaximum(i)*1.9835;
                    }
                    else if( i == Calendar.JUNE)
                    {
                        demand += demands.get(i)*15; // just until the tenth of June, so ten days in June.
                    }
                }
            }
            else
            {
                warning("one of the time conditions was not met during irrigation demand calculation");
            }
            
            
            // setup the Julian dates for the calendar
            
            /*
             *  Per "Use of Diagram" on Pine Flat water control diagram
             *  before 26 May calculate runoff to 10 June
             * After 26 May,  /  Next 15 days
             *   min of      -|
             *                \  to 31 July
             */
            
            
        

            return demand;
        }
}
