/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.db.test;

import decodes.cwms.CwmsTsId;
import decodes.db.DataType;
import decodes.sql.DbKey;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import decodes.tsdb.TimeSeriesDb;
import decodes.tsdb.TimeSeriesIdentifier;
import ilex.var.TimedVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import opendcs.dai.TimeSeriesDAI;
import opendcs.dao.DbObjectCache;

/**
 * TimeSeriesDAO for use in unit tests.
 *
 * @author L2EDDMAN
 */
public class TestDbTimeSeriesDAO implements TimeSeriesDAI {

    Fixtures fixtures = null;
    TimeSeriesDb db = null;
    public static final DbKey DATA_STAGE_GOES = DbKey.createDbKey(0);
    public static final DbKey DATA_STAGE_LOS = DbKey.createDbKey(1);
    public static final DbKey DATA_STAGE_IP = DbKey.createDbKey(2);
    public static final DbKey DATA_STAGE_COMB = DbKey.createDbKey(3);
    public static final DbKey DATA_STAGE_COMB_missing = DbKey.createDbKey(4);

    public static final DbKey DATA_FLOW = DbKey.createDbKey(5);

    private HashMap<DbKey, CTimeSeries> timeseries;

    public TestDbTimeSeriesDAO(TimeSeriesDb db) throws Exception {
        this.db = db;
        fixtures = Fixtures.getFixtures(db);

        //timeseries = new HashMap<>();
    }

    @Override
    public TimeSeriesIdentifier getTimeSeriesIdentifier(String tsname) throws DbIoException, NoSuchObjectException {
        CwmsTsId tsi = new CwmsTsId();

        String tsname_parts[] = tsname.split("\\.");
        tsi.setDisplayName(tsname);
        tsi.setUniqueString(tsname);
        tsi.setKey(fixtures.getTimeSeriesKey(tsname));
        tsi.setSiteName(tsname_parts[0]);
        tsi.setDataType(new DataType("cwms", tsname_parts[1]));

        return tsi;
    }

    @Override
    public TimeSeriesIdentifier getTimeSeriesIdentifier(DbKey key) throws DbIoException, NoSuchObjectException {
        CTimeSeries cts = fixtures.getDC().getTimeSeriesByUniqueSdi(key);
        if (cts != null) {
            return cts.getTimeSeriesIdentifier();
        } else {
            throw new NoSuchObjectException("No time series for DbKey: " + key.toString());
        }
    }

    @Override
    public void fillTimeSeriesMetadata(CTimeSeries ts) throws DbIoException, BadTimeSeriesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int fillTimeSeries(CTimeSeries ts, Date from, Date until) throws DbIoException, BadTimeSeriesException {
        return fillTimeSeries(ts,from,until,true,true,true);
    }

    @Override
    public int fillTimeSeries(CTimeSeries ts, Date from, Date until, boolean include_lower, boolean include_upper, boolean overwriteExisting) throws DbIoException, BadTimeSeriesException {
    CTimeSeries from_dc = fixtures.getDC().getTimeSeriesByUniqueSdi(ts.getSDI());
        if (from_dc == null) {
            return 0;
        }
        
        long _from = from.getTime();
        long _until = until.getTime();
        
        ts.setTimeSeriesIdentifier(from_dc.getTimeSeriesIdentifier());
        for (int i = 0; i < from_dc.size(); i++) {
            TimedVariable tv = from_dc.sampleAt(i);
            long dt = tv.getTime().getTime();
            boolean include = false;
            if( ((include_lower && dt >= _from ) || dt > _from) && ((include_upper && dt <= _until) || dt < _until )  ){
                 if(overwriteExisting){
                     ts.addSample(tv);
                 } else {
                     TimedVariable orig = ts.findWithin(tv.getTime(), 30);
                     if( orig == null ){
                         ts.addSample(tv);
                     }
                 }
            }

        }
        return from_dc.size();    
    }

    @Override
    public int fillTimeSeries(CTimeSeries ts, Collection<Date> queryTimes) throws DbIoException, BadTimeSeriesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TimedVariable getPreviousValue(CTimeSeries ts, Date refTime) throws DbIoException, BadTimeSeriesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public TimedVariable getNextValue(CTimeSeries ts, Date refTime) throws DbIoException, BadTimeSeriesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveTimeSeries(CTimeSeries ts) throws DbIoException, BadTimeSeriesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteTimeSeriesRange(CTimeSeries ts, Date from, Date until) throws DbIoException, BadTimeSeriesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteTimeSeries(TimeSeriesIdentifier tsid) throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CTimeSeries makeTimeSeries(TimeSeriesIdentifier tsid) throws DbIoException, NoSuchObjectException {
        CTimeSeries cts = new CTimeSeries(tsid.getKey(), tsid.getInterval(), tsid.getTableSelector());
        return cts;
    }

    @Override
    public ArrayList<TimeSeriesIdentifier> listTimeSeries() throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reloadTsIdCache() throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DbObjectCache<TimeSeriesIdentifier> getCache() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DbKey createTimeSeries(TimeSeriesIdentifier tsid) throws DbIoException, NoSuchObjectException, BadTimeSeriesException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * this one stays open
     */
    @Override
    public void close() {

    }
    /*
     @Override
     public ArrayList<TimeSeriesIdentifier> listTimeSeries(boolean forceRefresh) throws DbIoException {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }

     @Override
     public void setRoundSec(int roundSec) {
     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
     */

    @Override
    public ArrayList<TimeSeriesIdentifier> listTimeSeries(boolean forceRefresh) throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAppModule(String module) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
