/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.db;

import decodes.db.DatabaseException;
import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DbIoException;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    PreparedStatement stmtGetThresholds_no_checks;
    PreparedStatement stmtGetThresholds;
    
    public ThresholdDAO(DatabaseConnectionOwner db){
        super(db, "ThresholdDAO");
        
        try {
            stmtGetThresholds_no_checks = db.getConnection().prepareStatement("select * from ccp.alarm_thresholds where sitedatatypeid = ?" );
            stmtGetThresholds = db.getConnection().prepareStatement("select * from ccp.alarm_thresholds where sitetypetypeid = ? and check_type in ?");
            // get properaties and such
        } catch (SQLException ex) {
            Logger.getLogger(ThresholdDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public ArrayList<Threshold> getThresholds(DbKey SDI, ArrayList<String> checks) {
       ArrayList<Threshold> thresholds = new ArrayList<Threshold>();
        
        String q = "select from ccp.alarm_thresholds where sitedatatypeid = " + SDI.toString();
        
        try {
            ResultSet rs;
            stmtGetThresholds_no_checks.setLong(1, SDI.getValue());
            stmtGetThresholds.setLong(1, SDI.getValue());
            // and check in checks
            if( checks.size() > 0 ){
                stmtGetThresholds.setArray(2, (Array) checks);                
                rs = stmtGetThresholds.executeQuery();
            } else{
                rs = stmtGetThresholds_no_checks.executeQuery();
            }
            
            while( rs.next() ){
                try {
                    thresholds.add( new Threshold(rs) );
                } catch (DatabaseException ex) {
                    Logger.getLogger(ThresholdDAO.class.getName()).log(Level.SEVERE, null, ex);
                    warning( "Failed to process a threshold");
                }
            }
            
            
            
            
            
            return thresholds;
        } catch (SQLException ex) {
            Logger.getLogger(ThresholdDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        /*
            do the sql, build the thresholds
        
        */
        return null;
        
        
        
    }

    @Override
    public ArrayList<Threshold> getThresholds(CTimeSeries cts, ArrayList<String> checks) {
        return this.getThresholds(cts.getSDI() , checks);        
    }

    @Override
    public void saveThreashold(Threshold threshold) {
        // check for existing threshold id
        // check for existing check of same parameters ( SDI, check, a,b,duration )
        
        
    }

    
}
