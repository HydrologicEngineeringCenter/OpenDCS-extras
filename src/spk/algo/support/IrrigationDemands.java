/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import decodes.tsdb.DbCompException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import spk.algo.support.DateTime;
/**
 *
 * @author L2EDDMAN
 */
public class IrrigationDemands {

        /*
         * the array list has enough capacity in it that we can assume the array starts at 1
         */
        private TreeMap< Date, ArrayList<Double> > demands;
    
        public IrrigationDemands( String filename ) throws DbCompException, FileNotFoundException, IOException, ParseException, java.text.ParseException{
            SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            File f = new File(filename);
            if(!f.exists())
                throw new DbCompException("No irrigation demand file, this project requires one");
            BufferedReader reader = new BufferedReader( new FileReader(f) );
            JSONParser parser = new JSONParser();
            demands = new TreeMap< Date, ArrayList<Double> >();
            
            JSONArray array = (JSONArray)parser.parse(reader);
            for( int i=0; i < array.size(); i++ )
            {
                JSONObject demand = (JSONObject)array.get(i);
                String time = (String)demand.get("name");
                Date eff_date = df.parse(time);
                ArrayList<Double> _demands = new ArrayList<Double>();
                for( int z=0; z < 366; z++){ _demands.add(0.0); } // initialize the array to a full leap year
                //double _demands[] = new double[366];
                JSONArray children = (JSONArray)demand.get("children");
                // children are guaranteed to be sorted
                for( int j = 0; j < children.size(); j++ ){
                    if( j+1 < children.size() ){ // otherwise we are at the last demand
                        // get the first flow
                        JSONObject obj = (JSONObject)children.get(j);
                        String demand_line = (String)obj.get("name");
                        String parts[] = demand_line.split(" -> ");
                        time = parts[0];
                        int t1 = DateTime.to_wy_julian( df.parse(time) );
                        double flow1 = Double.parseDouble( parts[1] );
                        
                        obj = (JSONObject)children.get(j+1);
                        demand_line = (String)obj.get("name");
                        parts = demand_line.split(" -> ");
                        time = parts[0];
                        int t2 = DateTime.to_wy_julian( df.parse(time) );
                        // we don't actually need the 2nd flow
                        double flow2 = Double.parseDouble( parts[1] ); 
                        // using the julian data this should take care of leap year
                        for( int k=t1; k < t2; k++ ){
                            _demands.add(k, flow1);
                        }
                        _demands.add(t2, flow2 );
                        
                    }
                    else{
                        JSONObject obj = (JSONObject)children.get(j);
                        String demand_line = (String)obj.get("name");
                        String parts[] = demand_line.split(" -> ");
                        time = parts[0];
                        int t1 = DateTime.to_wy_julian( df.parse(time) );
                        double flow1 = Double.parseDouble( parts[1] );
                        for( int k=t1; k < t1+31; k++ ){ // just arbitrary go through the next month in case we only have 31July
                            _demands.add(k, flow1);
                        }
                    }
                                                   
                    
                }
                demands.put(eff_date, _demands);
                
            }
        }
        
        public double[] getDemands( Date t ) throws Exception{
            Date nearest = demands.floorKey(t);
            if( nearest == null) { throw new Exception("No Information previous or on this day"); }
            ArrayList<Double> tmp = demands.get(nearest);
            double list[] = new double[366];
            for( int i = 0; i < 366; i++){
                list[i] = tmp.get(i);
            }
            return list;
        }
        
}
