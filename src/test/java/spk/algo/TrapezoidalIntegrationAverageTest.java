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
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import ilex.var.TimedVariable;
import java.util.ArrayList;
import java.util.Date;
import opendcs.dai.TimeSeriesDAI;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import opendcs.db.test.Fixtures;
import opendcs.db.test.TestDatabase;
import opendcs.db.test.TestDbTimeSeriesDAO;
import opendcs.db.test.UnitHelpers;

/**
 *
 * @author L2EDDMAN
 */
public class TrapezoidalIntegrationAverageTest {

    TrapezoidalIntegrationAverage instance = null;
    TestDatabase db = null;
    TimeSeriesDAI tsdai = null;
    Fixtures fixtures = null;
    Date start = null;
    Date end = null;

    public TrapezoidalIntegrationAverageTest() throws Exception {
        db = new TestDatabase();
        tsdai = db.makeTimeSeriesDAO();
        fixtures = Fixtures.getFixtures(db);
        start = Fixtures.sdf.parse("2013-01-01T00:00:00+0000");
        end = Fixtures.sdf.parse("2019-10-01T00:00:00+0000");
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("TrapezoidalIntegrationAverage");
        dbca.setExecClass("spk.algo.TrapezoidalIntegrationAverage");

        DbComputation comp = new DbComputation(DbKey.NullKey, "TrapIntTest");
        comp.setAlgorithmName("TrapIntegration");
        comp.setAlgorithm(dbca);

        DbCompParm parm = new DbCompParm("input", fixtures.getTimeSeriesKey("TEST.Flow.Inst.15Minutes.0.raw"), "15Minutes", "Flow.Inst.0.raw", 0);
        comp.addParm(parm);

        parm = new DbCompParm("average", fixtures.getTimeSeriesKey("TEST.Flow.Ave.1Hour.1Hour.calc"), "1Hour", "Flow.Ave..1Hour.calc", 0);
        comp.addParm(parm);

        comp.prepareForExec(db);
        DataCollection dc = UnitHelpers.getCompData(comp, tsdai, start, end);

        instance = (TrapezoidalIntegrationAverage) comp.getExecutive();
        UnitHelpers.prepForApply(instance, dc);
        System.out.println("for debugger haltpoint");
        //instance.init(comp, db);

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of initAWAlgorithm method, of class TrapezoidalIntegrationAverage.
     */
    @Test
    public void testInitAWAlgorithm() throws Exception {
    }

    /**
     * Test of beforeTimeSlices method, of class TrapezoidalIntegrationAverage.
     */
    @Test
    public void testBeforeTimeSlices() throws Exception {
        instance.beforeTimeSlices();
        assertEquals(0, instance.count);
    }

    /**
     * Test of doAWTimeSlice method, of class TrapezoidalIntegrationAverage.
     */
    @Test
    public void testDoAWTimeSlice() throws Exception {
        instance.input = 1.0;
        instance.beforeTimeSlices();
        instance.setTimeSliceBaseTime(new Date(0));
        instance.doAWTimeSlice();
        assertEquals(1, instance.count);
    }

    /**
     * Test of afterTimeSlices method, of class TrapezoidalIntegrationAverage.
     */
    @Test
    public void testAfterTimeSlices() throws Exception {
        instance.beforeTimeSlices();
        // loop
        instance.input = 10;

        UnitHelpers.setBaseTime(instance, new Date(0));
        instance.setTimeSliceBaseTime(new Date(0));
        instance.doAWTimeSlice();
        instance.setTimeSliceBaseTime(new Date(900 * 1000));
        instance.doAWTimeSlice();
        instance.afterTimeSlices();
        assertEquals(10, instance.average.getDoubleValue(), 0.0001);
    }

    /**
     * Test averaging
     */
    @Test
    public void testFlatAverage() throws Exception {
        DataCollection dc = instance.getDataCollection();
        ArrayList<Date> times = new ArrayList<>();
        times.add(Fixtures.sdf.parse("2018-07-10T00:00:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T00:15:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T00:30:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T00:45:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T01:00:00+0000"));
        String tsname = instance.getParmTsUniqueString("input");
        //DbKey key = instance.get
        CTimeSeries cts = dc.getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(tsname));

        instance.beforeTimeSlices();
        for (Date dt : times) {
            UnitHelpers.setBaseTime(instance, dt);
            TimedVariable tv = cts.findWithin(dt, 300);
            instance.input = tv.getDoubleValue();
            instance.doAWTimeSlice();
        }
        instance.afterTimeSlices();

        assertEquals("Output varied, should be flat", 10.0, instance.average.getDoubleValue(), .0001);

    }

