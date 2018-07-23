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
import java.text.ParseException;
import java.util.Date;
import opendcs.dai.TimeSeriesDAI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import spk.db.test.Fixtures;
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
    Fixtures fixtures = null;
    /**
     * Dates of Interest 02/04/2016 17:00:00, 0.12, goes, 02/04/2016 16:37:38,
     * user1 01/28/2016 18:00:00, 0.07, goes, 01/28/2016 17:35:32, user1
     * 07/01/2013 19:00:00, 0.0, goes, 11/06/2013 21:11:47, user2
     */
    Date t1 = null;
    Date t2 = null;
    Date t3 = null;

    public CombineStream_AdvTest() throws ParseException, Exception {

        db = new TestDatabase();
        tsdai = db.makeTimeSeriesDAO();
        fixtures = Fixtures.getFixtures(db);
        
        t1 = Fixtures.sdf.parse("2013-07-01T19:00:00+0000");
        t2 = Fixtures.sdf.parse("2016-01-28T18:00:00+0000");
        t3 = Fixtures.sdf.parse("2016-02-04T17:00:00+0000");

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

        DbComputation comp = new DbComputation(DbKey.NullKey, "CombineStreamTest");

        DbCompParm parm = new DbCompParm("goes", TestDbTimeSeriesDAO.DATA_STAGE_GOES, "15Minute", "Stage.0.GOES-raw", 0);
        comp.addParm(parm);

        parm = new DbCompParm("output", TestDbTimeSeriesDAO.DATA_STAGE_COMB_missing, "15Minute", "Stage.0.Combined-raw_missing", 0);
        comp.addParm(parm);

        comp.setAlgorithmName("CombStream");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);

        for (DbCompParm p : comp.getParmList()) {
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, new Date(2013, 10, 1), new Date(2014, 10, 1));
            dc.addTimeSeries(cts);
        }

        instance = (CombineStream_Adv) comp.getExecutive();
        instance.StationsDir = "classpath:/shared/stations/";

        UnitHelpers.prepForApply(instance, dc);

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
        UnitHelpers.setBaseTime(instance, tv.getTime());
        instance.goes = tv.getDoubleValue();
        instance.ip = Double.NEGATIVE_INFINITY;
        instance.los = Double.NEGATIVE_INFINITY;
        instance.doAWTimeSlice();

        assertEquals("output does not match the GOES value", instance.goes, instance.output.getDoubleValue(), .0001);
    }
    
    @Test
    public void testOnlyIPPresent() throws Exception {
        DataCollection dc = instance.getDataCollection();

        CTimeSeries ts = dc.getTimeSeriesAt(0);
        CTimeSeries ts_out = dc.getTimeSeriesAt(1);

        TimedVariable tv = ts.findWithin(t1, 500);
        UnitHelpers.setBaseTime(instance, tv.getTime());
        instance.ip = tv.getDoubleValue();
        instance.goes = Double.NEGATIVE_INFINITY;
        instance.los = Double.NEGATIVE_INFINITY;
        instance.doAWTimeSlice();

        assertEquals("output does not match the GOES value", instance.ip, instance.output.getDoubleValue(), .0001);
    }
    @Test
    public void testOnlyLOSPresent() throws Exception {
        DataCollection dc = instance.getDataCollection();

        CTimeSeries ts = dc.getTimeSeriesAt(0);
        CTimeSeries ts_out = dc.getTimeSeriesAt(1);

        TimedVariable tv = ts.findWithin(t1, 500);
        UnitHelpers.setBaseTime(instance, tv.getTime());
        instance.los = tv.getDoubleValue();
        instance.ip = Double.NEGATIVE_INFINITY;
        instance.goes = Double.NEGATIVE_INFINITY;
        instance.doAWTimeSlice();

        assertEquals("output does not match the GOES value", instance.los, instance.output.getDoubleValue(), .0001);
    }
    
    

    @Test
    public void testExistingOutputIsSame() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("CombineStream_Adv");
        dbca.setExecClass("spk.algo.CombineStream_Adv");
        DataCollection dc = new DataCollection();

        DbComputation comp = new DbComputation(DbKey.NullKey, "CombineStreamTest");

        DbCompParm parm = new DbCompParm("goes", fixtures.getTimeSeriesKey("TEST.Stage.Inst.15Minutes.0.GOES-raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("output", fixtures.getTimeSeriesKey("TEST.Stage.Inst.15Minutes.0.Combined-raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        comp.setAlgorithmName("CombStream");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);

        for (DbCompParm p : comp.getParmList()) {
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, new Date(2013, 10, 1), new Date(2018, 10, 1));
            dc.addTimeSeries(cts);
        }

        instance = (CombineStream_Adv) comp.getExecutive();
        instance.StationsDir = "classpath:/shared/stations/";

        UnitHelpers.prepForApply(instance, dc);

        dc = instance.getDataCollection();

        CTimeSeries ts = dc.getTimeSeriesAt(0);
        CTimeSeries ts_out = dc.getTimeSeriesAt(1);

        TimedVariable tv = ts.findWithin(t1, 500);
        UnitHelpers.setBaseTime(instance, tv.getTime());
        instance.goes = tv.getDoubleValue();
        instance.ip = Double.NEGATIVE_INFINITY;
        instance.los = Double.NEGATIVE_INFINITY;
        instance.doAWTimeSlice();

        assertFalse("Value was output when it shouldn't have been", instance.output.isChanged());
    }
    
    @Test
    public void testGOESPrimaryIsUsed() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("CombineStream_Adv");
        dbca.setExecClass("spk.algo.CombineStream_Adv");
        DataCollection dc = new DataCollection();

        DbComputation comp = new DbComputation(DbKey.NullKey, "CombineStreamTest");

        DbCompParm parm = new DbCompParm("goes", fixtures.getTimeSeriesKey("TEST-3.Stage.Inst.15Minutes.0.GOES-raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("los", fixtures.getTimeSeriesKey("TEST-3.Stage.Inst.15Minutes.0.LOS-raw"), "15Minutes", null, 0);
        comp.addParm(parm);
        
        parm = new DbCompParm("ip", fixtures.getTimeSeriesKey("TEST-3.Stage.Inst.15Minutes.0.IP-raw"), "15Minutes", null, 0);
        comp.addParm(parm);
        
        parm = new DbCompParm("output", fixtures.getTimeSeriesKey("TEST.Stage.Inst.15Minutes.0.Combined-raw_missing"), "15Minutes", null, 0);
        comp.addParm(parm);

        comp.setAlgorithmName("CombStream");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);

        for (DbCompParm p : comp.getParmList()) {
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, new Date(2013, 10, 1), new Date(2018, 10, 1));
            dc.addTimeSeries(cts);
        }

        instance = (CombineStream_Adv) comp.getExecutive();
        instance.StationsDir = "classpath:/shared/stations/";

        UnitHelpers.prepForApply(instance, dc);
