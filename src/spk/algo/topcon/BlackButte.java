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
import spk.algo.support.WaterControlDiagram;
import spk.algo.support.DateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.lang.Math;
import spk.algo.support.Dates;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Handle the upstream storage calculation as defined in the Black Butte Water
 * Control Diagram. 
 *
 * @author L2EDDMAN
 *
 */
//AW:JAVADOC_END
public class BlackButte
	extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
	public double EastPark;	//AW:TYPECODE=i
	public double StonyGorge;	//AW:TYPECODE=i
        public double FloodControlParameter;  //AW:TYPECODE=i
	String _inputNames[] = { "EastPark", "StonyGorge", "FloodControlParameter" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        WaterControlDiagram graph = null;
        // These are used for upstream storage( see section 3b1 of WC Diagram )
        //Date sep = null;
        //Date feb = null;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable Adjustment = new NamedVariable("Adjustment", 0);
        public NamedVariable AllowedStorageUnbound = new NamedVariable("AllowedStorageUnbound", 0);
        public NamedVariable AllowedStorage = new NamedVariable("AllowedStorage", 0);
        public NamedVariable AllowedStorageRain = new NamedVariable("AllowedStorageRain", 0);
        public NamedVariable EastParkAvailable = new NamedVariable( "EastParkAvailable", 0);
        public NamedVariable StonyGorgeAvailable = new NamedVariable( "StonyGorgeAvailable", 0);
	String _outputNames[] = { "AllowedStorage", "AllowedStorageUnbound","AllowedStorageRain", "EastParkAvailable", "StonyGorgeAvailable", "Adjustment"  };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public String graph_file = "";
        public boolean StorAll = true;
	String _propertyNames[] = { "graph_file", "StorAll" };
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
                try{
                    debug3( "loading graph");
                    graph = new WaterControlDiagram( graph_file );
                    debug3( "setting bouding dates for upstream storage");
                    // these dates bound: sep,oct,nov,dec,jan,feb so we must bump

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
	 * Sections in comments refere to the Water Control Diagram.
	 *
	 * @throws DbCompException (or subclass thereof) if execution of this
	 *        algorithm is to be aborted.
	 */
	protected void doAWTimeSlice()
		throws DbCompException
	{
//AW:TIMESLICE
                
                Dates dates = new Dates( _timeSliceBaseTime);
                double stony = 0.0;
                double east = 0.0;
                double adjustment = 0.0;
                double allowed_storage_unbounded = 0.0;
                double allowed_storage = 0.0;
		// Enter code to be executed at each time-slice.
                
                int wy_day = DateTime.to_wy_julian(_timeSliceBaseTime); 
                debug3( "******************************");
                debug3( "******************************");
                debug3( "Calculating TCS for date(julian):" + _timeSliceBaseTime + " ( " + wy_day + " )" );
                if( isMissing( FloodControlParameter ))
                {
                    info( "Current Implementation does not handle missing values, moving to next timeslice" );
                    return;
                }
                try{
                    // get the initial top con value
                    debug2( "Getting Base TCS value" );
                    double tcs_rain = graph.get_allowed_storage(wy_day - 1, FloodControlParameter);// all of the graphs are zero based
                    debug3( " TCS for rain is " + tcs_rain );
                    
                    debug3( "retrieving East Park and Stony Gorge, if missing");
                    if( isMissing( EastPark ) )
                    {
                        EastPark = this.getPrevValue("EastPark", _timeSliceBaseTime);
                    }
                    if( isMissing( StonyGorge ) ){
                        StonyGorge = this.getPrevValue("StonyGorge", _timeSliceBaseTime);
                    }                     
                    // upstream storage, see Black Butte WC Diagram: USE OF DIAGRAM: section 3
                    debug3( "Calculating East Park Allowed Storage" );
                    // Section 3a
                    //               3a1            3a2
                    east = Math.min( 48200 - EastPark, 10000);
                    debug3(" East Park availabe is " + east );
                    //
                    // Section 3b
                    //
                    debug3( "Calculating Stony Gorge Allowed Storage" );
                    if( wy_day > dates.September01 || wy_day < dates.February01 )
                    {// section 3b1 is in effect
                        stony = 28400.0 - StonyGorge;
                        debug3( " stony gorge available (3b1) is " + stony );
                    }
                    else
                    {// section 3b2 is in effect
                        stony = Math.max( 0, 28400.0 - StonyGorge );
                        debug3( " stony gorge available (3b2) is " + stony);
                    }
                    debug3( "Calculating allowed storage adjustment");
                    // Section 3c
                    adjustment = Math.max( 0, Math.min( 40000, east + stony ) );
                    debug3( "Adjustment is " + adjustment );
                    // confirm above with Kyle Keer before "production"
                    //  I belive there was something about this never going
                    //  below zero, but I don't see that in the jython version
                    //  I wrote. Or the Diagram.
                    //  Talked to Kevin Richardson, basic reasoning, we don't give up
                    //  credit space in our reservoirs.


                    debug3 ( "calculating unbounded storage" );                    
                    allowed_storage_unbounded = tcs_rain + adjustment;
                    debug3( " unbounded storage is " + allowed_storage_unbounded );
                   
                    debug3 ( "Applying bounds to storage" );                                      
                    allowed_storage = graph.bound(wy_day, allowed_storage_unbounded );
                    debug3( " bounded storage is " + allowed_storage );
                    
                    setOutput(AllowedStorage, allowed_storage);
                    if( StorAll == true )
                    { // we may not always want to record these values
                        setOutput(Adjustment, adjustment);
                        setOutput(EastParkAvailable, east);
                        setOutput(StonyGorgeAvailable, stony);
                        setOutput(AllowedStorageRain, tcs_rain);
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
}
