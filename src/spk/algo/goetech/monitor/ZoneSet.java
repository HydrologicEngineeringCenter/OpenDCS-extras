/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.algo.goetech.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import static spk.algo.support.Logging.debug3;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class ZoneSet {
    
    private ArrayList<Zone> zones;
    
    public ZoneSet( String zonefile ) throws Exception{
        zones = new ArrayList<Zone>();
        File f = new File( zonefile );
        if( !f.exists() )
        {
            throw new java.lang.Exception( "The requested graph file doesn't exist ");
        }

        BufferedReader reader = new BufferedReader( new FileReader(f)  );
        try{
            String line = null;
            String []parts = null;
            String section = null;
            StringBuffer zone = new StringBuffer();
            String mode ="NONE";
            while( (line = reader.readLine()) != null ){                   
                if( line.contains("zone")){
                        if( mode.equalsIgnoreCase("NONE")){
                            zone = new StringBuffer();
                            zone.append(line);
                            mode = "ZONE";
                        } else if( mode.equalsIgnoreCase("ZONE")){                            
                            // the current zone has ended
                            zones.add( new Zone( zone.toString() ) );
                            zone = new StringBuffer();
                            zone.append(line);
                        }                                                
                }
                else if( line.contains("point") ){
                    if( mode.equalsIgnoreCase("ZONE")){
                        zone.append(line );
                    }
                }                
            }
        }
        catch( java.io.IOException e )
        {
            debug3( "load_graph: Failed to read file, reason: " + e.getMessage() );
        }


        reader.close();

        }

    
    
    
    public String getZone( Point p ){
        for( Zone z: zones){
            if ( z.inZone(p ) ){
                return z.getName();
            }
        }
        return null;
    }
    
}
