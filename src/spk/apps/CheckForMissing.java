/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.apps;
import decodes.tsdb.TsdbAppTemplate;
import decodes.tsdb.DbComputation;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.util.CmdLineArgs;
import ilex.cmdline.StringToken;
import ilex.cmdline.TokenOptions;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import lrgs.gui.DecodesInterface;




/**
 * This software will, on some interval, scan through a list of time series and check if any are missing
 * for a specified interval. If they are missing, and message will be sent to the alarm processor
 * @author L2EDDMAN
 */
public class CheckForMissing extends TsdbAppTemplate {

    //private StringToken compname = new StringToken("C","Computation to Run","", TokenOptions.optSwitch, "" );
    
    public CheckForMissing(){
        super("compsbyapp.log");
     
    }
    
    
    @Override
    protected void addCustomArgs( CmdLineArgs cmdLineArgs){
        
     
        appNameArg.setDefaultValue("compsbyapp");
    }
    
    
    @Override
    protected void runApp() throws Exception {
        //System.out.println( this.compname.getValue() );                             
        System.out.println( "Query Database");
        ResultSet rs = theDb.doQuery( "select a.loading_application_id, "
                + "(select b.loading_application_name from ccp.hdb_loading_application b where a.loading_application_id=b.loading_application_id) AS App,"
                + "count(site_datatype_id) as num_comps "
                + "from ccp.cp_comp_tasklist a group by a.loading_application_id order by num_comps desc");
        System.out.printf("%10s %-20s %10s\n", "AppID", "App Name","Num Comps");
        while( rs.next() )
        {
            int id = rs.getInt(1);
            String name = rs.getString(2);
            int count = rs.getInt(3);
            System.out.printf("%10d %-20s %10d\n", id,name,count);
            
            
        }
        
        
    }
    
    

    public static void main( String args[]){
        CheckForMissing byapp = new CheckForMissing();
        try {
            //DecodesInterface.setGUI(false);
            byapp.execute(args);
        } catch (Exception ex) {
            Logger.getLogger(CheckForMissing.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
