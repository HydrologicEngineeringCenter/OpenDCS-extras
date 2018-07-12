/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;


import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.ParmRef;
import ilex.var.TimedVariable;
import java.util.Date;
import opendcs.dai.TimeSeriesDAI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import spk.db.test.TestDatabase;
import spk.db.test.TestDbTimeSeriesDAO;
import spk.db.test.UnitHelpers;
/**
 *
 * @author L2EDDMAN
 */
public class CombineStream_AdvTest {
    
    CombineStream_Adv instance = null;
    TestDatabase db = null;
    TimeSeriesDAI tsdai = null;
    
    /**
     * Dates of Interest 02/04/2016 17:00:00, 0.12, goes, 02/04/2016 16:37:38,
     * user1 01/28/2016 18:00:00, 0.07, goes, 01/28/2016 17:35:32, user1
     * 07/01/2013 19:00:00, 0.0, goes, 11/06/2013 21:11:47, user2
     */
    Date t1 = null;
    Date t2 = null;
    Date t3 = null;
    
    public CombineStream_AdvTest() {
        db = new TestDatabase();
        tsdai = db.makeTimeSeriesDAO();
        t1 = new Date(2013 - 1900, 6, 1, 19, 0, 0);
        t2 = new Date(2016 - 1900, 0, 28, 18, 0, 0);
        t3 = new Date(2016 - 1900, 1, 4, 17, 0, 0);
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
        DataCollection dc = new DataCollection();
        
        DbComputation comp = new DbComputation(DbKey.NullKey,"CombineStreamTest");
        
        DbCompParm parm = new DbCompParm("goes",TestDbTimeSeriesDAO.DATA_STAGE_GOES,"15Minute","Stage.0.GOES-raw",0);
        comp.addParm(parm);
        
        parm = new DbCompParm("output",TestDbTimeSeriesDAO.DATA_STAGE_COMB_missing,"15Minute","Stage.0.Combined-val_missing",0);
        comp.addParm(parm);
        
        comp.setAlgorithmName("CombStream");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);
        
        for(DbCompParm p: comp.getParmList()){
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, new Date(2013, 10, 1), new Date(2014,10,1));
            dc.addTimeSeries(cts);
        }
        
        
        instance = (CombineStream_Adv) comp.getExecutive();
        instance.StationsDir="classpath:/shared/stations/";
        //instance.prepForApply(dc);
        UnitHelpers.prepForApply(instance, dc);
        ParmRef parmRef = instance.getParmRef("output");
      
        
        
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
    public void testOnlyGOESPresent() throws Exception {
        DataCollection dc = instance.getDataCollection();

        CTimeSeries ts = dc.getTimeSeriesAt(0);
        CTimeSeries ts_out = dc.getTimeSeriesAt(1);

        TimedVariable tv = ts.findWithin(t1, 500);
        instance.setTimeSliceBaseTime(tv.getTime());
        instance.goes = tv.getDoubleValue();
        instance.ip = Double.NEGATIVE_INFINITY;
        instance.los = Double.NEGATIVE_INFINITY;
        instance.doAWTimeSlice();

        assertEquals( "output does not match the GOES value", instance.goes, instance.output.getDoubleValue(), .0001 );
        
        
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
