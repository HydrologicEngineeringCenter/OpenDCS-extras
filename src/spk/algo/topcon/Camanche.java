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
 * Calculate the Top Con for Camanche
 *   Camache has Snowmelt, and adjustments to upstream storage
 * @author L2EDDMAN
 *
 */
//AW:JAVADOC_END
public class Camanche
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double RemainingRunoff;	//AW:TYPECODE=i	      
        public double salt_springs;   //AW:TYPECODE=i
        public double lower_bear; //AW:TYPECODE=i
        public double pardee; //AW:TYPECODE=i
        
	String _inputNames[] = { "RemainingRunoff", "salt_springs", "lower_bear", "pardee" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        WaterControlDiagram graph = null;
        IrrigationDemands irrigation;
        SimpleDateFormat df;
        Dates dates; // holds important times
//AW:LOCALVARS_END

//AW:OUTPUTS
        public NamedVariable AllowedStorage = new NamedVariable("AllowedStorage", 0);
        public NamedVariable AllowedStorageUnbound = new NamedVariable("AllowedStorageUnbound", 0);
        public NamedVariable AllowedStorageRain = new NamedVariable("AllowedStorageRain", 0);
        public NamedVariable AllowedStorageSnow = new NamedVariable("AllowedStorageSnow", 0);        
        public NamedVariable NonTransferableSpace = new NamedVariable("NonTransferableSpace", 0);
        public NamedVariable TransferableSpace = new NamedVariable("TransferableSpace", 0 );
        public NamedVariable GrossReservation = new NamedVariable("GrossReservation", 0 );
        public NamedVariable SnowCredit = new NamedVariable("SnowCredit", 0);
        public NamedVariable RainCredit = new NamedVariable("RainCredit", 0);
                
	String _outputNames[] = { "AllowedStorage", "AllowedStorageUnbound","AllowedStorageRain", "AllowedStorageSnow", "NonTransferableSpace", "TransferableSpace", "GrossReservation", "SnowCredit", "RainCredit"  };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public String graph_file = "";
        public boolean StorAll = true;
        public String irrigation_demand_file = "";
        public double max_rain_salt_springs = 90000;
        public double max_snow_salt_springs = 140000;
        public double percent_salt_springs = .75;
        public double max_rain_lower_bear = 34000;
        public double max_snow_lower_bear = 52000;
        public double percent_lower_bear = .25;
        public double snow_percent = .8;
        public double pardee_gross_pool = 197950;
	String _propertyNames[] = { "graph_file", "StorAll", "irrigation_demand_file", "max_rain_salt_springs", "max_snow_salt_springs", "percent_salt_springs","max_rain_lower_bear", "max_snow_lower_bear", "percent_lower_bear", "snow_percent", "pardee_gross_pool"};
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
                
                
                //rain
                //salt springs 
                // max_rain, salt percent TS
                
                //lower bear
                // max rain, lower bear percent TS
                
                // snow
                // salt springs
                // max snow, total percent
                
                // lower bear
                // max snow //total percent
                
            
                try{
                
                    dates = new Dates(_timeSliceBaseTime);
  

                    int wy_day = DateTime.to_wy_julian(_timeSliceBaseTime);

                    // section 1
                    double tcs_rain                    = graph.get_allowed_storage(wy_day-1, 0);
                    double tcs_snow                    = graph.get_allowed_storage(wy_day-1, RemainingRunoff, true);
                    double non_transferable_space      = graph.get_allowed_storage(wy_day-1, 1);
                    
                    // the math in the diagram is based on these being space required, the difl file has everything listed
                    // Actual Top of Conservation
                    double gross_reservation = Math.max( graph.get_upper_bound(wy_day-1) - tcs_rain, graph.get_upper_bound(wy_day-1)-tcs_snow ); 
                    if( gross_reservation > 200000){
                        gross_reservation = 200000;
                    }
                    
                    //section 2
                    double transferable_space      = gross_reservation - (graph.gross_pool()-non_transferable_space);
                    
                    // rain credits
                    //section 3 a
                    double salt_springs_space_available = Math.max(0, max_rain_salt_springs-salt_springs);
                    double salt_springs_rain_credit = Math.min( percent_salt_springs*transferable_space, salt_springs_space_available);
                    
                    //secton 3 b
                    double lower_bear_space_available = Math.max(0 , max_rain_lower_bear - lower_bear);
                    double lower_bear_rain_credit = Math.min(percent_lower_bear*transferable_space, lower_bear_space_available);
                    
                    // section 3 c
                    double rain_credit = lower_bear_rain_credit + salt_springs_rain_credit;
                    
                    // section 4 a
                    double salt_springs_snow_credit = Math.min( 
                            snow_percent*(max_snow_salt_springs-(salt_springs_rain_credit+salt_springs)), // section 4 a 1
                            percent_salt_springs*gross_reservation // section 4 a 2                            
                            );
                    // section 4 b
                    double lower_bear_snow_credit = Math.min(
                             snow_percent*(max_snow_lower_bear-(lower_bear+lower_bear_rain_credit)), // section 4 b 1
                             percent_lower_bear*gross_reservation // section 4 b 2
                             
                            );
                    
                    // section 4 c
                    double snow_credit = lower_bear_snow_credit + salt_springs_snow_credit;
                    
                    // section 5
                    double credit = snow_credit + rain_credit;
                    
                    // section 6
                    
                    double ebmud = gross_reservation - credit; // we subtract the credit here
                    
                    // section 7
                    double pardee_space_available = pardee_gross_pool - pardee;
                    double camanche_unbound = graph.gross_pool() - (ebmud - pardee_space_available);
                    
                    //double camanche = Math.min( graph.gross_pool(), camanche_unbound);
                    double camanche = graph.bound(wy_day-1, camanche_unbound);
                    
                    setOutput(AllowedStorage, camanche);
                    
                    
                    
                    //setOutput(AllowedStoragePardee, pardee);
                    if( StorAll == true )
                    { // we may not always want to record these values                                         
                        setOutput(GrossReservation, gross_reservation );
                        setOutput(NonTransferableSpace, non_transferable_space );
                        setOutput(SnowCredit, snow_credit);
                        setOutput(RainCredit, rain_credit);
                        setOutput(TransferableSpace, transferable_space);
                        setOutput(AllowedStorageRain, tcs_rain);
                        setOutput(AllowedStorageSnow, tcs_snow);
                        setOutput(AllowedStorageUnbound, camanche_unbound);                                                
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