    @Test
    public void testRisingAverage() throws Exception {
        DataCollection dc = instance.getDataCollection();
        ArrayList<Date> times = new ArrayList<>();
        times.add(Fixtures.sdf.parse("2018-07-10T01:00:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T01:15:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T01:30:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T01:45:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T02:00:00+0000"));
        String tsname = instance.getParmTsUniqueString("input");
        //DbKey key = instance.get
        CTimeSeries cts = dc.getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(tsname));

        instance.beforeTimeSlices();
        for (Date dt : times) {
            UnitHelpers.setBaseTime(instance, dt);
            TimedVariable tv = cts.findWithin(dt, 300);
            instance.input = tv.getDoubleValue();
            instance.doAWTimeSlice();
        }
        instance.afterTimeSlices();

        assertEquals("Output varied, should be flat", 30.0, instance.average.getDoubleValue(), .0001);

    }

    @Test
    public void testFallingAverage() throws Exception {
        DataCollection dc = instance.getDataCollection();
        ArrayList<Date> times = new ArrayList<>();
        times.add(Fixtures.sdf.parse("2018-07-10T02:00:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T02:15:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T02:30:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T02:45:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T03:00:00+0000"));
        String tsname = instance.getParmTsUniqueString("input");
        //DbKey key = instance.get
        CTimeSeries cts = dc.getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(tsname));

        instance.beforeTimeSlices();
        for (Date dt : times) {
            UnitHelpers.setBaseTime(instance, dt);
            TimedVariable tv = cts.findWithin(dt, 300);
            instance.input = tv.getDoubleValue();
            instance.doAWTimeSlice();
        }
        instance.afterTimeSlices();

        assertEquals("Output varied, should be flat", 30.0, instance.average.getDoubleValue(), .0001);

    }

    @Test
    public void testMissingValues_NoOutput() throws Exception {
        instance.minSamplesNeeded = 5;
        DataCollection dc = instance.getDataCollection();
        ArrayList<Date> times = new ArrayList<>();
        times.add(Fixtures.sdf.parse("2018-07-10T03:00:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T03:15:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T03:30:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T03:45:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T04:00:00+0000"));
        String tsname = instance.getParmTsUniqueString("input");
        //DbKey key = instance.get
        CTimeSeries cts = dc.getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(tsname));

        instance.beforeTimeSlices();
        for (Date dt : times) {
            UnitHelpers.setBaseTime(instance, dt);
            TimedVariable tv = cts.findWithin(dt, 300);
            instance.input = tv.getDoubleValue();
            instance.doAWTimeSlice();
        }
        instance.afterTimeSlices();
        assertFalse("output was changed", instance.average.isChanged());

    }

    @Test
    public void testMissingValues_Output() throws Exception {
        instance.minSamplesNeeded = 3;
        DataCollection dc = instance.getDataCollection();
        ArrayList<Date> times = new ArrayList<>();
        times.add(Fixtures.sdf.parse("2018-07-10T03:00:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T03:15:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T03:30:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T03:45:00+0000"));
        times.add(Fixtures.sdf.parse("2018-07-10T04:00:00+0000"));
        String tsname = instance.getParmTsUniqueString("input");
        //DbKey key = instance.get
        CTimeSeries cts = dc.getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(tsname));

        instance.beforeTimeSlices();
        for (Date dt : times) {
            UnitHelpers.setBaseTime(instance, dt);
            TimedVariable tv = cts.findWithin(dt, 300);
            instance.input = tv.getDoubleValue();
            instance.doAWTimeSlice();
        }
        instance.afterTimeSlices();

        assertEquals("Output varied, should be flat", 10.0, instance.average.getDoubleValue(), .0001);

    }

}
