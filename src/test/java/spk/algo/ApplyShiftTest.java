/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

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

    /**
     * Dates of Interest 02/04/2016 17:00:00, 0.12, goes, 02/04/2016 16:37:38,
     * user1 01/28/2016 18:00:00, 0.07, goes, 01/28/2016 17:35:32, user1
     * 07/01/2013 19:00:00, 0.0, goes, 11/06/2013 21:11:47, user2
     */
    Date t1 = null;
    Date t2 = null;
    Date t3 = null;

    public ApplyShiftTest() {
        db = new TestDatabase();
        tsdai = db.makeTimeSeriesDAO();

        t1 = new Date(2013 - 1900, 6, 1, 19, 0, 0);
        t2 = new Date(2016 - 1900, 0, 28, 18, 0, 0);
        t3 = new Date(2016 - 1900, 1, 4, 17, 0, 0);
    }

    @Before
    public void setUp() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("ApplyShift");
        dbca.setExecClass("spk.algo.ApplyShift");
        DataCollection dc = new DataCollection();
        DbComputation comp = new DbComputation(DbKey.NullKey, "ApplyShiftTest");

        DbCompParm parm = new DbCompParm("input", TestDbTimeSeriesDAO.DATA_STAGE_GOES, "15Minute", "Stage.0.GOES-raw", 0);
        comp.addParm(parm);

        parm = new DbCompParm("output", TestDbTimeSeriesDAO.DATA_STAGE_COMB, "15Minute", "Stage.0.Combined-raw", 0);
        comp.addParm(parm);

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
        instance.setTimeSliceBaseTime(tv.getTime());
        instance.input = tv.getDoubleValue();
        instance.doAWTimeSlice();

        TimedVariable tv_out = new TimedVariable(tv);
        tv.setValue(instance.output);
        ts_out.addSample(tv_out);

        TimedVariable in = ts.findWithin(t1, 500);
        TimedVariable out = ts_out.findWithin(t1, 500);

        assertEquals("input and output don't match correctly", in.getDoubleValue(), 40, .0001);

    }


}
