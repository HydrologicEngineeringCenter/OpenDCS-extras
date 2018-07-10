/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;


import java.util.*;
import java.io.*;
import java.util.regex.Pattern;

/**
 *
 * @author L2EDDMAN
 */
public class FBOCurves {
    
    private class Pair{
        public double x;
        public double y;
                
        public Pair( double x, double y ){
            this.x = x;
            this.y = y;
        }
        public Pair(){
            this.x = Double.NEGATIVE_INFINITY;
            this.y = Double.NEGATIVE_INFINITY;
        }
    }
    
   private HashMap<Integer, ArrayList<Pair> > curves;
   
   public FBOCurves( InputStreamReader data ) throws IOException{
       curves = new HashMap<>();
       BufferedReader reader = new BufferedReader(data);
       String line = null;
       while( (line = reader.readLine() ) != null){
           String parts[] = line.split(":");
           if( "fbo_curve".equalsIgnoreCase(parts[0])){
               // we have a curve so process               
               this.add_curve(parts[1].trim() );               
           }
       }
       
   }
   
   public FBOCurves(){
       this.curves = new HashMap<>();
   }
   
   public void add_curve( String data  ){
       System.out.println(data);
        String parts[] = data.split(",");
       int window = Integer.parseInt(parts[0].trim());
       System.out.println(parts[1]);
       String pairs[] = parts[1].trim().split("\\s+");
       System.out.println("num pairs " + pairs.length );
       curves.put(window, new ArrayList<Pair>() );
       ArrayList<Pair> set = curves.get(window);
       for( String pair: pairs ){
           System.out.println(pair);
           String vals[] = pair.trim().split(Pattern.quote("|") );
           System.out.println("num vals " + vals.length);
           double x = Double.parseDouble(vals[0].trim());
           double y = Double.parseDouble(vals[1].trim());
           set.add( new Pair(x,y) );
       }
   }
   
   
   public double get_topcon( int window, double volume ){
       if( this.curves.containsKey(window ) ){
           ArrayList<Pair> _p = this.curves.get(window);
           Pair pairs[] = (Pair[]) _p.toArray(new Pair[_p.size()]);
           double upper_volume, upper_topcon;
           double lower_volume, lower_topcon;
           
           if( volume < pairs[0].x ){
               return pairs[0].y;                     
           }
           
           for( int i = 0; i < pairs.length; i++ ){
               if( pairs[i].x == volume ){
                   return pairs[i].y;
               } else if( pairs[i].x > volume ){
                   /**
                    * (((higher-lower))/(fcp_higher-fcp_lower))*(fcp-fcp_lower)+lower;
                    */
                   upper_volume = pairs[i].x;
                   upper_topcon = pairs[i].y;
                   lower_volume = pairs[i-1].x;
                   lower_topcon = pairs[i-1].y;
                   double topcon = ((upper_topcon-lower_topcon)/(upper_volume-lower_volume))*(volume-lower_volume)+lower_topcon;
                   return topcon;
               }
           }
           
           return pairs[pairs.length-1].y; // volume greater than maximum volume
           
       } else{       
        return Double.NEGATIVE_INFINITY;
       }
   }
   
}
