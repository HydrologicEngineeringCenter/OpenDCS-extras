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
import java.util.logging.Level;
import java.util.logging.Logger;




/**
 *
 * @author L2EDDMAN
 */
public class BatchComp extends TsdbAppTemplate {

    private StringToken compname = new StringToken("C","Computation to Run","", TokenOptions.optSwitch, "" );
    
    public BatchComp(){
        super("batchcomp.log");
    }
    
    
    @Override
    protected void addCustomArgs( CmdLineArgs cmdLineArgs){
        cmdLineArgs.addToken(compname);
        
    }
    
    
    @Override
    protected void runApp() throws Exception {
        System.out.println( this.compname.getValue() );                             
        DbComputation comp = new DbComputation(null, compname.getValue().trim() ) ;
     
        //comp.
          
        comp.prepareForExec(theDb);
        
        
        
    }
    
    

    public static void main( String args[]){
        BatchComp bc = new BatchComp();
        try {
            bc.execute(args);
        } catch (Exception ex) {
            Logger.getLogger(BatchComp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
