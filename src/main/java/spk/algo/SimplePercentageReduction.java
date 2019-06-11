package spk.algo;

import decodes.sql.DbKey;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import decodes.tsdb.ParmRef;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import ilex.var.NoConversionException;
import ilex.var.TimedVariable;

//AW:IMPORTS
// Place an import statements you need here.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.lang.Math;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import opendcs.dai.TimeSeriesDAI;

import spk.algo.support.DateTime;
import spk.algo.support.Dates;

//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Calculates the reduces volume from paragraph two of the Echo/Rockport water control manual
 * 
 * forecasted_diversion should be in ac-ft as is expected to be an irregular time series
 * marker value is ignored except to use the time to calculate number of days to the configure to_date property.
 * 
 * if cfs sum gets converted to Ac-ft, otherwise it gets used as is
 * 
 * This version pulls data from database instead of a text file.
 */
//AW:JAVADOC_END
public class SimplePercentageReduction
	extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double forecasted_diversion;    //AW:TYPECODE=i
        public double marker;	//AW:TYPECODE=i
	String _inputNames[] = { "forecasted_diversion","marker" };
//AW:INPUTS_END


//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	TreeMap< Date, Double > diversion_data;
        ArrayList<Date> dates;        
        ArrayList<Double> marker_values;
        final SimpleDateFormat df = new SimpleDateFormat("ddMMMyyyy");
        final SimpleDateFormat df2 = new SimpleDateFormat("yyyy");
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable reduced = new NamedVariable("reduced", 0.0f );
	String _outputNames[] = { "reduced" };
//AW:OUTPUTS_END

