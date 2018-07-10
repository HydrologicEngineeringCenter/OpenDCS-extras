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

import spk.apps.support.Alarms.AlarmResponse;
import spk.apps.support.Alarms.AlarmState;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public interface AlarmStateDAI {
    
    public void saveAlarm( AlarmState alarm ) throws DbIoException;
    public AlarmState getAlarmBySiteName( SiteName sn ) throws DbIoException,NoSuchObjectException;
    public AlarmState getAlarmByKey( DbKey key ) throws DbIoException,NoSuchObjectException;
    
   
    
}
