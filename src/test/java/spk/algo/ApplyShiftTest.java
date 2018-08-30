/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.algo;

import decodes.db.Database;
import decodes.db.TestDatabaseIO;
import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.DbIoException;
import decodes.tsdb.DuplicateTimeSeriesException;
import decodes.tsdb.ParmRef;
import decodes.tsdb.TimeSeriesDb;
import ilex.var.TimedVariable;
import java.lang.reflect.Field;
import java.util.Date;
import opendcs.dai.TimeSeriesDAI;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import spk.db.test.Fixtures;
import spk.db.test.TestDatabase;
import spk.db.test.TestDbTimeSeriesDAO;
import spk.db.test.UnitHelpers;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class ApplyShiftTest {

    ApplyShift instance = null;
    TimeSeriesDb db = null;
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

    public ApplyShiftTest() throws Exception {
        db = new TestDatabase();
        tsdai = db.makeTimeSeriesDAO();
        fixtures = Fixtures.getFixtures(db);
        t1 = new Date(2013 - 1900, 6, 1, 19, 0, 0);
        t2 = new Date(2016 - 1900, 0, 28, 18, 0, 0);
        t3 = new Date(2016 - 1900, 1, 4, 17, 0, 0);
         Database _db = new Database();
        _db.setDbIo(new TestDatabaseIO("classpath:/decodes/xml"));
        _db.read();
        _db.unitConverterSet.prepareForExec();
        //_db.prepareForExec();
        
    }

    @Before
    public void setUp() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("ApplyShift");
        dbca.setExecClass("spk.algo.ApplyShift");
        DataCollection dc = new DataCollection();
        DbComputation comp = new DbComputation(DbKey.NullKey, "ApplyShiftTest");

        DbCompParm parm = new DbCompParm("input", TestDbTimeSeriesDAO.DATA_STAGE_GOES, "15Minutes", "Stage.0.GOES-raw", 0);
        comp.addParm(parm);

        parm = new DbCompParm("output", TestDbTimeSeriesDAO.DATA_STAGE_COMB, "15Minutes", "Stage.0.Combined-raw", 0);
        comp.addParm(parm);

        comp.setAlgorithmName("ApplyShift");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);
        /*
        for (DbCompParm p : comp.getParmList()) {
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, new Date(2013, 1, 1), new Date(2014, 10, 1));
            dc.addTimeSeries(cts);
        }
        */
        dc = UnitHelpers.getCompData(comp, tsdai, new Date(2013-1900, 1, 1), new Date(2014-1900, 10, 1));
        instance = (ApplyShift) comp.getExecutive();
        instance.ShiftsDir = "classpath:/shared/stations/";
        //instance.prepForApply(dc);
        UnitHelpers.prepForApply(instance, dc);
    }

    /**
     * Test of initAWAlgorithm method, of class ApplyShift.
     */
    @Test
    public void testInitAWAlgorithm() throws Exception {
    }

    /**
     * Test of beforeTimeSlices method, of class ApplyShift.
     */
    @Test
    public void testBeforeTimeSlices() throws Exception {
        instance.beforeTimeSlices();
    }

    /**
     * Test of doAWTimeSlice method, of class ApplyShift.
     */
    @Test
    public void testDoAWTimeSlice() throws Exception {
        instance.beforeTimeSlices();

        DataCollection dc = instance.getDataCollection();

        CTimeSeries ts = dc.getTimeSeriesAt(0);
        CTimeSeries ts_out = dc.getTimeSeriesAt(1);

        TimedVariable tv = ts.findWithin(t1, 500);

        /*Class<?> clz = instance.getClass().getSuperclass();
         Field fields[] = clz.getDeclaredFields();
         Field tsbt = clz.getDeclaredField("_timeSliceBaseTime");
         tsbt.setAccessible(true);
         tsbt.set(instance, tv.getTime() );
         */
        UnitHelpers.setBaseTime(instance, tv.getTime());

        //instance.setTimeSliceBaseTime(tv.getTime());
        instance.input = tv.getDoubleValue();
        instance.doAWTimeSlice();

        TimedVariable tv_out = new TimedVariable(tv);
        tv.setValue(instance.output);
        ts_out.addSample(tv_out);

        TimedVariable in = ts.findWithin(t1, 500);
        TimedVariable out = ts_out.findWithin(t1, 500);

        assertEquals("input and output don't match correctly", in.getDoubleValue(), out.getDoubleValue(), .0001);

    }

    @Test
    public void testMinimumValue() throws Exception {
        instance.beforeTimeSlices();
        instance.MinimumValue = 40.0;
        DataCollection dc = instance.getDataCollection();

        CTimeSeries ts = dc.getTimeSeriesAt(0);
        CTimeSeries ts_out = dc.getTimeSeriesAt(1);

        TimedVariable tv = ts.findWithin(t1, 500);
        UnitHelpers.setBaseTime(instance, tv.getTime());

        instance.input = tv.getDoubleValue();
        instance.doAWTimeSlice();

        TimedVariable tv_out = new TimedVariable(tv);
        tv.setValue(instance.output);
        ts_out.addSample(tv_out);

        TimedVariable in = ts.findWithin(t1, 500);
        TimedVariable out = ts_out.findWithin(t1, 500);

        assertEquals("input and output don't match correctly", in.getDoubleValue(), 40, .0001);

    }

    @Test
    public void testGetPZF_units_not_set() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("ApplyShift");
        dbca.setExecClass("spk.algo.ApplyShift");
        DataCollection dc = new DataCollection();
        DbComputation comp = new DbComputation(DbKey.NullKey, "ApplyShiftTest");

        DbCompParm parm = new DbCompParm("input", fixtures.getTimeSeriesKey("TEST-2.Stage.Inst.15Minutes.0.raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("output", fixtures.getTimeSeriesKey("TEST.Stage.Inst.15Minutes.0.calc"), "15Minutes", null, 0);
        comp.addParm(parm);
        //comp.setProperty("input_EU", "m");
        comp.setAlgorithmName("ApplyShift");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);

        for (DbCompParm p : comp.getParmList()) {
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, new Date(2013, 10, 1), new Date(2014, 10, 1));
            dc.addTimeSeries(cts);
        }

        instance = (ApplyShift) comp.getExecutive();
        instance.ShiftsDir = "classpath:/shared/stations/";
        //instance.prepForApply(dc);
        UnitHelpers.prepForApply(instance, dc);

        //Date dt = df.parse("06/26/2018 23:00:00+0000");
        Date dt = Fixtures.sdf.parse("2018-06-26T23:00:00+0000");
        UnitHelpers.setBaseTime(instance, dt);
        instance.input = 1;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertEquals("PZF value was not used", 1.81, instance.output.getDoubleValue(), .0001);
        instance.input = 2;
        instance.doAWTimeSlice();

        // 2ft in meters = 0.6096
        assertEquals("PZZ value was used", 2, instance.output.getDoubleValue(), .0001);
    }

    @Test
    public void testGetPZF_with_units() throws Exception{

        DbCompAlgorithm dbca = new DbCompAlgorithm("ApplyShift");
        dbca.setExecClass("spk.algo.ApplyShift");
        DataCollection dc = new DataCollection();
        DbComputation comp = new DbComputation(DbKey.NullKey, "ApplyShiftTest");

        DbCompParm parm = new DbCompParm("input", fixtures.getTimeSeriesKey("TEST-2.Stage.Inst.15Minutes.0.raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("output", fixtures.getTimeSeriesKey("TEST.Stage.Inst.15Minutes.0.calc"), "15Minutes", null, 0);
        comp.addParm(parm);
        comp.setProperty("input_EU", "m");
        comp.setProperty("output_EU", "m");
        comp.setAlgorithmName("ApplyShift");
        comp.setAlgorithm(dbca);
        comp.prepareForExec(db);

        for (DbCompParm p : comp.getParmList()) {
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, new Date(2013, 10, 1), new Date(2014, 10, 1));
            dc.addTimeSeries(cts);
        }

        instance = (ApplyShift) comp.getExecutive();
        instance.ShiftsDir = "classpath:/shared/stations/";
        //instance.prepForApply(dc);
        UnitHelpers.prepForApply(instance, dc);

        //Date dt = df.parse("06/26/2018 23:00:00+0000");
        Date dt = Fixtures.sdf.parse("2018-07-17T00:00:00+0000");
        UnitHelpers.setBaseTime(instance, dt);
        instance.input = .5;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();

        // 2ft in meters = 0.6096
        assertEquals("PZF value was not used", .6096, instance.output.getDoubleValue(), .0001);

    }

}