//AW:PROPERTIES
        public String to_date = "30Jun";
        public double percent_per_day = 0.25;
	String _propertyNames[] = { "to_date", "percent_per_day" };
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
            
            

            marker_values = new ArrayList<Double>();
            dates = new ArrayList<Date>();
            diversion_data = new TreeMap<Date, Double>();
            
            
            
            
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
                /*
                 * Probably do nothing
                 */
                 if ( !isMissing( forecasted_diversion ) ){
                     diversion_data.put(_timeSliceBaseTime, forecasted_diversion);
                 }
                 
                 if ( !isMissing( marker ) ){
                     marker_values.add(marker);
                     dates.add(_timeSliceBaseTime);
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
            
                /*
                 * we have our base line data
                 * now figure out what to do
                 *  
                 */
                 
                 GregorianCalendar cal = new GregorianCalendar();
                 

                 HashSet<Integer> recs = this.comp.getTriggeringRecNums();
                 boolean TriggeredByDiversion = false;
                 PreparedStatement stmt = null;
                 ResultSet rs = null;
                 try {                    
                    stmt = this.tsdb.getConnection().prepareStatement("select site_datatype_id from ccp.cp_comp_tasklist where record_num = ?");                    
                    debug3("Statement Prepared");                    
                    if( recs.size() > 0 )
                    {
                         // Was triggered, now figure what triggered the comp                                               
                         stmt.setLong(1, recs.iterator().next().longValue() );
                         debug3("Executing tasklist query");
                         rs = stmt.executeQuery();                                                  
                         if( rs.next() ){                             
                             debug3("Getting Record Num");
                             long sdi = rs.getLong(1);                                                          
                             String ts ="TDAI creation/get failed";
                             TimeSeriesDAI tdai = null;
                             try{
                                tdai = tsdb.makeTimeSeriesDAO();
                                ts = tdai.getTimeSeriesIdentifier(DbKey.createDbKey(sdi)).getUniqueString();                             
                             }finally{
                                 if( tdai != null ){
                                    tdai.close();
                                 }
                             }                             
                             debug3( "Time Series name = " + ts );                             
                             if( ts.equalsIgnoreCase( getParmRef("forecasted_diversion").timeSeries.getTimeSeriesIdentifier().getUniqueString() ) ){
                                 // Was Triggered by a runoff entry
                                 TriggeredByDiversion = true;                                 
                            }
                        } 
                    }
                     
                 
                } catch (SQLException ex) {
                    debug3(ex.getMessage());
                    Logger.getLogger(SimplePercentageReduction.class.getName()).log(Level.SEVERE, null, ex);               
                } catch (NoSuchObjectException ex) {
                    debug3(ex.getMessage());
                    Logger.getLogger(SimplePercentageReduction.class.getName()).log(Level.SEVERE, null, ex);
                } catch (DbIoException ex) {
                    Logger.getLogger(SimplePercentageReduction.class.getName()).log(Level.SEVERE, null, ex);
                } finally{
                     if( stmt != null ){
                         try {
                             stmt.close();
                         } catch (SQLException ex) {
                             Logger.getLogger(SimplePercentageReduction.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
                     
                     if( rs != null ){
                         try {
                             rs.close();
                         } catch (SQLException ex) {
                             Logger.getLogger(SimplePercentageReduction.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
                 }
                 
                 
                 
                 
                 int last_forecast_idx = 0; // used for sum
                 Date start,end;
                 if( TriggeredByDiversion ){
                     // get a different set of data, either end of current snowmelt season or now, whichever is less
                     
                     // We have a list of the current runoffs. so get data between the first one
                     // and the end of the runoff season for the last one. 
                     start = diversion_data.firstKey();
                     end = diversion_data.lastKey();
                     
                     cal.setTime(end);                     
                     cal.set( Calendar.MONTH, Calendar.SEPTEMBER);
                     cal.set( Calendar.DAY_OF_MONTH, 2);
                     end = cal.getTime();
                    
                     /*
                      * here we rebuild the inflow/date time series
                      */
                     getMarkerData(start,end);
                 } else{
                 

                    Date first_inflow = dates.get(0);
                    Date last_inflow = dates.get( dates.size() - 1 );

                    /* we should get all of the Forecast Runoff values
                     * for the water years between these dates
                     */


                    cal.setTime(first_inflow);
                    cal.set( Calendar.MONTH, Calendar.JANUARY);
                    cal.set( Calendar.DAY_OF_MONTH, 30);                 
                    start = cal.getTime();

                    cal.setTime(last_inflow);
                    cal.set( Calendar.MONTH, Calendar.SEPTEMBER);
                    cal.set( Calendar.DAY_OF_MONTH, 2);
                    end = cal.getTime();
                 }
                 
                 getDiversionData( start, end ); // we need this range of data no matter what
                 
                 
                 
                 debug3( "getting flows between " + start + " and " + end );
                 Date d;
                 Date last_runoff = new Date(0); 
                 
                 double flow;
                 int count = 0;
                 int month;
                 Date end_day = null;
                 cal.setTimeZone(aggTZ);
                 GregorianCalendar cal2 = new GregorianCalendar(this.aggTZ);
                 
                 
                 for( int i = 0; i < marker_values.size(); i++ ){
                     try {
                         d = dates.get(i);
                         flow = marker_values.get(i);
                         String newdate = to_date + df2.format(d);
                         end_day = df.parse( newdate );
                                                  
                         int wy_day = DateTime.to_wy_julian(d,aggTZ);
                         int wy_end_day = DateTime.to_wy_julian(end_day,aggTZ);
                         Dates days = new Dates(d);
                         
                         if( wy_day < days.February01 || wy_day > wy_end_day){
                             setOutput(reduced, 0, d);
                         } else{
                             try{
                                 Date runoff_date = getRunoffDate(d);
                                 double runoff = getDiversion(runoff_date);
                                 cal2.setTime(d);
                                 /*
                                 the following logic assumes we never cross a year
                                 */
                                 int _start = cal2.get(Calendar.DAY_OF_YEAR);
                                 cal2.setTime(end_day);
                                 int _end =cal2.get(Calendar.DAY_OF_YEAR);
                                 count = (_end-_start)+1; // count is inclusive of the day we are on
                                 
                                 double remaining = Math.max(0, runoff*(count*percent_per_day/100.0)); // negative remaining runoff is impossible
                                 setOutput( reduced, remaining, d);
                             }
                             catch( Exception err){
                                 debug3(err.getMessage() );
                             }
                         }
                     }
//AW:AFTER_TIMESLICES_END
                     catch (ParseException ex) {
                         debug3("to_date specification is bad, please use the ddbbb format (e.g. 3Jun or 03Jun, etc)");
                     }

                 } 
                 
                 
                 
            
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

        public double getDiversion( Date d )
        {
            // go through the list and get the nearest runoff to this date
            return diversion_data.get(d);
        }

        public Date getRunoffDate( Date cur_date)
        {
            return diversion_data.floorKey(cur_date);
        }
        
        public void getDiversionData( Date start, Date end){
            ParmRef pr = getParmRef("forecasted_diversion");
            
            // ParmRef time series aren't changeable. so we have to create a fresh time series to fill
            decodes.tsdb.CTimeSeries ts = new decodes.tsdb.CTimeSeries(pr.compParm);
            try{
                    ts.setUnitsAbbr(pr.timeSeries.getUnitsAbbr());
                    debug3("Querying database");
            
                    // tsdb is the generic interface to the database backend of the CCP                    
                    if( tsdb.fillTimeSeries( ts, start,end) == 0 )
                    {
                        warning( "Could not access timeseries, assuming it doesn't exist and we are starting at 0 (returning empty array)");                        
                    }
                    debug3( "database returned " + ts.size() + " entries");

                    for( int i =0; i < ts.size(); i++ )
                    {
                    //TimedVariable tv = pr.timeSeries.findWithin(previous_fcp_date, roundSec);
                        TimedVariable tv = ts.sampleAt(i);

                        try{
                            if( tv != null )
                            {
                                debug3( " Date" + tv.timeString() + "  value " + tv.getDoubleValue());
                                //this.inflows.add(tv.getDoubleValue());
                                //this.dates.add(tv.getDateValue());
                                
                                this.diversion_data.put( tv.getTime(), tv.getDoubleValue() );
                                //data[i] = tv.getDoubleValue();

                            }

                        }
                        catch( NoConversionException e)
                        {
                            warning( "could not convert data from provided  time series");

                        }
                    }
                }
                catch( DbIoException e)
                {
                    warning( "Could not access timeseries, returning empty array: " + e.toString() );

                }
                catch( BadTimeSeriesException e )
                {
                    warning( "Could not access timeseries, returning empty array: " + e.toString() );

                }

        }
        
        public void getMarkerData( Date start, Date end ){
            // if this is called we're rebuilding everything
            marker_values.clear();
            dates.clear();
            ParmRef pr = getParmRef("marker");
            
            // ParmRef time series aren't changeable. so we have to create a fresh time series to fill
            decodes.tsdb.CTimeSeries ts = new decodes.tsdb.CTimeSeries(pr.compParm);
            try{
                    ts.setUnitsAbbr(pr.timeSeries.getUnitsAbbr());
                    debug3("Querying database");
            
                    // tsdb is the generic interface to the database backend of the CCP                    
                    if( tsdb.fillTimeSeries( ts, start,end) == 0 )
                    {
                        warning( "Could not access timeseries, assuming it doesn't exist and we are starting at 0 (returning empty array)");                        
                    }
                    debug3( "database returned " + ts.size() + " entries");

                    for( int i =0; i < ts.size(); i++ )
                    {
                    //TimedVariable tv = pr.timeSeries.findWithin(previous_fcp_date, roundSec);
                        TimedVariable tv = ts.sampleAt(i);

                        try{
                            if( tv != null )
                            {
                                debug3( " Date" + tv.timeString() + "  value " + tv.getDoubleValue());
                                this.marker_values.add(tv.getDoubleValue());
                                this.dates.add(tv.getTime());
                                //this.runoff_data.put( tv.getDateValue(), tv.getDoubleValue() );
                                //data[i] = tv.getDoubleValue();

                            }

                        }
                        catch( NoConversionException e)
                        {
                            warning( "could not convert data from provided  time series");

                        }
                    }
                }
                catch( DbIoException e)
                {
                    warning( "Could not access timeseries, returning empty array");

                }
                catch( BadTimeSeriesException e )
                {
                    warning( "Could not access timeseries, returning empty array");

                }
            
        }
}
