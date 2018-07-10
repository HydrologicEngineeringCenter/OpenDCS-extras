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
 * Handle Top Con for a site that only has a simple Snow Diagram
 * 
 *
 * @author L2EDDMAN
 *
 */
//AW:JAVADOC_END
public class LittleAndMountainDell
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS	
        public double FloodControlParameter;  //AW:TYPECODE=i
	String _inputNames[] = { "FloodControlParameter" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        WaterControlDiagram graph = null;        
//AW:LOCALVARS_END

//AW:OUTPUTS	
        public NamedVariable LittleDell = new NamedVariable("LittleDell", 0);        
        public NamedVariable MountainDell = new NamedVariable("MoutainDell", 0);        
	String _outputNames[] = { "LittleDell", "MoutainDell"   };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public String graph_file = "";       
        public double max_in_mountain_dell = 2000;
        public double mountain_rainspace = 1000;
        public double little_rainspace = 3000;
	String _propertyNames[] = { "graph_file","max_in_mountain_dell","mountain_rainspace","little_rainspace" };
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
                Dates dates = new Dates( _timeSliceBaseTime);
                
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
                    //section 2
                    double tcs_snow = 19700.0 - graph.get_allowed_storage(wy_day - 1, FloodControlParameter, true);// all of the graphs are zero based
                    
                    debug3( " TCS for snow is " + tcs_snow );
                    
                    
                    allowed_storage = graph.bound(wy_day, tcs_snow);
                    debug3( " bounded storage is " + allowed_storage );
                    //section 2a1
                    double mtn_snow = Math.min( 2000, tcs_snow * 0.25 );
                    //section 2a2
                    double ltl_snow = tcs_snow - mtn_snow;
                    
                    // section 3a
                    double mtn_req = mtn_snow + mountain_rainspace;
                    // section 3b
                    double ltl_req = ltl_snow + little_rainspace;
                    
                    //section 4a
                    double mtn_allow = 3200.0 - mtn_req;
                    // section 4b
                    double ltl_allow = 20500 - ltl_req;
                    
                    
                    setOutput(LittleDell, ltl_allow);
                    setOutput(MountainDell, mtn_allow);
                    

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
