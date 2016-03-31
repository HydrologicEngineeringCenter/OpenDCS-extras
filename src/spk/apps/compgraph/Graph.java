/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.compgraph;

import java.util.ArrayList;

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
            
            if( tmp.tsid.getValue() == node.tsid.getValue() ){                                
                return false; // already have
            }
        }
        nodes.add(node); // we don't already have this node
        return true;
        
    }
    
    public boolean addEdge( GraphEdge edge ){
        for (GraphEdge tmp : edges) {
            if( tmp.source.getValue() == edge.source.getValue()
                    &&tmp.target.getValue() == edge.target.getValue() 
                    ){
                return false; //we have this edge already
            }                
        }
        edges.add(edge);
        return true;
    }
    
        
    void printgraph() {
        System.out.println("[");
        for( GraphNode node:  nodes){
            System.out.println(node);
        }
        for( GraphEdge edge: edges){
            System.out.println(edge);
        }
        System.out.println("]");
    }
}
