/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.compgraph;

import decodes.sql.DbKey;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class GraphEdge {    
    String source;
    String target;
    
    public GraphEdge( String source, String target){
        this.source = source;
        this.target = target;
        
    }
    
    public String toString(){
        return String.format( "{\r\n \"data\": { \"id\":\"%s_%s\", \"source\": \"%s\", \"target\":\"%s\"}\r\n}", this.source,this.target,this.source,this.target);
    }
}
