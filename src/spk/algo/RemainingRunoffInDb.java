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
import opendcs.dai.TimeSeriesDAI;

import spk.algo.support.DateTime;
import spk.algo.support.Dates;

//AW:IMPORTS_END

//AW:JAVADOC
/**
 * Takes the runoff from the CDEC B120 report ( or some other estimate of runoff ) and an Inflow time series and calculates the remaining runoff.
 * The algorithm will query data back to the last snowmelt forcast point, and as such will likely be "slow"; since it's only daily data shouldn't
 * be that big of a deal.
 * 
 * Expect ac-ft or cfs.
 * 
 * if cfs sum gets converted to Ac-ft, otherwise it gets used as is
 * 
 */
//AW:JAVADOC_END
public class RemainingRunoffInDb
	extends spk.algo.support.AlgBaseNew
{
//AW:INPUTS
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
        public double forecasted_runoff;    //AW:TYPECODE=i
        public double inflow;	//AW:TYPECODE=i
	String _inputNames[] = { "forecasted_runoff","inflow" };
//AW:INPUTS_END


//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	TreeMap< Date, Double > runoff_data;
        ArrayList<Date> dates;        
        ArrayList<Double> inflows;
        SimpleDateFormat df = null;
//AW:LOCALVARS_END

//AW:OUTPUTS
	public NamedVariable runoff_remaining = new NamedVariable("runoff_remaining", 0.0f );
	String _outputNames[] = { "runoff_remaining" };
//AW:OUTPUTS_END

//AW:PROPERTIES
        
	String _propertyNames[] = {  };
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
            
            

            inflows = new ArrayList<Double>();
            dates = new ArrayList<Date>();
            runoff_data = new TreeMap<Date, Double>();
            
            
            
            
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
                 if ( !isMissing( forecasted_runoff ) ){
                     runoff_data.put(_timeSliceBaseTime, forecasted_runoff);
                 }
                 
                 if ( !isMissing( inflow ) ){
                     inflows.add(inflow);
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
                 boolean TriggeredByRunoff = false;
                 PreparedStatement stmt = null;
                 ResultSet rs = null;
                 try {
                    
                    stmt = this.tsdb.getConnection().prepareStatement("select site_datatype_id from ccp.cp_comp_tasklist where record_num = ?");
                    //stmt = this.tsdb.getConnection().prepareStatement("select record_num from ccp.cp_comp_tasklist");
                    debug3("Statement Prepared");
                    //debug3( "Connection valid:" + this.tsdb.getConnection().isValid(30) );
                    if( recs.size() > 0 )
                    {
                         // Was triggered
                        
                         //ResultSet rs = this.tsdb.doQuery( );                         
                         stmt.setLong(1, recs.iterator().next().longValue() );
                         debug3("Executing tasklist query");
                         rs = stmt.executeQuery();
                         
                         debug3("Looping Through tasklist");
                         if( rs.next() ){
                             
                             debug3("Getting Record Num");
                             long sdi = rs.getLong(1);
                             
                             //String ts = this.tsdb.getTimeSeriesIdentifier(DbKey.createDbKey(sdi)).getUniqueString();
                             TimeSeriesDAI tdai = tsdb.makeTimeSeriesDAO();
                             String ts = tdai.getTimeSeriesIdentifier(DbKey.createDbKey(sdi)).getUniqueString();
                             
                             
                             
                             debug3( "Time Series name = " + ts );                             
                             if( ts.equalsIgnoreCase( getParmRef("forecasted_runoff").timeSeries.getTimeSeriesIdentifier().getUniqueString() ) ){
                                 // Was Triggered by a runoff entry
                                 TriggeredByRunoff = true;                                 
                            }
                        } 
                    }
                     
                 
                } catch (SQLException ex) {
                    debug3(ex.getMessage());
                    Logger.getLogger(RemainingRunoffInDb.class.getName()).log(Level.SEVERE, null, ex);               
                } catch (NoSuchObjectException ex) {
                    debug3(ex.getMessage());
                    Logger.getLogger(RemainingRunoffInDb.class.getName()).log(Level.SEVERE, null, ex);
                } catch (DbIoException ex) {
                    Logger.getLogger(RemainingRunoffInDb.class.getName()).log(Level.SEVERE, null, ex);
                } finally{
                     if( stmt != null ){
                         try {
                             stmt.close();
                         } catch (SQLException ex) {
                             Logger.getLogger(RemainingRunoffInDb.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
                     
                     if( rs != null ){
                         try {
                             rs.close();
                         } catch (SQLException ex) {
                             Logger.getLogger(RemainingRunoffInDb.class.getName()).log(Level.SEVERE, null, ex);
                         }
                     }
                 }
                 
                 
                 
                 
                 int last_forecast_idx = 0; // used for sum
                 Date start,end;
                 if( TriggeredByRunoff ){
                     // get a different set of data, either end of current snowmelt season or now, whichever is less
                     
                     // We have a list of the current runoffs. so get data between the first one
                     // and the end of the runoff season for the last one. 
                     start = runoff_data.firstKey();
                     end = runoff_data.lastKey();
                     
                     cal.setTime(end);
                     cal.set( Calendar.MONTH, Calendar.SEPTEMBER);
                     cal.set( Calendar.DAY_OF_MONTH, 2);
                     end = cal.getTime();
                    
                     /*
                      * here we rebuild the inflow/date time series
                      */
                     getFlowData(start,end);
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
                 
                 getRunoffData( start, end ); // we need this range of data no matter what
                 
                 
                 
                 debug3( "getting flows between " + start + " and " + end );
                 Date d;
                 Date last_runoff = new Date(0); 
                 
                 double flow;
                 double sum = 0;
                 int month;
                 cal.setTimeZone(aggTZ);
                 for( int i = 0; i < inflows.size(); i++ ){
                     d = dates.get(i);
                     flow = inflows.get(i);
                     
                     int wy_day = DateTime.to_wy_julian(d);
                     Dates days = new Dates(d);                     
                     
                     if( wy_day <= days.February01 || wy_day > days.August01){
                         setOutput(runoff_remaining, 0, d);
                     } else{
                         Date runoff_date = getRunoffDate(d);
                         double runoff = getRunoff(runoff_date);
                         
                         double flows[] = this.getPrevValues("inflow", runoff_date, d);
                         sum=0;
                         for( double f: flows){
                             sum+= f;
                         }                                                              
                         ParmRef ref = this.getParmRef("inflow");
                         if( ref.timeSeries.getUnitsAbbr().equalsIgnoreCase( "cfs" ) ){
                            sum=sum*1.9835; // otherwise it's already in acre-ft
                         }// otherwise use sum as is
                         double remaining = runoff-(sum);
                         setOutput( runoff_remaining, remaining, d);                                                  
                     }

                 } 
                 
                 
                 
            
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

        public double getRunoff( Date d )
        {
            // go through the list and get the nearest runoff to this date
            return runoff_data.get(d);
        }

        public Date getRunoffDate( Date cur_date)
        {
            return runoff_data.floorKey(cur_date);
        }
        
        public void getRunoffData( Date start, Date end){
            ParmRef pr = getParmRef("forecasted_runoff");
            
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
                                
                                this.runoff_data.put( tv.getTime(), tv.getDoubleValue() );
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
        
        public void getFlowData( Date start, Date end ){
            // if this is called we're rebuilding everything
            inflows.clear();
            dates.clear();
            ParmRef pr = getParmRef("inflow");
            
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
                                this.inflows.add(tv.getDoubleValue());
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
