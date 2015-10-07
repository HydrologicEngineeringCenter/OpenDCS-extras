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
import spk.apps.support.Alarms.Threshold;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public interface ThresholdDAI {
    public ArrayList<Threshold> getThresholds( DbKey SDI, ArrayList<String> checks);
    public ArrayList<Threshold> getThresholds( CTimeSeries cts, ArrayList<String> checks);
    
    public void saveThreashold( Threshold threshold );
}
