/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.compgraph;

import decodes.comp.Computation;
import decodes.sql.DbKey;
import decodes.tsdb.DbComputation;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.commons.lang.xwork.StringUtils;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class GraphNode {
    public String id;
    public String name;    
    public String type;
    public String datatype;
    public DbComputation comp;
    
    GraphNode(String key, String name,  String type, String datatype, DbComputation comp ) {
        this.id = key;
        this.name = name;
        this.comp = comp;
        this.type = type;
        
        
        
        this.datatype = datatype.toLowerCase().split("-")[0]; // we only care about the base parameter
    }
    
    public String toString(){
        return String.format("{\r\n\"data\":{ \"id\": \"%s\", \"name\": \"%s\", \"type\": \"%s\", \"datatype\": \"%s\", \"extra\": %s }\r\n}", this.id, this.name,this.type,this.datatype,this.extradata());
        
        
    }
    
    
    public String extradata(){
        String data = "{}";
        if( comp != null ){
            data = "{\r\n";
            data = data +"\"algorithmName\": \"" + comp.getAlgorithmName() + "\",\r\n";
            data = data +"\"loadingApplication\": \"" + comp.getApplicationName() + "\",\r\n";
            data = data +"\"isGroupComp\": \"" + comp.hasGroupInput() + "\",\r\n";
            data = data +"\"groupName\": \"" + comp.getGroupName() + "\",\r\n";
            data = data +"\"processDataFrom\": \"" + comp.getValidStart() + "\",\r\n";
            data = data +"\"processDataUntil\": \"" + comp.getValidEnd() + "\",\r\n";
            ArrayList<String> proppairs = new ArrayList<String>();
            Properties properties = comp.getProperties();
            for( Object key: properties.keySet()){
                String prop = properties.getProperty((String)key );
                if( prop == null || prop.equals("\"\"") || prop.equals("") ){
                    prop = "not defined";
                }
                proppairs.add( String.format("\"%s\": \"%s\"", (String)key, prop ));                
            }
            
            if( properties.size() > 0){
                data = data + "\"properties\": {\r\n";
                data = data + StringUtils.join(proppairs, ",");
                data = data + "}\r\n";
            } else{
                data = data + "\"properties\": {}\r\n";
            }
            
            
            data = data + "}\r\n";
                    
        } else{
            // perhaps to something with the time series data?
        }
        return data;
    }
    
    
}
