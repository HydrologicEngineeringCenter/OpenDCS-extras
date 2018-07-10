/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.compgraph;

import java.util.ArrayList;
import org.apache.commons.lang.xwork.StringUtils;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class Graph {
    ArrayList<GraphNode> nodes;
    ArrayList<GraphEdge> edges;

    
    public Graph(){
        nodes = new ArrayList<GraphNode>();
        edges = new ArrayList<GraphEdge>();

    }
        
    public boolean addNode( GraphNode node ){
        for( int i=0; i < nodes.size(); i++){
            GraphNode tmp = nodes.get(i);
            
            if( tmp.id.equalsIgnoreCase( node.id ) ){                                
                return false; // already have
            }
        }
        nodes.add(node); // we don't already have this node
        return true;
        
    }
    
    public boolean addEdge( GraphEdge edge ){
        for (GraphEdge tmp : edges) {
            if( tmp.source.equalsIgnoreCase(edge.source)
                    && tmp.target.equalsIgnoreCase( edge.target )
                    ){
                return false; //we have this edge already
            }                
        }
        edges.add(edge);
        return true;
    }
    
        
    void printgraph() {
        
        System.out.println("{ \"data\": [");
        
        System.out.print( StringUtils.join(nodes.toArray(), ",\r\n") );
        System.out.println(",");
        System.out.print( StringUtils.join(edges.toArray(), ",\r\n") );
        /*
        for( GraphNode node:  nodes){
            System.out.println(node);
        }
        for( GraphEdge edge: edges){
            System.out.println(edge);
        }
        */
        System.out.println("]");
        ArrayList<String> datatypes = new ArrayList<String>();
        for( GraphNode node: nodes){
            if( !datatypes.contains("\""+node.datatype+"\"") ){
                datatypes.add("\""+node.datatype +"\"");
            }
        }
        System.out.println(", \"categories\": [");
        System.out.print( StringUtils.join(datatypes,",\r\n"));
        System.out.println("]");
        System.out.println("}");
    }
}
