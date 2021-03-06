/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps;
import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.TsdbAppTemplate;
import decodes.tsdb.DbComputation;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.util.TSUtil;
import decodes.util.CmdLineArgs;
import ilex.cmdline.StringToken;
import ilex.cmdline.TokenOptions;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.Set;
import ilex.util.Logger;
import java.util.Date;
import lrgs.gui.DecodesInterface;
import spk.apps.support.Alarms.AlarmCondition;
import spk.apps.support.Alarms.AlarmList;
import spk.apps.support.Alarms.AlarmResponse;
import java.net.Socket;

/**
 * This software will, on some interval, scan through a list of time series and check if any are missing
 * for a specified interval. If they are missing, and message will be sent to the alarm processor
 * 
 * NOTE: this is not fully setup and while it works, it wont shut down unless you use the kill command on it.
 * 
 * @author L2EDDMAN
 */
public class CheckForMissing extends TsdbAppTemplate {

    //private StringToken compname = new StringToken("C","Computation to Run","", TokenOptions.optSwitch, "" );
    private StringToken alarmfile = new StringToken("A","File with list of alarms","",TokenOptions.optSwitch|TokenOptions.optRequired,"");
    private StringToken socketfile = new StringToken("S","Unix domain socket were we send the alarms","",TokenOptions.optSwitch|TokenOptions.optRequired,"");
    
    
    public CheckForMissing(){
        super("checkformissing.log");
        
    }
    
    
    @Override
    protected void addCustomArgs( CmdLineArgs cmdLineArgs){
        
        cmdLineArgs.addToken(alarmfile);
        cmdLineArgs.addToken(socketfile);
        appNameArg.setDefaultValue("checkformissing");
    }
    
    
    @Override
    protected void runApp() throws Exception {
        //System.out.println( this.compname.getValue() );                             
        AlarmList alarms = new AlarmList();
        alarms.load_alarms( AlarmCondition.CHECK_MISSING, alarmfile.getValue() );
        Set<String> timeseries = alarms.get_timeseries_names();
        
        //TODO: setup the comp lock stuff for this
        while(true){
            
                for( String ts_name: timeseries){
                    try{
                        DbKey key = theDb.getKey(ts_name);

                        CTimeSeries cts = theDb.makeTimeSeries(ts_name);

                        Date end = new Date();

                        int duration = alarms.get_duration(ts_name);
                        Date start = new Date( end.getTime() - duration*1000);
                        theDb.fillTimeSeries(cts, start, end);
                        AlarmResponse res = alarms.check_timeseries(cts);

                        


                        if( res != null ){
                            // send alarm onto the mailer/storage daemon
                            Logger.instance().info(cts.getNameString());
                            Logger.instance().info( res.toString() );
                            
                            Socket s = new Socket("spk-wmlocal1.spk.usace.army.mil",51900);
                            s.getOutputStream().write(res.toString().getBytes() );
                            s.close();
                        }
                    } catch( Exception err){
                        Logger.instance().info( err.toString() );
                    }


                }
            
            Thread.sleep(60*5*1000);            
        }
        
        
    }
    

    

    public static void main( String args[]){
        CheckForMissing byapp = new CheckForMissing();
        try {
            DecodesInterface.setGUI(false);
            
            byapp.execute(args);
        } catch (Exception ex) {
            Logger.instance().fatal(ex.toString());
            //Logger.getLogger(CheckForMissing.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
