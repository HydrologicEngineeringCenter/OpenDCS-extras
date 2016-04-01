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

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class GraphNode {
    public String id;
    public String name;    
    public String type;
    public String datatype;
    
    
    GraphNode(String key, String name,  String type, String datatype ) {
        this.id = key;
        this.name = name;
        
        this.type = type;
        
        
        
        this.datatype = datatype.toLowerCase().split("-")[0]; // we only care about the base parameter
    }
    
    public String toString(){
        return String.format("{\r\n\"data\":{ \"id\": \"%s\", \"name\": \"%s\", \"type\": \"%s\", \"datatype\": \"%s\" }\r\n}", this.id, this.name,this.type,this.datatype);
        
        
    }
    
    
}
