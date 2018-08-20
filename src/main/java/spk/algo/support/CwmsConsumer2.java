/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import cwmsdb.CwmsTsJdbc;
import decodes.consumer.DataConsumer;
import decodes.consumer.DataConsumerException;
import decodes.cwms.CwmsConstants;
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
//import decodes.cwms.;
import decodes.cwms.CwmsFlags;
import decodes.cwms.CwmsTsId;
import decodes.datasource.UnknownPlatformException;
import decodes.db.ConfigSensor;
import decodes.db.DataType;

import decodes.db.Database;
import decodes.sql.DbKey;
import decodes.tsdb.BadConnectException;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.VarFlags;
import ilex.util.AuthException;
import ilex.util.UserAuthFile;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import opendcs.dai.TimeSeriesDAI;
/**
 *
 * @author L2EDDMAN
 */
public class CwmsConsumer2 extends DataConsumer{
    private CwmsTimeSeriesDb db = null;
    private CwmsTsJdbc db2 = null;
    public  String version = null;
    public boolean savePower = false;
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
            //db2 = new CwmsTsJdbc( db.getConnection() );
        } catch (BadConnectException ex) {
            Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
            Logging.debug3( ex.getMessage() );
            return;
        }
        
        savePower = Boolean.parseBoolean( prprts.getProperty("savePower", "false") );
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
        if( p == null){
            try {
                p = dm.getRawMessage().getPlatform();
            } catch (UnknownPlatformException ex) {
                Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Logging.debug3("Processing Platform: " + p.getSiteName(false));
        
        
        CTimeSeries cts = null;
        TimeSeriesDAI tdao = null;
        String tsid = null;
        
        // GOES EIRP is ALWAYS associated with the main logger name
        // So we'll just pull the platform name and use it.
        if( savePower ){
             tsid = String.format("%s.%s.Inst.0.0.%s", p.getSiteName(false), "Power-GOES XMIT",this.version);
            //long test = db.createTimeSeriesInDb(id);
            
            try{
                tdao = db.makeTimeSeriesDAO();  
                //TimeSeriesIdentifier _tsid= tdao.getTimeSeriesIdentifier(tsid);                
                try{
                    cts = db.makeTimeSeries(tsid);
                }
                catch(NoSuchObjectException e){
                    Logging.debug3(e.getMessage());
                    Logging.debug3("attempting to create a new time series");
                    //int tscode = db2.createTsCode(db.getDbOfficeId(), tsid, 0, 0, 0,false, true);
                    //long tscode = db2.createTsCodeBigInteger(tsid, tsid, 0, 0, 0, false,true);
                    CwmsTsId _tsid = new CwmsTsId();
                    _tsid.setUniqueString(tsid);
                    DbKey key =null;
                    try {
                        key = tdao.createTimeSeries(_tsid);
                    } catch (BadTimeSeriesException ex) {
                        Logging.debug3( ex.getMessage() );
                    }
                    if( key != null ){
                        cts = db.makeTimeSeries(tsid);
                    }
                }
                
                cts.setDisplayName(tsid);        
                cts.setUnitsAbbr("W");
                double sig_strength = dm.getRawMessage().getPM("SignalStrength").getDoubleValue();
                TimedVariable _tv = new TimedVariable();              
                _tv.setTime( dm.getRawMessage().getOrigDcpMsg().getXmitTime() );
                
                _tv.setValue( sig_strength);
                VarFlags.setToWrite(_tv);
                cts.addSample(_tv);
                
                tdao.saveTimeSeries(cts);
            }
            catch(NoSuchObjectException e){
                Logging.debug3(e.getMessage());

            } catch (DbIoException ex) {
                Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex){
                Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, ex);
            } finally{
                tdao.close();
            }
        }
        
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
                cts = null;
                try {
                    tdao = db.makeTimeSeriesDAO();
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
                    //String type = s.getProperty(CwmsConstants.CWMS_PARAM_TYPE);
                    
                    Logging.debug3("Interval: " + interval_s);
                    //ConfigSensor config = p.getConfig().getSensor(s.getNumber());
                    //config.g
                    // For now, everything is inst.
                    tsid = String.format("%s.%s.Inst.%s.0.%s", name, sensor,interval_s,this.version);

                    Logging.debug3("Storing: " + tsid);


                    //CwmsTsId id = (CwmsTsId)db.getTimeSeriesIdentifier(tsid);
                    //Logging.debug3("TSID is: " + id.displayName);

                    /*  need to change this to handle brand new sites/time series */

                    
                    //long test = db.createTimeSeriesInDb(id);
                    try{
                        cts = db.makeTimeSeries(tsid);
                    }
                    catch(NoSuchObjectException e){
                        Logging.debug3(e.getMessage());
                        Logging.debug3("attempting to create a new time series");
                        //int tscode = db2.createTsCode(db.getDbOfficeId(), tsid, 0, 0, 0,false, true);
                        //long tscode = db2.createTsCodeBigInteger(tsid, tsid, 0, 0, 0, false,true);
                        CwmsTsId _tsid = new CwmsTsId();
                        _tsid.setUniqueString(tsid);
			DbKey key =null;
                        try {
                            key = tdao.createTimeSeries(_tsid);
                        } catch (BadTimeSeriesException ex) {
                            Logging.debug3( ex.getMessage() );
                        }
                        if( key != null ){
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
                        try{
                            TimedVariable tv = ts.sampleAt(i);  
                            
                            tv.setFlags(decodes.tsdb.VarFlags.TO_WRITE);
                            Logging.debug3( "TV value = " + tv.getStringValue() );

                            times[i] = tv.getTime().getTime();
                            try{
                                values[i] = tv.getDoubleValue();                                
                                quals[i] = CwmsFlags.flag2CwmsQualityCode(tv.getFlags());
                            } catch( Exception err ){
                                values[i] = Double.NEGATIVE_INFINITY;
                                quals[i] = CwmsFlags.VALIDITY_MISSING;
                            }

                            cts.addSample(tv);                    
                            Logging.debug3("Date -> Data: " + tv.getTime().toString() + " -> " + tv.getDoubleValue() + " Quality: " + quals[i]);                   
                        } catch( Exception err ){
                            Logging.debug3(err.getMessage());
                            Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, err);
                        }
                    }

                    Logging.debug3("There are " + cts.size() + " values to store" );

                    cts.sort(); // lack of this caused a really annoying issue.
                    //db2.store(db.getDbOfficeId(), tsid, units, times, values, quals, ts.size(), "REPLACE WITH NON MISSING", true, null);
                    
                    try{
                        tdao.saveTimeSeries(cts);
                    } catch( Exception err ){
                        Logger.getLogger(CwmsConsumer2.class.getName()).log(Level.SEVERE, null, err);
                    }
                    
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
                } 
                finally{
                        tdao.close();
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
