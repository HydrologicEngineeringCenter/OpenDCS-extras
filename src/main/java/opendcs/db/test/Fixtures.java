/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opendcs.db.test;

import decodes.cwms.CwmsTsId;
import decodes.db.DataType;
import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DataCollection;
import decodes.tsdb.TimeSeriesDb;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.VarFlags;
import ilex.var.TimedVariable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import opendcs.dai.TimeSeriesDAI;
import spk.algo.support.Resource;

/**
 * base data for used by Unit Tests handles loading data
 *
 * @author L2EDDMAN
 */
public class Fixtures {

    private static Fixtures fixtures = null;
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private DataCollection dc = null;

    private HashMap<String, DbKey> keys_by_tsname = null;
    private TimeSeriesDAI tsdai;
    private TimeSeriesDb db;
    private Date start = null;
    private Date end = null;

    /**
     *
     */
    private Fixtures() {
        dc = new DataCollection();
        keys_by_tsname = new HashMap<>();
    }

    /**
     * Will initialize data if needed.
     * Needs a TimeSeriesDAI implementation to prepare data
     * @param tsdai
     * @return
     * @throws Exception 
     */
    public static Fixtures getFixtures(TimeSeriesDb db) throws Exception {
        if (fixtures == null) {
            
            fixtures = new Fixtures();
            fixtures.db = db;
            fixtures.tsdai = db.makeTimeSeriesDAO();
            fixtures.loadData(/*fixtures.tsdai*/);
        }
        return fixtures;
    }
    /**
     * Get the Data Collection
     * @return 
     */
    public DataCollection getDC() {
        return dc;
    }

    /**
     * load our data from files
     */
    private void loadData(/*TimeSeriesDAI tsdai*/) throws Exception {
        if (dc.size() == 0) {

                // these are unit tests, we don't need to reload a cache
            // need to go through a list of data
            BufferedReader reader = new BufferedReader(new InputStreamReader(Resource.fromURI(URI.create("classpath:/data/data.txt"))));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if( line.charAt(0) == '#') continue;
                String parts[] = line.split(",");
                DbKey key = DbKey.createDbKey(Long.parseLong(parts[0]));
                String tsname = parts[1];
                URI uri = URI.create("classpath:/data/" + tsname.replace(" ","%20") + ".csv");
                String units = parts[2];

                String tsname_parts[] = tsname.split("\\.");
                TimeSeriesIdentifier tsid = new CwmsTsId();
                tsid.setDisplayName(tsname);
                tsid.setUniqueString(tsname);
                tsid.setKey(key);
                tsid.setSiteName(tsname_parts[0]);
                tsid.setDataType(new DataType("cwms", tsname_parts[1]));

                CTimeSeries cts = tsdai.makeTimeSeries(tsid);
                cts.setTimeSeriesIdentifier(tsid);
                cts.setUnitsAbbr(units);

                this.fillDataFromStream(cts, uri);

                dc.addTimeSeries(cts);
                
                keys_by_tsname.put(tsname, key);
                
            }

        }

    }

    /**
     * used by loadData. Handles retrieving data from the classpath for the unit tests
     * @param cts
     * @param uri
     * @throws Exception 
     */
    public void fillDataFromStream(CTimeSeries cts, URI uri) throws Exception {
            
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            BufferedReader reader = new BufferedReader( new InputStreamReader(Resource.fromURI(uri)));
            String line = null;
            while( (line = reader.readLine()) != null ){
                if( line.trim().isEmpty() ) continue;
                String parts[] = line.split(",");
                TimedVariable tv = new TimedVariable();
                tv.setTime(sdf.parse(parts[0]));
                tv.setValue(Double.parseDouble(parts[1]));
                tv.setFlags(VarFlags.DB_ADDED);
                cts.addSample(tv);
            }
    }

    public DbKey getTimeSeriesKey(String unique_name) {
        return keys_by_tsname.get(unique_name);
    }

    
}
