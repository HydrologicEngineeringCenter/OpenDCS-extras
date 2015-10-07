/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.db;

import decodes.db.SiteName;
import decodes.sql.DbKey;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import spk.db.AlarmStateDAI;
import opendcs.dao.DaoBase;
import opendcs.dao.DatabaseConnectionOwner;
import spk.apps.support.Alarms.AlarmResponse;
import spk.apps.support.Alarms.AlarmState;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class AlarmStateDAO 
    	extends DaoBase 
	implements AlarmStateDAI{

    public AlarmStateDAO(DatabaseConnectionOwner db){
        super(db, "AlarmDAO");
        
        // get properaties and such
    }
    
    @Override
    public void saveAlarm(AlarmState alarm) throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AlarmState getAlarmBySiteName(SiteName sn) throws DbIoException, NoSuchObjectException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AlarmState getAlarmByKey(DbKey key) throws DbIoException, NoSuchObjectException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
