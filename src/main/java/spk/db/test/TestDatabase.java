/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.db.test;

import decodes.sql.DbKey;
import decodes.tsdb.BadConnectException;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.ConstraintException;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import decodes.tsdb.TimeSeriesDb;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.TsGroup;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import opendcs.dai.IntervalDAI;
import opendcs.dai.ScheduleEntryDAI;
import opendcs.dai.TimeSeriesDAI;

/**
 * A Simple Database Implementation for use in unit tests (and maybe integration tests)
 * @author Michael Neilson
 */
public class TestDatabase extends TimeSeriesDb{

    TimeSeriesDAI tsdai = null;
    
    
    public TestDatabase(){
        TestDatabase.sdiIsUnique = true;
    }
    
    @Override
    public DbKey connect(String appName, Properties credentials) throws BadConnectException {
        // not needed 
        return DbKey.createDbKey(0);
    }

    @Override
    public void setParmSDI(DbCompParm parm, DbKey siteId, String dtcode) throws DbIoException, NoSuchObjectException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DataCollection getNewDataSince(DbKey applicationId, Date sinceTime) throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void validateParm(DbKey siteId, String dtcode, String interval, String tabSel, int modelId) throws ConstraintException, DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int findModelId(int modelRunId) throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int findMaxModelRunId(int modelId) throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String flags2LimitCodes(int flags) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String flags2RevisionCodes(int flags) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String[] getTsIdParts() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TimeSeriesIdentifier makeEmptyTsId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean transformUniqueString(TimeSeriesIdentifier tsidRet, DbCompParm parm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TimeSeriesDAI makeTimeSeriesDAO() {
        try {
            if( tsdai == null ){
                tsdai = new TestDbTimeSeriesDAO();
            }
            return tsdai;
        } catch (Exception ex) {
            Logger.getLogger(TestDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public TimeSeriesIdentifier expandSDI(DbCompParm parm) throws DbIoException, NoSuchObjectException {
        TimeSeriesDAI tsdai = this.makeTimeSeriesDAO();
        TimeSeriesIdentifier tsi = tsdai.getTimeSeriesIdentifier(parm.getSiteDataTypeId());
        if( tsi != null ){
            parm.setSite(tsi.getSite());
            parm.setDataType(tsi.getDataType());
        } 
        
        return tsi;
        
    }

    @Override
    public IntervalDAI makeIntervalDAO() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ScheduleEntryDAI makeScheduleEntryDAO() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<TimeSeriesIdentifier> expandTsGroup(TsGroup tsGroup) throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TimeSeriesIdentifier transformTsidByCompParm(TimeSeriesIdentifier tsid, DbCompParm parm, boolean createTS, boolean fillInParm, String timeSeriesDisplayName) throws DbIoException, NoSuchObjectException, BadTimeSeriesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