/*
        dc = instance.getDataCollection();

        CTimeSeries ts = dc.getTimeSeriesAt(0);
        CTimeSeries ts_out = dc.getTimeSeriesAt(1);

        TimedVariable tv = ts.findWithin(t1, 500);
        */
        UnitHelpers.setBaseTime(instance, t1);
        instance.goes = 10;
        instance.ip = 20;
        instance.los = 30;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();

        assertEquals("GOES value wasn't used", 10, instance.output.getDoubleValue(), .0001);
    }
    
    @Test
    public void testIPPrimaryIsUsed() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("CombineStream_Adv");
        dbca.setExecClass("spk.algo.CombineStream_Adv");
        DataCollection dc = new DataCollection();

        DbComputation comp = new DbComputation(DbKey.NullKey, "CombineStreamTest");

        DbCompParm parm = new DbCompParm("goes", fixtures.getTimeSeriesKey("TEST-3.Stage.Inst.15Minutes.0.GOES-raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("los", fixtures.getTimeSeriesKey("TEST-3.Stage.Inst.15Minutes.0.LOS-raw"), "15Minutes", null, 0);
        comp.addParm(parm);
        
        parm = new DbCompParm("ip", fixtures.getTimeSeriesKey("TEST-3.Stage.Inst.15Minutes.0.IP-raw"), "15Minutes", null, 0);
        comp.addParm(parm);
        
        parm = new DbCompParm("output", fixtures.getTimeSeriesKey("TEST.Stage.Inst.15Minutes.0.Combined-raw_missing"), "15Minutes", null, 0);
        comp.addParm(parm);

        comp.setAlgorithmName("CombStream");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);

        for (DbCompParm p : comp.getParmList()) {
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, new Date(2013, 10, 1), new Date(2018, 10, 1));
            dc.addTimeSeries(cts);
        }

        instance = (CombineStream_Adv) comp.getExecutive();
        instance.StationsDir = "classpath:/shared/stations/";

        UnitHelpers.prepForApply(instance, dc);
/*
        dc = instance.getDataCollection();

        CTimeSeries ts = dc.getTimeSeriesAt(0);
        CTimeSeries ts_out = dc.getTimeSeriesAt(1);

        TimedVariable tv = ts.findWithin(t1, 500);
        */
        UnitHelpers.setBaseTime(instance, t2);
        instance.goes = 10;
        instance.ip = 20;
        instance.los = 30;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();

        assertEquals("GOES value wasn't used", 20, instance.output.getDoubleValue(), .0001);
    }
    
    @Test
    public void testLOSPrimaryIsUsed() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("CombineStream_Adv");
        dbca.setExecClass("spk.algo.CombineStream_Adv");
        DataCollection dc = new DataCollection();

        DbComputation comp = new DbComputation(DbKey.NullKey, "CombineStreamTest");

        DbCompParm parm = new DbCompParm("goes", fixtures.getTimeSeriesKey("TEST-3.Stage.Inst.15Minutes.0.GOES-raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("los", fixtures.getTimeSeriesKey("TEST-3.Stage.Inst.15Minutes.0.LOS-raw"), "15Minutes", null, 0);
        comp.addParm(parm);
        
        parm = new DbCompParm("ip", fixtures.getTimeSeriesKey("TEST-3.Stage.Inst.15Minutes.0.IP-raw"), "15Minutes", null, 0);
        comp.addParm(parm);
        
        parm = new DbCompParm("output", fixtures.getTimeSeriesKey("TEST.Stage.Inst.15Minutes.0.Combined-raw_missing"), "15Minutes", null, 0);
        comp.addParm(parm);

        comp.setAlgorithmName("CombStream");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);

        for (DbCompParm p : comp.getParmList()) {
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, new Date(2013, 10, 1), new Date(2018, 10, 1));
            dc.addTimeSeries(cts);
        }

        instance = (CombineStream_Adv) comp.getExecutive();
        instance.StationsDir = "classpath:/shared/stations/";

        UnitHelpers.prepForApply(instance, dc);
/*
        dc = instance.getDataCollection();

        CTimeSeries ts = dc.getTimeSeriesAt(0);
        CTimeSeries ts_out = dc.getTimeSeriesAt(1);

        TimedVariable tv = ts.findWithin(t1, 500);
        */
        UnitHelpers.setBaseTime(instance, t3);
        instance.goes = 10;
        instance.ip = 20;
        instance.los = 30;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();

        assertEquals("GOES value wasn't used", 30, instance.output.getDoubleValue(), .0001);
    }

}
