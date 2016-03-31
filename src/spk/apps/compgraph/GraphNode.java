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
    public DbKey tsid;
    public String name;    
    public String type;
    
    
    GraphNode(DbKey key, String name,  String type ) {
        this.tsid = key;
        this.name = name;
        
        this.type = type;
    }
    
    public String toString(){
        return String.format("{\r\ndata:{ id: '%s%s', name: '%s', type: '%s' }\r\n},\r\n", this.type, this.tsid, this.name,this.type);
    }
    
    
}
