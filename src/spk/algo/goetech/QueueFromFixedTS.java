package spk.algo.goetech;



import spk.algo.*;
import java.util.Date;

import ilex.var.NamedVariableList;
import ilex.var.NamedVariable;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbIoException;
import decodes.tsdb.VarFlags;
import decodes.tsdb.algo.AWAlgoType;
import java.util.logging.Level;
import java.util.logging.Logger;
import decodes.cwms.CwmsFlags;
import decodes.db.DataType;
import decodes.sql.DbKey;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.NoSuchObjectException;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.TsGroup;
import ilex.var.TimedVariable;
import java.util.ArrayList;
import java.util.HashSet;
import opendcs.dai.CompDependsDAI;
import opendcs.dai.ComputationDAI;
import opendcs.dai.TimeSeriesDAI;
import opendcs.dai.TsGroupDAI;

//AW:IMPORTS
// Place an import statements you need here.
//AW:IMPORTS_END

//AW:JAVADOC
/**
* This will take an input find all of the group comps that have it as a dependant parameters
* and insert values for those specific comps into the tasklist
 */
//AW:JAVADOC_END
public class QueueFromFixedTS
	extends decodes.tsdb.algo.AW_AlgorithmBase
{
//AW:INPUTS
        public double input;   //AW:TYPECODE=i
        
        // input values, declare a variable, and add the string of the variable name to the _inputNames array
	String _inputNames[] = { "input" };
//AW:INPUTS_END

//AW:LOCALVARS
	// Enter any local class variables needed by the algorithm.
	 private ArrayList<Date> dates;        
//AW:LOCALVARS_END

//AW:OUTPUTS
	
	String _outputNames[] = { };
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
                dates = new ArrayList<Date>();
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
            dates.add(_timeSliceBaseTime);
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
            ComputationDAI compdao = tsdb.makeComputationDAO();            
            ArrayList<TimeSeriesIdentifier> tsids = new ArrayList<TimeSeriesIdentifier>();
            TimeSeriesDAI tsdai = tsdb.makeTimeSeriesDAO();
            TimeSeriesIdentifier itsid = getParmTsId("input");
            CTimeSeries icts = new CTimeSeries(itsid.getKey(),itsid.getInterval(),itsid.getTableSelector());
            try {
                tsdai.fillTimeSeriesMetadata(icts);
                int fillDependentCompIds = tsdb.fillDependentCompIds(icts, comp.getAppId() );
            } catch (DbIoException ex) {
                Logger.getLogger(QueueFromFixedTS.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadTimeSeriesException ex) {
                Logger.getLogger(QueueFromFixedTS.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            HashSet<DbKey> dependentCompIds = icts.getDependentCompIds();
            for( DbKey key: dependentCompIds){
                if( !key.equals( this.comp.getKey() )){ // we don't want an endless loop
                    DbComputation mycomp;
                    try {
                        mycomp = compdao.getComputationById(key);
                        if( mycomp.hasGroupInput() && mycomp.isEnabled() ){
                            for( DbCompParm parm: mycomp.getParmList() ){
                                debug3(parm.getDataTypeId().toString());
                                if( parm.isInput() && parm.getSiteDataTypeId().isNull() ){                                    
                                    DataType dt = parm.getDataType();
                                    TsGroup group = mycomp.getGroup();
                                    //group.addIntervalCode(parm.getInterval());
                                    group.addVersion( parm.getVersion() );
                                    group.addDataTypeId(dt.getKey());
                                    group.setTransient();                                    
                                    group.clearExpandedList();
                                    //group.refilter(tsdb);
                                    tsids = tsdb.expandTsGroup(group);                                    
                                    break; // we got what we need
                                }

                            }
                        }
                        
                        CTimeSeries cts = null;
                        for( TimeSeriesIdentifier tsid: tsids ){
                            debug3("inserting data for " + tsid.getUniqueString() );
                            cts = new CTimeSeries(tsid.getKey(), tsid.getInterval(), tsid.getTableSelector() );                                 
                            tsdai.fillTimeSeries(cts, dates);
                            // a little odd, but we are just trying to requeue data
                            TimedVariable tv = null;
                            for( int i = 0; i < cts.size(); i++ ){
                                tv = cts.sampleAt(i);
                                tv.setFlags( tv.getFlags() | VarFlags.TO_WRITE );
                            }
                            tsdai.saveTimeSeries(cts);
                            debug3("saved data to database");
                            
                        }
                        
                        
                    } catch (DbIoException ex) {
                        warning("database connection failed");
                    } catch (NoSuchObjectException ex) {
                        debug3("no comp for key " + key);
                    } catch (BadTimeSeriesException ex) {
                        debug3("could read timeseries data");
                    }
                    
                    
                }
             
            }
                       tsdai.close();
                compdao.close();
                    
//AW:AFTER_TIMESLICES_END
;
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
