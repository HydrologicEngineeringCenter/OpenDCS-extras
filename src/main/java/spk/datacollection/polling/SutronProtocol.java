/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.datacollection.polling;
import decodes.db.TransportMedium;
import decodes.polling.IOPort;
import decodes.polling.LoggerProtocol;
import decodes.polling.LoginException;
import decodes.polling.PollingDataSource;
import decodes.polling.ProtocolException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import lrgs.common.DcpMsg;
/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class SutronProtocol extends LoggerProtocol{
    private PollingDataSource dataSource;

    @Override
    public void login(IOPort port, TransportMedium tm) throws LoginException {
        //String login_msg = "\x7Foperator/C\rwatmansec\r";
        try {            
            BufferedReader in = new BufferedReader( new InputStreamReader( port.getIn() ));            
            
            String login_msg = "operator/C\rwatmansec\r";  
            port.getOut().write( 0x06 );
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {                
            }
            port.getOut().write( 0x7F );
            //while( in.ready() ){
            //    Logger.getLogger(SutronProtocol.class.getName()).log(Level.INFO, null, in.readLine() );                
            //}
            port.getOut().write(login_msg.getBytes("UTF8"));
        } catch (IOException ex) {
            Logger.getLogger(SutronProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public DcpMsg getData(IOPort port, TransportMedium tm, Date since) throws ProtocolException {
        DcpMsg msg = new DcpMsg();
        
        return msg;
    }

    @Override
    public void goodbye(IOPort port, TransportMedium tm) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDataSource(PollingDataSource dataSource) {
        this.dataSource = dataSource;
        
    }

    @Override
    public void setAbnormalShutdown(Exception abnormalShutdown) {
        
    }
    
}
