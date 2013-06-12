package spk.algo;

import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;


//AW:IMPORTS
// Place an import statements you need here.
import java.util.TreeMap;
import spk.algo.support.StationData;
import java.util.Map;
import java.util.Map.Entry;
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

        /**
         *  variables for the source to use
         */
        public static final int GOES = 0;
        public static final int IP = 1;
        public static final int LOS = 2;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable output = new NamedVariable("output", 0);
	String _outputNames[] = { "output" };
//AW:OUTPUTS_END

//AW:PROPERTIES        
        public String StationsDir = "/shared/stations/";
	String _propertyNames[] = { "StationsDir" };
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
                map = StationData.loadData( StationsDir + "/"+ location+".station");
                // make sure the units are set correctly
                debug3(" have map for station: " + (map != null) );
                String units_goes,units_ip,units_los = null;
                String units_o = null;
                units_goes = getInputUnitsAbbr( "goes" );
                units_ip = getInputUnitsAbbr( "ip" );
                units_los = getInputUnitsAbbr( "los" );
                units_o = getParmRef( "output").timeSeries.getUnitsAbbr();
                //units_i1 = ref1.timeSeries.getUnitsAbbr();
                //units_i2 = ref2.timeSeries.getUnitsAbbr();

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
                    default:
                        have_an_output = false;
                       debug3("you should never see this");
                }

                if( have_an_output )
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
