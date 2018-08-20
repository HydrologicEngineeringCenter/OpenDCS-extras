/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.apps.compgraph;
import decodes.cwms.CwmsGroupHelper;
import decodes.cwms.CwmsTimeSeriesDb;
import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.CompAppInfo;
import decodes.tsdb.DataCollection;
import decodes.tsdb.TsdbAppTemplate;
import decodes.tsdb.DbComputation;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbCompResolver;
import decodes.tsdb.TimeSeriesIdentifier;
import decodes.tsdb.TsGroup;
import decodes.tsdb.VarFlags;
import decodes.util.CmdLineArgs;
import ilex.cmdline.StringToken;
import ilex.cmdline.TokenOptions;
import ilex.var.TimedVariable;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import lrgs.gui.DecodesInterface;
import opendcs.dai.CompDependsDAI;
import opendcs.dai.ComputationDAI;
import opendcs.dai.LoadingAppDAI;
import opendcs.dai.TimeSeriesDAI;
import opendcs.dai.TsGroupDAI;
import org.apache.commons.lang.xwork.StringUtils;




/**
 * Runs a simple query to show the status of the system
 * @author L2EDDMAN
 */
public class BuildCompGraph extends TsdbAppTemplate {

    //private StringToken compname = new StringToken("C","Computation to Run","", TokenOptions.optSwitch, "" );
    private StringToken group = new StringToken("G","Time Series Group","",TokenOptions.optSwitch|TokenOptions.optRequired,"");
    
    private static final String compquery = "select comp.computation_id,comp.computation_name,comp.algorithm_id,algo.algorithm_name,ALGOPARM.ALGO_ROLE_NAME, COMPPARM.SITE_DATATYPE_ID FROM ccp.cp_computation comp \n" +
"    INNER JOIN ccp.cp_algorithm algo \n" +
"    ON comp.algorithm_id = algo.algorithm_id \n" +
"    INNER JOIN CCP.CP_ALGO_TS_PARM algoparm     \n" +
"    ON algo.algorithm_id = ALGOPARM.ALGORITHM_ID and ALGOPARM.PARM_TYPE='o'\n" +
"    INNER JOIN CCP.CP_COMP_TS_PARM compparm\n" +
"    ON ALGOPARM.ALGO_ROLE_NAME = COMPPARM.ALGO_ROLE_NAME";
    
    public BuildCompGraph(){
        super("compgraph.log");
     
    }
    
    @Override
    public void initDecodes(){
        //skip this
        
    }
    
