package spk.algo;

import decodes.db.CompositeConverter;
import decodes.db.EngineeringUnit;
import decodes.tsdb.CTimeSeries;
import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.ParmRef;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import ilex.var.TimedVariable;
import decodes.db.UnitConverter;
import decodes.tsdb.BadTimeSeriesException;

//AW:IMPORTS
// Place an import statements you need here.
import java.util.TreeMap;
import spk.algo.support.StationData;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import opendcs.dai.TimeSeriesDAI;
import spk.algo.reports.ReleaseChangeNotice;
//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Combine the "3" streams together. This interface will allow Data Q/A people 
 * to manually set the primary station. All sensors must have the 3 versions
 * (LOS,GOES,IP) even if one of them is empty. It simplifies the configuration,
 *
 * @author L2EDDMAN
 * 
 */
//AW:JAVADOC_END
public class CombineStream_Adv
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double goes; //AW:TYPECODE=i
        public double ip; //AW:TYPECODE=i
        public double los; //AW:TYPECODE=i
	String _inputNames[] = { "goes", "ip", "los" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
        /**
         * Used for the filename for this station
         */
	public String location = "";
        /**
         * Array Indexed by effective data for the station data information
         */
        public TreeMap< Date, StationData> map;
        // array that holds the primary source transitions
        private TimeSeriesDAI timeSeriesDAO;
        private CTimeSeries outputTS = null; // hold the output TS for each iteration through the loop.
        /**
         *  variables for the source to use
         */
        public static final int GOES = 0;
        public static final int IP = 1;
        public static final int LOS = 2;
        public static final int OFF = 3;
        //UnitConverter uc = null;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable output = new NamedVariable("output", 0);
	String _outputNames[] = { "output" };
//AW:OUTPUTS_END

//AW:PROPERTIES        
        public String StationsDir = "/shared/stations/";
        public boolean checkPZF = false;
	String _propertyNames[] = { "StationsDir", "checkPZF" };
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
		// This code will be executed once before each group of time slices.
		// For TimeSlice algorithms this is done once before all slices.
		// For Aggregating algorithms, this is done before each aggregate
		// period.

                /*
                 * scan in station file, find all of the data source transition points
                 */
                 debug3("setting up computation");
                TimeSeriesIdentifier tsid = getParmTsId( "goes" );
                if( tsid == null )
                {
                    tsid = getParmTsId("ip");
                }

                if( tsid == null )
                {
                    tsid = getParmTsId("los");
                }

                if( tsid == null)
                {
                    throw new DbCompException("Failed to get TS ID information");
                }

                debug3("extracting location/parameter name " + ( tsid != null ) );
                debug3("site name  = " + tsid.getSiteName() );
                debug3("data type  =" + tsid.getDataType().getCode());
                debug3("getting full location name");
                location = tsid.getSiteName() + "." + tsid.getDataType().getCode(); //+"." + tsid.getPart("paramtype")+"."+tsid.getInterval();
                debug3("getting station information for: " + location);
                try{
                    map = StationData.loadData( StationsDir + "/"+ location+".station");
                } catch( Exception ex ){
                    throw new DbCompException(ex.getLocalizedMessage());
                }
                // make sure the units are set correctly
                debug3(" have map for station: " + (map != null) );
                String units_goes,units_ip,units_los = null;
                String units_o = null;
                units_goes = getInputUnitsAbbr( "goes" );
                units_ip = getInputUnitsAbbr( "ip" );
                units_los = getInputUnitsAbbr( "los" );
                //units_o = getParmRef( "output").timeSeries.getUnitsAbbr();
                //units_i1 = ref1.timeSeries.getUnitsAbbr();
                //units_i2 = ref2.timeSeries.getUnitsAbbr();
                //EngineeringUnit us = EngineeringUnit.getEngineeringUnit("ft");
                //EngineeringUnit si = EngineeringUnit.getEngineeringUnit(units_o);
                //UnitConverter uc = CompositeConverter.build(us, si);
                ParmRef outputParmRef = getParmRef("output");
                outputTS = new CTimeSeries(outputParmRef.compParm);
                
                timeSeriesDAO = tsdb.makeTimeSeriesDAO();
                Date start;
                try {
                    //this.baseTimes
                    TreeSet<Date> allInputData = this.baseTimes;
                    start = allInputData.first();
                    start.setDate(start.getDate()-1);
                    timeSeriesDAO.fillTimeSeries(outputTS, start, allInputData.last() );
                } catch (DbIoException ex) {
                    warning(ex.getLocalizedMessage());
                } catch (BadTimeSeriesException ex) {
                   warning(ex.getLocalizedMessage());
                }
                
                debug3( "time series units are:" );
                debug3( "    goes: " + units_goes );
                debug3( "      ip: " + units_ip );
                debug3( "     los: " + units_los );
                debug3( "  output:  " + units_o );
                debug3( "setting output TS units to input units" );
                setOutputUnitsAbbr( "output", units_goes ); // these should all be the same,
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
		int source = getPrimary( this._timeSliceBaseTime );
                double output = Double.NEGATIVE_INFINITY;
                boolean have_an_output = true;
                debug3( "inputs are:");
                debug3( " goes = " + goes );
                debug3( "   ip = " + ip );
                debug3( "  los = " + los );

                switch( source )
                {
                    // GOES -> IP -> LOS, shift with rotate for each one
                    // as primary
                    case GOES:
                           debug3("Primary Data Source is GOES");
                           if( !isMissing( goes ) )
                           {
                              output =  goes;
                           }
                           else if( !isMissing( ip ) )
                           {
                               output = ip;
                           }
                           else if( !isMissing( los ) )
                           {
                               output = los;
                           }
                           else
                           {
                               have_an_output = false;
                               debug1("Sensor has no value for this time slice.");
                           }
                           break;
                    case IP:
                            debug3("Primary Data Source is IP");
                            if( !isMissing( ip ) )
                            {
                                output = ip;
                            }
                            else if( !isMissing( los ) )
                            {
                                output = los;
                            }
                            else if( !isMissing( goes ) )
                            {
                                output = goes;
                            }
                            else
                            {
                                have_an_output = false;
                                debug1("Sensor has no value for this time slice.");
                            }
                            break;
                    case LOS:
                            debug3("Primary Data source is IP");
                            if( !isMissing( los ) )
                            {
                                output = los;
                            }
                            else if( !isMissing( goes ) )
                            {
                                output = goes;
                            }
                            else if( !isMissing( ip ) )
                            {
                                output = ip;
                            }
                            else
                            {
                                have_an_output = false;
                                debug1("Sensor has no value for this time slice.");
                            }
                            break;
                    case OFF:                            
                            have_an_output = false; // this period will just be missing
                            break;
                    default:
                        have_an_output = false;
                       debug3("you should never see this");
                }
                
                /*
                if( checkPZF && have_an_output ){
                    output = Math.max(output, getPZF(_timeSliceBaseTime,getInputUnitsAbbr("goes")));
                } */               
               // ParmRef outputParmRef = getParmRef("output");
                //CTimeSeries outputTS = new CTimeSeries(outputParmRef.compParm);
                
                double currentout = Double.NEGATIVE_INFINITY;
                try
                {
                    /*
                        timeSeriesDAO = tsdb.makeTimeSeriesDAO();
                        //TimedVariable currentoutput = 
                        timeSeriesDAO.fillTimeSeries(outputTS, _timeSliceBaseTime,_timeSliceBaseTime );
                    */
                        TimedVariable currentoutput = outputTS.findWithin(_timeSliceBaseTime, 100);
                        currentout = currentoutput.getDoubleValue();
                        
                }
                catch (Exception e)
                {
                        // any error means the data doesn't exist yet so we have to right it out
                } 
                debug3( "existing output= " + currentout);
                if( have_an_output && (!ReleaseChangeNotice.fequals(currentout,output,0.0001) || isMissing(currentout) ) )
                {
                    debug3( "output= " + output );
                    setOutput( this.output, output );
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

        /**
         *  figure out what the primary feed for this station should be
         * @param sliceTime
         * @return the number indicating the primary data source
         */
        public int getPrimary( Date sliceTime )

        {
            /**
             *  return the nearest previous time shift value
             *  otherwise return 0.0; assuming there is no shift
             *
             *
             */
             // searching backwards find the first date early that date_time
            if( map == null )
            {
                debug3(" there is no map");
                return GOES;
            }
            Map.Entry<Date,StationData> d = map.floorEntry(sliceTime);
            if( d!= null)
            {
                debug3("found a specific entry for Primary Feed");
                return d.getValue().primary_feed;
            }
            else
            {
                debug3( "no entry found for this sensor/date" );
            }
            return GOES;
        }
        
        

}
