/**
 *
 *
 *
 *
 */
package spk.algo.support;

import java.io.BufferedInputStream;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.TreeMap;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import spk.exceptions.FormatException;

/**
 * This class provided the functions need to load the station information text
 * files for shifts and primary data feed.
 *
 * @author L2EDDMAN
 */
public class StationData {

    // the pieces of station information we'll need

    /**
     * the shift value
     */
    public double shift;
    /**
     * date this shift was entered
     */
    public Date date_entered;
    /**
     * what data feed should we use
     */
    public int primary_feed;
    /**
     * person who last edited things
     */
    public String who;
    /**
     * Point of Zero flow, used in the shift calculation to make sure the value
     * never goes below the rating table
     */
    public Double point_zero_flow;
    /**
     * Point of Zero flow Units, used in the shift or combined calculation to
     * adjust value for minimum values
     */
    public String point_zero_flow_units;

    public Date valid_starting;
    
    /**
     * Read in the parameter file data
     *
     * @param file File for the station we want information for
     * @return A effective date indexed tree of station data we can search
     * through
     * @throws spk.exceptions.FormatException
     */
    public static TreeMap<Date, StationData> loadData(String file) throws FormatException, Exception {
        TreeMap<Date, StationData> map = new TreeMap<Date, StationData>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(Resource.fromURI(file)));
        } catch (FileNotFoundException ex ){
            Logging.warning("failed to load file " + ex.getLocalizedMessage() + " assuming no data exists yet");
            return map; // 
        } catch (Exception ex){
            throw ex;
        }
        Date date, date_entered;
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC")); // these files will always be in GMT

        String date_str, shift, feed, entered, who, line, pzf = null;
        int line_no = 0;
        String parts[] = null;
        while ((line = reader.readLine()) != null) {
            line_no++;
            try{
                //System.out.println( line );
                if( line.trim().isEmpty() ) continue;
                else if( line.charAt(0) == '#') continue;
                parts = line.split(",");
                date_str = parts[0];
                shift = parts[1];
                feed = parts[2];
                entered = parts[3];
                who = parts[4];
                if (parts.length > 5) {
                    pzf = parts[5];
                }

                        // convert date
                // convert shift
                StationData d = new StationData();
                date = df.parse(date_str);
                d.valid_starting = date;
                d.shift = Double.parseDouble(shift);
                d.who = who;
                if (pzf != null && !pzf.trim().isEmpty()) {
                    String p2[] = pzf.split("\\s+");
                    d.point_zero_flow = Double.parseDouble(p2[0]);
                    if (p2.length > 1) {
                        d.point_zero_flow_units = p2[1];
                    } else {
                        d.point_zero_flow_units = "na";
                    }
                } else {
                    d.point_zero_flow = Double.NEGATIVE_INFINITY;
                }
                d.date_entered = df.parse(entered);
                feed = feed.trim();
                Logging.debug3(" processing for " + date_str + " shift=" + d.shift + " and primary feed of '" + feed + "' " + "entered by " + d.who);

                if (feed.equalsIgnoreCase("goes")) {
                    d.primary_feed = 0;
                } else if (feed.equalsIgnoreCase("ip")) {
                    d.primary_feed = 1;
                } else if (feed.equalsIgnoreCase("los")) {
                    d.primary_feed = 2;
                } else if (feed.equalsIgnoreCase("off")) {
                    d.primary_feed = 3;
                } else {

                    d.primary_feed = 0;// default to goes if unknown
                }

                StationData tmp = map.get(date);
                if (tmp == null) {
                    map.put(date, d);
                } else {
                    if (d.date_entered.getTime() > tmp.date_entered.getTime()) {
                        map.put(date, d); // if there is a newer entry, overwrite it 
                    }
                }
            } catch( Exception err ){
                throw new FormatException(err, line_no);
            }
        }
        return map;

    }

}
