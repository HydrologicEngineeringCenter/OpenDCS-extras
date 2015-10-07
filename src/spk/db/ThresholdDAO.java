/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.db;

import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import java.util.ArrayList;
import opendcs.dao.DaoBase;
import opendcs.dao.DatabaseConnectionOwner;
import opendcs.dao.DbObjectCache;
import opendcs.dao.SiteDAO;
import spk.apps.support.Alarms.Threshold;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class ThresholdDAO 
        extends DaoBase 
	implements ThresholdDAI{

    protected static DbObjectCache<Threshold> cache = new DbObjectCache<Threshold>(15 * 60 * 1000L, false);
    
    
    public ThresholdDAO(DatabaseConnectionOwner db){
        super(db, "ThresholdDAO");
        
        // get properaties and such
    }

    @Override
    public ArrayList<Threshold> getThresholds(DbKey SDI, ArrayList<String> checks) {
       ArrayList<Threshold> thresholds = new ArrayList<Threshold>();
        
        String q = "select from ccp.alarm_thresholds where sitedatatypeid = " + SDI.toString();
        // and check in checks
        
        /*
            do the sql, build the thresholds
        
        */
        
        return thresholds;
        
    }

    @Override
    public ArrayList<Threshold> getThresholds(CTimeSeries cts, ArrayList<String> checks) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void saveThreashold(Threshold threshold) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
