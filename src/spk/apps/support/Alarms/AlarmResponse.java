/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.support.Alarms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author L2EDDMAN
 */
public class AlarmResponse implements Comparable<AlarmResponse>{
    
    public String project;    
    public String site;
    public String condition;
    public String timestamp;
    public String timeseries;
    public String units;
    public String color;
    public double value;
    public int priority;
    public static SimpleDateFormat isofmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    
    public AlarmResponse(){
        project = "";
        site = "";
        condition = "";
        Date t = new Date();
        
        isofmt.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        timestamp = isofmt.format( t );
        timeseries = "";
        units = "";
    }
    
    @Override      
    public String toString(){
        StringBuilder buf = new StringBuilder();
        buf.append( "\"" + site + "\":{\n");
        buf.append( "\t\"project\": \"" + project + "\",\n" );
        buf.append( "\t\"condition\": \"" + condition + "\",\n");
        buf.append( "\t\"color\": \"" + color + "\",\n");
        if( value == Double.NEGATIVE_INFINITY ){
            buf.append( "\t\"value\": null,\n");
        }else{    
            buf.append( "\t\"value\": " + value + ",\n");
        }
        buf.append( "\t\"timestamp\": \"" + timestamp + "\",\n");
        buf.append( "\t\"timeseries\": \"" + timeseries + "\",\n");
        buf.append( "\t\"units\": \"" + units + "\",\n");
        buf.append("}\n");
        return buf.toString();
    }
    
    @Override
    public int compareTo( AlarmResponse other){
        if( this.priority < other.priority ){
            return -1;
        } else if( this.priority > other.priority){
            return 1;
        } else{
            return 0;
        }
    }
        
}
