/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.support.Alarms;

import decodes.tsdb.CTimeSeries;
import ilex.util.Logger;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author L2EDDMAN
 */
public class AlarmList {

    HashMap< String, ArrayList<Alarm> > alarms;
    HashMap< String, Integer>     longest_duration; // keep track of the longest duration of each ts so we can get the needed data
    
    public AlarmList(){
        
        alarms = new HashMap<String, ArrayList<Alarm> > ();
        longest_duration = new HashMap<String, Integer> ();
    }
    /*  lines are in the format of:
        Site;units,condition;duration,action:level,color:priority
        
        @param type_mask specifies which alarms this instance will process
    
    */
    public void load_alarms( int type_mask, String file ) throws FileNotFoundException{
        FileReader file_reader = new FileReader(file);
        try{
            BufferedReader reader = new BufferedReader( file_reader );
            
            String line;
            while( ( line = reader.readLine()  ) != null   ){
                Logger.instance().debug3("Processing: " + line);
                if( line.trim().length() == 0 ) continue;
                if( line.trim().charAt(0) == '#') continue;
                // done with comments and blank space

                String parts[] = line.split(",");        
                String ts_units = parts[0];
                String ts = ts_units.split(";")[0];
                Alarm a = new Alarm(line);

                ArrayList<Alarm> list = alarms.get(ts);
                if( list == null){
                    Logger.instance().debug3("Creating new list for Time Seires: " + ts);
                    list = new ArrayList<Alarm>();
                    alarms.put( ts, list);
                    longest_duration.put(ts, 0);
                }
                int result = type_mask & a.get_condition().get_check_type();
                Logger.instance().debug3("Mask check: " + result);
                if(( type_mask & a.get_condition().get_check_type() ) > 0){
                       list.add(a);
                    if( a.get_condition().get_duration() > longest_duration.get(ts ) ){
                       longest_duration.put(ts, a.get_condition().get_duration());
                    }
                }
            }
            
            
        }catch( Exception x){
            Logger.instance().info( x.toString() );
        }
        
    }
    
    public AlarmResponse check_timeseries( CTimeSeries ts ){
        /*
        Assume calling program has gotten all data
        */
        try{
            
            ArrayList<Alarm> myalarms = alarms.get(ts.getTimeSeriesIdentifier().getUniqueName() );
            ArrayList<AlarmResponse> responses = new ArrayList<AlarmResponse>();
            for( Alarm a: myalarms){
                Logger.instance().debug3("Testing" + a.get_condition() );
                AlarmResponse res = a.check(ts);
                if( res != null ){
                    responses.add( res );
                }
            }
            Collections.sort(responses);
            // check responses and return the highest priority
            if( responses.size() > 0 ){
                return responses.get(responses.size()-1); // get the highest priority alarm
            }
            
        } catch( Exception err){
            Logger.instance().warning( err.toString() );
        }
        return null; // no alarm
    }
        
    public int get_duration( String timeseries ){
        try{
            return longest_duration.get(timeseries);
        } catch( Exception err ){
            return -1;
        }
    }
    
    public Set<String> get_timeseries_names(){
        return this.alarms.keySet();
    }
    
}
