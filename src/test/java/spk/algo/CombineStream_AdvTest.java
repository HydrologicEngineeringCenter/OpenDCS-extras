/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;


import decodes.sql.DbKey;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.ParmRef;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import spk.db.test.TestDatabase;
import spk.db.test.TestDbTimeSeriesDAO;
/**
 *
 * @author L2EDDMAN
 */
public class CombineStream_AdvTest {
    
    CombineStream_Adv instance = null;
    TestDatabase db = null;
    public CombineStream_AdvTest() {
        db = new TestDatabase();
    }
    
    @BeforeClass
    public static void setUpClass() {
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("CombineStream_Adv");
        dbca.setExecClass("spk.algo.CombineStream_Adv");        
        
        DbComputation comp = new DbComputation(DbKey.NullKey,"CombineStreamTest");        
        DbCompParm parm = new DbCompParm("goes",TestDbTimeSeriesDAO.DATA_STAGE_GOES,"15Minute","Stage.0.GOES-raw",0);
        comp.addParm(parm);
        comp.setAlgorithmName("TrapIntegration");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);
        instance = (CombineStream_Adv) comp.getExecutive();
        instance.prepForApply(new DataCollection());
        ParmRef parmRef = instance.getParmRef("goes");
      
        
        
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of initAWAlgorithm method, of class CombineStream_Adv.
     */
    @Test
    public void testInitAWAlgorithm() throws Exception {
    }

    /**
     * Test of beforeTimeSlices method, of class CombineStream_Adv.
     */
    @Test
    public void testBeforeTimeSlices() throws Exception {        
        instance.beforeTimeSlices();
    }

    /**
     * Test of doAWTimeSlice method, of class CombineStream_Adv.
     */
    @Test
    public void testDoAWTimeSlice() throws Exception {
    }

    /**
     * Test of afterTimeSlices method, of class CombineStream_Adv.
     */
    @Test
    public void testAfterTimeSlices() throws Exception {
    }    

    /**
     * Test of getPZF method, of class CombineStream_Adv.
     */
    @Test
    public void testGetPZF() {
    }
    
}
