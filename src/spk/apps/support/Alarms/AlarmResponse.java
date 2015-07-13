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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONWriter;
import org.json.JSONObject;
/**
 * The message that gets passed on for further processing. Use the JSON format for exchange.
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
        try {
            JSONObject json = new JSONObject();
            
            json.put("site_name", site);
            json.put("project", project);
            json.put("condition", condition);
            json.put("color", color);
            json.put("timestamp", timestamp);
            json.put("timeseries", timeseries);
            json.put("units", units);
            if( value == Double.NEGATIVE_INFINITY ){
                json.put("value",(Object) null);
                
            } else{
                json.put("value", value);
            }
            /*
            StringBuilder buf = new StringBuilder();
            buf.append( "{\"site_name\": \"" + site + "\",\n");
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
            buf.append( "\t\"units\": \"" + units + "\"\n");
            buf.append("}\n");
            return buf.toString();
                    */
            return json.toString();
        } catch (JSONException ex) {
            Logger.getLogger(AlarmResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "Unable to format";
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