    @Override
    protected void addCustomArgs( CmdLineArgs cmdLineArgs){
        
        cmdLineArgs.addToken( group );
        appNameArg.setDefaultValue("compgraph");
    }
    
    
    @Override
    protected void runApp() throws Exception {
    
        DecodesInterface.initDecodesMinimal(cmdLineArgs.getPropertiesFile());
       
        Graph graph = new Graph();
        
        
        
        
        
        ComputationDAI compDAO = theDb.makeComputationDAO();
        TsGroupDAI groupDAO = theDb.makeTsGroupDAO();
        TimeSeriesDAI tsDAO = theDb.makeTimeSeriesDAO();
       
    
              
        TsGroup my_group = groupDAO.getTsGroupByName(group.getValue());
        CwmsGroupHelper helper = new CwmsGroupHelper((CwmsTimeSeriesDb) theDb);
        helper.expandTsGroup(my_group);
        //helper.expandTsGroupDescriptors(my_group); // expand the group
        ArrayList<TimeSeriesIdentifier> expandedList = my_group.getExpandedList();
        DataCollection dc = new DataCollection();
        LoadingAppDAI loadingappDAO = theDb.makeLoadingAppDAO();
        ArrayList<CompAppInfo> compapps = loadingappDAO.listComputationApps(true);
        
        
        for( TimeSeriesIdentifier tsout: expandedList ){
            CTimeSeries cts = theDb.makeTimeSeries(tsout);
            // doesn't matter what the value is, we're just tricking the system into processing the data
            // and it needs a value with the DB_ADDED flag set
            TimedVariable tv = new TimedVariable(1);
            tv.setTime( new Date() );
            tv.setFlags( VarFlags.DB_ADDED );
            cts.addSample(tv);
            dc.addTimeSeries(cts);          
            
            // we want ALL of the dependencies in this app
            String q = "select computation_id from ccp.cp_comp_depends where ts_id = " + tsout.getKey();
            ResultSet rs = theDb.doQuery(q);
            while(rs!=null && rs.next() ){
                cts.addDependentCompId( DbKey.createDbKey(rs, 1) );
            }            
            rs.close();
            
            /*
            DbComputation comp = outlist.findCompByOutput(tsout.getKey());
            if( comp != null ){                
                System.out.println("Processing comp: " + comp.getName());
                System.out.println("\tOutput: " + tsout.getUniqueString());
                // already have concrete comp just add the elements
                DbKey target = tsout.getKey();
                GraphNode node = new GraphNode(target, tsout.getUniqueString(), comp);
                nodes.add( node );
                Iterator<DbCompParm> parms = comp.getParms();
                while( parms !=null && parms.hasNext() ){
                    DbCompParm parm = parms.next();
                    
                    if( parm.isInput() ){ // we already have the output, don't need to do it again.
                        System.out.print("\tInput: " + parm.getRoleName() + "/");
                        DbKey source = parm.getSiteDataTypeId();
                        if( source.getValue() != DbKey.NullKey.getValue() ){
                            TimeSeriesIdentifier tsin = tsDAO.getTimeSeriesIdentifier(source);
                            System.out.println(tsin.getUniqueString() );
                            nodes.add( new GraphNode(source,tsin.getUniqueString(),null));// need to actually search first
                            edges.add( new GraphEdge(source,target));
                            
                        } else{
                            System.out.println("");
                        }
                    }
                }
                
            } else{
                //go through the comps and attempt to create each.
                
                
            }
            */
        }
        
        DbCompResolver resolver = new DbCompResolver(theDb);
        DbComputation comps[] = resolver.resolve(dc);
        for( DbComputation comp: comps ){
            System.out.println(comp.getName() );
            Iterator<DbCompParm> parms = comp.getParms();
            String compid="COMP"+comp.getId();
            // do this twice, because we need a unique ID for the comps
            // so combine the inputs
            while( parms !=null && parms.hasNext()){
                DbCompParm parm = parms.next();
                if( parm.isInput() && !parm.getSiteDataTypeId().isNull() ){
                    compid = compid + "_" + parm.getSiteDataTypeId();
                }
            }
            parms = comp.getParms();
            while( parms !=null && parms.hasNext()){
                DbCompParm parm =parms.next();
                
                if( parm.isOutput()){
                    
                    DbKey target = parm.getSiteDataTypeId();
                    
                    System.out.print("\tOutput: " + parm.getRoleName() +"/");
                    if( !target.isNull()) {
                        String target_id = "TS"+target;
                        TimeSeriesIdentifier ts = tsDAO.getTimeSeriesIdentifier(target);
                        System.out.println( ts.getUniqueString() );
                        graph.addNode(new GraphNode(target_id, ts.getUniqueString(),"TS", ts.getPart("param"),null));
                        graph.addNode( new GraphNode( compid, comp.getName(), "COMP","comp", comp ));                        
                        /*
                        TODO: may need to figure out a way to deal with input vs output properties 
                        on edges.
                        
                        */
                        graph.addEdge( new GraphEdge( compid,target_id, "\"extra\": {}" ) ); 
                    }
                }else {

                    System.out.print("\tInput: " + parm.getRoleName() + "/");
                    DbKey source = parm.getSiteDataTypeId();
                    if( source.getValue() != DbKey.NullKey.getValue() ){
                        TimeSeriesIdentifier tsin = tsDAO.getTimeSeriesIdentifier(source);
                        
                        String source_id = "TS"+source;
                        System.out.println(tsin.getUniqueString() );
                        graph.addNode( new GraphNode( compid, comp.getName(), "COMP", "comp",comp));
                        graph.addNode( new GraphNode( source_id,tsin.getUniqueString(),"TS", tsin.getPart("param"),null));
                        
                        // find this edges properties.
                        String rolename = parm.getRoleName();
                        ArrayList<String> proplist = new ArrayList<String>();
                        Properties properties = comp.getProperties();
                        for( Object key: properties.keySet()){
                                String prop = (String)key;
                                if( prop.contains(rolename)){
                                    proplist.add( String.format("\"%s\": \"%s\"",prop,properties.getProperty(prop)) );
                                }
                        }
                        String data = "";
                        if( proplist.size() > 0 ){
                            data = "\"extra\": { \"properties\": {" + StringUtils.join(proplist, ",\r\n") + "}\r\n}\r\n";
                        } else{
                            data = "\"extra\": {}";                                    
                        }
                        // special case for basin precip until names fixed
                        
                        graph.addEdge( new GraphEdge( source_id,compid, data  ) );
                    } else{
                        System.out.println(" ");
                    }

     
     
                }
            }

                        
        }
        
        //System.out.println("[");
        graph.printgraph();
        
        compDAO.close();
        tsDAO.close();
        groupDAO.close();
        
        
        
    }
    
    

    public static void main( String args[]){
        BuildCompGraph byapp = new BuildCompGraph();
        
        System.setProperty("DCSTOOL_HOME", "C:\\cwms\\");
        try {
            DecodesInterface.setGUI(false);
            
            byapp.execute(args);
        } catch (Exception ex) {
            Logger.getLogger(BuildCompGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
