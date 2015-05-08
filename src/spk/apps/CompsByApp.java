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
 *
 * @author L2EDDMAN
 */
public class CompsByApp extends TsdbAppTemplate {

    //private StringToken compname = new StringToken("C","Computation to Run","", TokenOptions.optSwitch, "" );
    
    public CompsByApp(){
        super("compsbyapp.log");
     
    }
    
    @Override
    public void initDecodes(){
        //skip this
        
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
        CompsByApp byapp = new CompsByApp();
        try {
            DecodesInterface.setGUI(false);
            
            byapp.execute(args);
        } catch (Exception ex) {
            Logger.getLogger(CompsByApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
