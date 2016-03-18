/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.algo.goetech.monitor;

import java.util.ArrayList;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 * 
 * Define defines a region of an XY plane that contains a zone of interest
 * for monitoring piezometer data
 * 
 */
public class Zone {
    private ArrayList<Point> polygon;
    private String zoneName;
    
    public Zone( String zone ){
        polygon = new ArrayList<Point>();
        
        String lines[] = zone.split("[\\r\\n]+");
        String parts[];
        for( String line: lines){
            parts = line.split(":");
            if( parts[0].equalsIgnoreCase("zone")){
                this.zoneName = parts[1];
            } else if( parts[0].equalsIgnoreCase("point")){
                String xy[] = parts[1].split(",");
                try{
                    double x = Double.parseDouble(xy[0]);
                    double y = Double.parseDouble(xy[1]);
                    polygon.add(new Point( x,y) );
                } catch( Exception err ){
                    
                }
            }
            
        }
        
    }
    
    public String getName(){
        return this.zoneName;
    }
    
    /**
     * given a point (x,y) determine if the point
     * is inside the region defined by this polygone.
     *
     */
    boolean inZone( double x, double y ){
        return this.inZone( new Point(x,y) );
    }
    
    boolean inZone( Point test ){
        // taken from https://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
        // modified as needed to work in java in this framework
        /*Copyright (c) 1970-2003, Wm. Randolph Franklin
            Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
            to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
            and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
                Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimers.
                Redistributions in binary form must reproduce the above copyright notice in the documentation and/or other materials provided with the distribution.
                The name of W. Randolph Franklin may not be used to endorse or promote products derived from this Software without specific prior written permission. 
            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS 
            FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
            IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                       
        */
        int i = 0;
        int j = 0;
        boolean c = false;
        
        
        for ( i=0,j=polygon.size()-1; i < polygon.size(); j = i++ ){
            Point verti = polygon.get(i);
            Point vertj = polygon.get(j);
            if( ( verti.y > test.y  ) != (vertj.y > test.y) && (test.x < (((vertj.x-verti.x)*(test.y-verti.y))/ (vertj.y-verti.y) + verti.x ) ) ){
                c = !c;
            }
            
        }
        return c;
        
    }
}
