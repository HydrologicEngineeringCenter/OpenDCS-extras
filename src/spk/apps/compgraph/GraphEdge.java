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
    DbKey source;
    DbKey target;
    String source_type;
    String target_type;
    
    public GraphEdge( DbKey source, DbKey target, String source_type, String target_type){
        this.source = source;
        this.target = target;
        this.source_type = source_type;
        this.target_type = target_type;
    }
    
    public String toString(){
        return String.format( "{\r\n data: { id:'%s%s_%s%s', source: '%s%s', target:'%s%s'}\r\n},\r\n", source_type,source,target_type,target,source_type,source,target_type,target);
    }
}
