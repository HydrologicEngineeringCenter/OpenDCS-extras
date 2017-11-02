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
import spk.algo.support.FBOCurves;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 *
 *Calculate the SAFCA version of the Folsom Flood Control Diagram.
 * @author L2EDDMAN
 */
//AW:JAVADOC_END

public class Folsom_FBO
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
	public double OneDay;	//AW:TYPECODE=i
        public double TwoDay;   //AW:TYPECODE=i
        public double ThreeDay;   //AW:TYPECODE=i
        public double FiveDay;   //AW:TYPECODE=i
	String _inputNames[] = { "OneDay", "TwoDay", "ThreeDay", "FiveDay" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        WaterControlDiagram graph = null;
        
//AW:LOCALVARS_END

//AW:OUTPUTS	
        public NamedVariable AllowedStorage1Day = new NamedVariable("AllowedStorage1Day", 0);
        public NamedVariable AllowedStorage2Day = new NamedVariable("AllowedStorage2Day", 0);
        public NamedVariable AllowedStorage3Day = new NamedVariable("AllowedStorage3Day", 0);
        public NamedVariable AllowedStorage5Day = new NamedVariable("AllowedStorage5Day", 0);
        public NamedVariable AllowedStorageBase = new NamedVariable("AllowedStorageBase", 0);
        public NamedVariable AllowedStorageFBO = new NamedVariable("AllowedStorageFBO",0 );
        public NamedVariable AllowedStorage = new NamedVariable("AllowedStorage", 0);               
	String _outputNames[] = { "AllowedStorage", "AllowedStorageBase","AllowedStorage1Day","AllowedStorage2Day","AllowedStorage3Day","AllowedStorage5Day", "AllowedStorageFBO" };
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
                double allowed_storage_base = 0.0;
                double allowed_storage = 0.0;
                double fbo_val = 0.0;
		// Enter code to be executed at each time-slice.
                int wy_day = DateTime.to_wy_julian(_timeSliceBaseTime);
                Dates dates = new Dates(_timeSliceBaseTime);
                if( isMissing( OneDay ) || isMissing(TwoDay) || isMissing(ThreeDay) || isMissing(FiveDay) )
                {
                    info( "Current Implementation does not handle missing values, moving to next timeslice" );
                    return;
                }
                try{
                    // get the initial top con value
                    debug2( "Getting Base TCS value" );
                    allowed_storage_base = graph.get_allowed_storage(wy_day-1,0);

                    /* 
                        i = 0, 24 hour
                        i = 1, 48 hour
                        i = 2, 72 hour
                        i = 4, 120 hour
                    */
                    
                    String names[] = new String[] { "1Day","2Day","3Day","5Day"};                
                    double topcons[] = new double[4];
                    FBOCurves fbo = this.graph.get_fbo_curves();
                    topcons[0] = fbo.get_topcon(24, OneDay);
                    topcons[1] = fbo.get_topcon(48, TwoDay);
                    topcons[2] = fbo.get_topcon(72, ThreeDay);
                    topcons[3] = fbo.get_topcon(120, FiveDay);
                    double max = Double.POSITIVE_INFINITY;
                    int idx = 0;
                    /**
                     * we want to choose the lowest of all of the values
                     */
                    for( int i = 0; i < topcons.length; i++ ){
                        if( topcons[i] < max ){
                            max = topcons[i];
                            idx = i;
                        }
                    }
                    info("Selected " + names[idx] + " value for Top Con");
                    fbo_val = max;
                    if( wy_day >= dates.November19 && wy_day < dates.March01 ){
                        allowed_storage = fbo_val;
                    } else{
                        allowed_storage = allowed_storage_base;
                    }
                    setOutput(AllowedStorage, allowed_storage);
                    if( StorAll == true )
                    { // we may not always want to record these values                     
                        setOutput(AllowedStorageBase, allowed_storage_base);
                        setOutput(AllowedStorageFBO, fbo_val);
                        setOutput(AllowedStorage1Day, topcons[0]);
                        setOutput(AllowedStorage2Day, topcons[1]);
                        setOutput(AllowedStorage3Day, topcons[2]);
                        setOutput(AllowedStorage5Day, topcons[3]);
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
