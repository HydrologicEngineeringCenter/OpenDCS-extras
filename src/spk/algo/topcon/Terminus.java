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
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Calculate the Top Con for Isabella
 *
 * @author L2EDDMAN
 *
 */
//AW:JAVADOC_END
public class Terminus
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
        // that should say RainFloodControlParamter....however there is a 24 character limit in that field.
//AW:INPUTS
	public double RemainingRunoff;	//AW:TYPECODE=i
        public double RainFCP;    //AW:TYPECODE=i
	String _inputNames[] = { "RemainingRunoff", "RainFCP" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        WaterControlDiagram graph = null;
        TreeMap< Date, ArrayList< Double > > irrigation;
        SimpleDateFormat df;
        // These are used for upstream storage( see section 3b1 of WC Diagram )
        //Date sep = null;
        //Date feb = null;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable Adjustment = new NamedVariable("Adjustment", 0);
        public NamedVariable AllowedStorageUnbound = new NamedVariable("AllowedStorageUnbound", 0);
        public NamedVariable AllowedStorage = new NamedVariable("AllowedStorage", 0);
        public NamedVariable AllowedStorageRain = new NamedVariable("AllowedStorageRain", 0);
        public NamedVariable AllowedStorageSnow = new NamedVariable("AllowedStorageSnow", 0);
	String _outputNames[] = { "AllowedStorage", "AllowedStorageUnbound","AllowedStorageRain", "AllowedStorageSnow", "Adjustment"  };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public String graph_file = "";
        public boolean StorAll = true;
        public String irrigation_demand_file = "";
	String _propertyNames[] = { "graph_file", "StorAll", "irrigation_demand_file" };
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
		
//AW:USERINIT_END
	}
	
	/**
	 * This method is called once before iterating all time slices.
	 */
        
	protected void beforeTimeSlices()
		throws DbCompException
	{
//AW:BEFORE_TIMESLICES
                try{
                    debug3( "loading graph");
                    graph = new WaterControlDiagram( graph_file );                
                    irrigation = WaterControlDiagram.get_irrigation_data(irrigation_demand_file);
                    /*
                     * Maybe a timeout for reload? E.g we load once and check once an hour or something...
                     */
                }
                catch( Exception e)
                {
                    debug3( "Failed to initialize comp: \n" + e.getMessage() );
                    debug3( "Aborting computation");
                    throw new DbCompException("Fail to initialize comp");
                }
		// This code will be executed once before each group of time slices.
		// For TimeSlice algorithms this is done once before all slices.
		// For Aggregating algorithms, this is done before each aggregate
		// period.
//AW:BEFORE_TIMESLICES_END
	}

	/**
	 * Section numbers in comments refer to the Water Control Diagram.
	 *
	 * @throws DbCompException (or subclass thereof) if execution of this
	 *        algorithm is to be aborted.
	 */
	protected void doAWTimeSlice()
		throws DbCompException
	{
//AW:TIMESLICE
                
                double adjustment = 0.0;
                double allowed_storage_unbounded = 0.0;
                double allowed_storage = 0.0;
		// Enter code to be executed at each time-slice.
                
                int wy_day = DateTime.to_wy_julian(_timeSliceBaseTime); 
                debug3( "******************************");
                debug3( "******************************");
                debug3( "Calculating TCS for date(julian):" + _timeSliceBaseTime + " ( " + wy_day + " )" );
                if( isMissing( RemainingRunoff ))
                {
                    info( "Current Implementation does not handle missing values, moving to next timeslice" );
                    return;
                }
                try{
                    // get the initial top con value
                    debug2( "Getting Base TCS value" );
                    double tcs_rain = graph.get_allowed_storage(wy_day - 1, RainFCP);// all of the graphs are zero based
                    debug3("*************************");
                    debug3("*************************");
                    double tcs_snow = graph.get_allowed_storage(wy_day - 1, RemainingRunoff, true );
                    debug3( " TCS for rain is " + tcs_rain );
                    debug3( " TCS for snow is " + tcs_snow );
                    
                    
                    debug3( "Calculating Irrigation Demand Adjustment" );
                    
                    // we now adjust the top con
                    //adjustment = calculate_irrigation();
                    adjustment = graph.normal_irrigation(_timeSliceBaseTime);
                    /*
                     *  TODO: Change this to be between Nov15th and Mar 1
                     *        Just the Rain Space, other wise the minimum of the two
                     * (10th November, ~2nd April)
                     */                    
                    if( wy_day > 41 && wy_day <184){
                        allowed_storage_unbounded = tcs_snow;
                    }else{
                        allowed_storage_unbounded = Math.min( tcs_snow + adjustment, tcs_rain );
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
         * 
         */
        public double calculate_irrigation()
        {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(_timeSliceBaseTime);
            int month = cal.get( Calendar.MONTH);
            int cur_day = cal.get(Calendar.DAY_OF_MONTH);
            double demand = 0.0;
            if( month < Calendar.FEBRUARY || month > Calendar.JULY )
                return 0.0; // we don't worry about irrigation at this time
            Date nearest = irrigation.floorKey(_timeSliceBaseTime);
            // demands array starts with 0=January, matches with java time
            ArrayList<Double> demands = (ArrayList<Double>)irrigation.get(nearest);
            int day_remaining = cal.getActualMaximum(cal.DAY_OF_MONTH) - cur_day + 1; // we add the current day
            demand = day_remaining*demands.get(month)*1.9835;

            for( int i=month+1; i <= Calendar.JUNE; i++ ) // go to 30 june
            {
                cal.set( cal.MONTH, i);
                demand += demands.get( cal.get(Calendar.MONTH))*cal.getActualMaximum(Calendar.DAY_OF_MONTH)*1.9835;
            }

            return demand;
        }
}
