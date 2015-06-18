/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.goetech;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 *
 * @author L2EDDMAN
 */
public class VibratingWireData {
    
    private HashMap< String, TreeMap<Date, VibratingWireSheet> > data;
    
    
    public VibratingWireData( String file ) throws java.lang.Exception{
        data = new HashMap< String, TreeMap< Date, VibratingWireSheet> >();
        SimpleDateFormat sdf = new SimpleDateFormat( "mm/dd/yyyy HH:MM");
        sdf.setTimeZone( TimeZone.getTimeZone("UTC"));
        File f = new File( file );
        if( !f.exists() )
        {
            throw new java.lang.Exception( "The requested graph file doesn't exist ");
        }

        BufferedReader reader = new BufferedReader( new FileReader(f)  );
        try{
            String line = null;
            String []parts = null;
            String location = null;
            while( (line = reader.readLine()) != null )
            {
                if( line.trim().equals("") || line.trim().charAt(0) == '#' ) continue; // comment or blank line, skip
                parts = line.split( ",");
                location = parts[0].toLowerCase();
                VibratingWireSheet s = new VibratingWireSheet();
                s.G = Double.parseDouble(parts[2]);
                s.A = Double.parseDouble(parts[3]);
                s.B = Double.parseDouble(parts[4]);
                s.C = Double.parseDouble(parts[5]);
                s.K = Double.parseDouble(parts[6]);
                s.R0 = Double.parseDouble(parts[7]);
                s.T0 = Double.parseDouble(parts[8]);
                s.P0 = Double.parseDouble(parts[9]);
                Date d = sdf.parse(parts[11].trim());
                
                if( !data.containsKey(location)){
                    data.put(location, new TreeMap<Date, VibratingWireSheet>() );
                }

                data.get(location).put(d, s);
                
            }
        }catch( java.io.IOException e ){

        }
    }
    
    public VibratingWireSheet getVibratingWireSheet( Date d, String location ){
        try{
            Date nearest = data.get(location).floorKey(d);
            VibratingWireSheet s = data.get(location).get(nearest);
            return s;
        }
        catch( java.lang.Exception e){
            return null;
        }                
    }
    
}
