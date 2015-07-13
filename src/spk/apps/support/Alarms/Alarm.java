/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.support.Alarms;

import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.TimeSeriesDb;
import decodes.tsdb.TimeSeriesHelper;
import ilex.var.NoConversionException;
import ilex.var.TimedVariable;
import java.util.Date;

/**
 * This class holds the components of an Alarm
 *  The timeseries for which it is for.
 *  The units of the data.
 *  The background color that will be displayed (if displayed on a dashboard of some sort.)
 *  The priority of this condition, higher number is a higher priority.
 *  site/project arbitrary shorter string a display system can use, can also be used for grouping
 *  condition that actual test that will be compared against.
 *  action email,text,no nothing, things like that.
 * @author L2EDDMAN
 */
public class Alarm {
    String timeseries_id;
    String units;
    String color;
    int priority;
    String site;
    String project;
    AlarmCondition condition;
    String actions; // placeholder for now
    
    public Alarm( String alarm_line ){
        String parts[] = alarm_line.split(",");
        String tsid_units[] = parts[0].split(";");
        timeseries_id = tsid_units[0];
        units = tsid_units[1];
        String color_priority[] = parts[3].split(";");
        color = color_priority[0];
        priority = Integer.parseInt(color_priority[1]);
        
        condition = new AlarmCondition( parts[1]);
        actions = parts[2];
        project = parts[4];
        site = parts[5];
    }
    
    public AlarmResponse check( CTimeSeries ts ) throws BadTimeSeriesException, NoConversionException{
        // 
        String ts_units = ts.getUnitsAbbr();
        if( ts_units.compareToIgnoreCase(this.units) != 0) {
            TimeSeriesHelper.convertUnits(ts, units);
        }
        
        boolean in_alarm =  this.condition.check( ts );
        if( in_alarm ){
            AlarmResponse response = this.buildResponse();
            TimedVariable tv = ts.sampleAt( ts.size() - 1 );
            if( tv != null){
                response.value = tv.getDoubleValue();
                response.timestamp = AlarmResponse.isofmt.format( tv.getTime() );
            } else{
                response.value = Double.NEGATIVE_INFINITY;
                response.timestamp = AlarmResponse.isofmt.format( new Date() );
            }
            
            
            return response;
        } else{
            return null;
        }
        //boolean in_alarm = this.condition.check(null, current_date, db);                
    }
    
    public AlarmCondition get_condition(){
        return this.condition;
    }
    
    public String get_color(){ return color;}
    public int get_priority(){ return priority;}
    
    private AlarmResponse buildResponse(){
        AlarmResponse res = new AlarmResponse();
        res.color = this.color;
        res.condition = this.condition.get_check();
        res.priority = this.priority;
        res.timeseries = this.timeseries_id;
        res.units = this.units;
        res.site = this.site;
        res.project = this .project;
        return res;
    }
    
}
