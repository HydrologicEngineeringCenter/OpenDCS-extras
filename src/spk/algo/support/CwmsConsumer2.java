/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import cwmsdb.CwmsTsJdbc;
import decodes.consumer.DataConsumer;
import decodes.consumer.DataConsumerException;
import ilex.var.TimedVariable;
import decodes.db.Platform;
import decodes.decoder.DecodedMessage;
import decodes.decoder.Sensor;
import decodes.decoder.TimeSeries;
import ilex.var.NoConversionException;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import decodes.cwms.CwmsTimeSeriesDb;
import decodes.cwms.CwmsSqlDatabaseIO;
import decodes.cwms.CwmsDbConfig;
import decodes.cwms.CwmsDbUtils;
import decodes.cwms.CwmsFlags;
import decodes.cwms.CwmsTsId;
import decodes.db.ConfigSensor;
import decodes.db.DataType;

import decodes.db.Database;
import decodes.tsdb.BadConnectException;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import ilex.util.AuthException;
import ilex.util.UserAuthFile;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;
/**
 *
 * @author L2EDDMAN
 */
public class CwmsConsumer2 extends DataConsumer{
    private CwmsTimeSeriesDb db = null;
    private CwmsTsJdbc db2 = null;
    public  String version = null;
    
    @Override
    public void open(String string, Properties prprts) throws DataConsumerException {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        db = new CwmsTimeSeriesDb();
        
        for( String k: prprts.stringPropertyNames()){
            Logging.debug3(k);
        }
        UserAuthFile af = new UserAuthFile(decodes.util.DecodesSettings.instance().DbAuthFile);
        //UserAuthFile af = new UserAuthFile("/usr1/l2ccp/DCSTOOL-CCP/.decodes.auth");
        try {
            af.read();
        } catch (IOException ex) {
            Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AuthException ex) {
            Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logging.debug3("Username= " +af.getUsername() );
        Properties p = new Properties();
        p.setProperty("username", af.getUsername());
        p.setProperty("password", af.getPassword());
        try {
            db.connect("decodes", p);            
            db2 = new CwmsTsJdbc( db.getConnection() );
        } catch (BadConnectException ex) {
            Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
            Logging.debug3( ex.getMessage() );
            return;
        } catch (SQLException ex) {
            Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
            Logging.debug3( ex.getMessage() );
            return;
        }
        
        
        version = prprts.getProperty("cwmsVersion");
        //CwmsDbConfig conf = CwmsDbConfig.instance();
        Logging.debug3( "**** office is:   " + db.getDbOfficeId() );
        Logging.debug3( "****  Version is: " + version);
        
    }

    @Override
    public void close() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        
        db.closeConnection();
    }

    @Override
    public void startMessage(DecodedMessage dm) throws DataConsumerException {
        Iterator<TimeSeries> it = dm.getAllTimeSeries();
        Platform p = dm.getPlatform();
        Logging.debug3("Processing Platform: " + p.getSiteName(false));
        
        while( it.hasNext()){
            TimeSeries ts = it.next();    
            if( ts.size() > 0 ){
                /* TODO: need to set this up to lookup CWMS <-> SHEF conversions perhaps
                 * or just makes sure there is a correct CWMS name for each sensor
                 */
                Sensor s = ts.getSensor();
                String name = s.getSensorSiteName();
                if( name == null )
                {
                    name = p.getSiteName(false);
                }
                Logging.debug3("Processing: " + name);
                CTimeSeries cts = null;
                try {
                    // build time series name
                    
                    String sensor = s.getName();                
                    String s2 = s.getDisplayName();
                    Iterator<DataType> dts = s.configSensor.getDataTypes();
                    while(dts.hasNext()){
                        DataType dt = dts.next();
                        if( dt.getStandard().equalsIgnoreCase("cwms") ){
                            sensor = dt.getCode();
                        }
                    }
                    Logging.debug3("Sensor: " + sensor + " / " + s2);
                    int interval = ts.getTimeInterval();
                    Logging.debug3("Interval: " + interval);
                    String interval_s = this.interval_str(interval);
                    Logging.debug3("Interval: " + interval_s);
                    //ConfigSensor config = p.getConfig().getSensor(s.getNumber());
                    //config.g
                    // For now, everything is inst.
                    String tsid = String.format("%s.%s.Inst.%s.0.%s", name, sensor,interval_s,this.version);

                    Logging.debug3("Storing: " + tsid);


                    //CwmsTsId id = (CwmsTsId)db.getTimeSeriesIdentifier(tsid);
                    //Logging.debug3("TSID is: " + id.displayName);

                    /*  need to change this to handle brand new sites/time series */

                    Logging.debug3(" " + db.canCreateTimeSeries() );
                    //long test = db.createTimeSeriesInDb(id);
                    try{
                        cts = db.makeTimeSeries(tsid);
                    }
                    catch(NoSuchObjectException e){
                        Logging.debug3(e.getMessage());
                        Logging.debug3("attempting to create a new time series");
                        int tscode = db2.createTsCode(db.getDbOfficeId(), tsid, 0, 0, 0,false, true);
                        if( tscode != 0 ){
                            cts = db.makeTimeSeries(tsid);
                        }
                    }
                    cts.setDisplayName(tsid);
                    String units = ts.getUnits(); 
                    Logging.debug3("Units ->" + units);
                    cts.setUnitsAbbr(ts.getUnits());


                    long times[] = new long[ts.size()];
                    double values[] = new double[ts.size()];
                    int quals[] = new int[ts.size()];

                    for( int i = 0; i < ts.size(); i++){                
                        TimedVariable tv = ts.sampleAt(i);  
                        //tv.setFlags(decodes.tsdb.VarFlags.TO_WRITE);
                        times[i] = tv.getTime().getTime();
                        values[i] = tv.getDoubleValue();
                        quals[i] = CwmsFlags.flag2CwmsQualityCode(tv.getFlags());

                        cts.addSample(tv);                    
                        Logging.debug3("Date -> Data: " + tv.getTime().toString() + " -> " + tv.getDoubleValue() + " Quality: " + quals[i]);                   
                    }

                    Logging.debug3("There are " + cts.size() + " values to store" );

                    db2.store(db.getDbOfficeId(), tsid, units, times, values, quals, ts.size(), "REPLACE ALL", true, null);


                    //db.fillTimeSeriesMetadata(cts);
                    //.
                    //I have no idea why this doesn't work.
                    //db.saveTimeSeries(cts);
        
                    db.commit();
                    Logging.debug3("The data should now be stored");
                } catch (DbIoException ex) {
                    Logging.debug3(ex.getMessage());
                    Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);                
                } catch (NoSuchObjectException ex) {
                    Logging.debug3(ex.getMessage());
                    Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
                }/* catch (BadTimeSeriesException ex) {
                    Logging.debug3(ex.getMessage());
                    Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
                }*/ catch (NoConversionException ex) {
                    Logging.debug3(ex.getMessage());
                    Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SQLException ex) {
                    Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
                } 
            
            
            }
            
            
        }
    }

    @Override
    public void println(String string) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void endMessage() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private String interval_str( int interval){
        int tmp;
        if( interval == 0){
            return "0";
        }
        else if( interval > 60){
            interval = interval/60; 
            if( interval >= 60 ){
                interval = interval/60; // hours
                if( interval == 1){
                    return "1Hour";
                }
                else{
                    return String.format("%dHours", interval);
                }
            }
            else{
                // minutes
                if( interval == 1){
                    return "1Minute";
                }
                else{
                    return String.format("%dMinutes", interval);
                }
                    
            }
                    
                    
        }
        return null;
    }
}
