/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;

import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.TimeSeriesDb;
import java.text.ParseException;
import java.util.Date;
import opendcs.dai.TimeSeriesDAI;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import spk.db.test.Fixtures;
import spk.db.test.TestDatabase;
import spk.db.test.UnitHelpers;

/**
 *
 * @author L2EDDMAN
 */
public class EliminateNegativePrecipTest {

    TimeSeriesDb db = null;
    TimeSeriesDAI tsdai = null;
    Fixtures fixtures = null;

    EliminateNegativePrecip instance = null;
    DbComputation comp = null;
    Date start = null;
    Date before_dip = null;
    Date dip = null;
    Date missing = null;
    Date normal_day = null;
    Date missing2 = null;
    Date before_spike = null;
    Date spike = null;
    Date reset = null;
    Date end = null;

    public EliminateNegativePrecipTest() throws ParseException, Exception {
        db = new TestDatabase();
        tsdai = db.makeTimeSeriesDAO();
        fixtures = Fixtures.getFixtures(db);
        start = Fixtures.sdf.parse("2018-07-10T00:00:00+0000");
        missing = Fixtures.sdf.parse("2018-07-10T00:15:00+0000");
        missing2 = Fixtures.sdf.parse("2018-07-10T00:30:00+0000");
        normal_day = Fixtures.sdf.parse("2018-07-10T01:15:00+0000");

        before_dip = Fixtures.sdf.parse("2018-07-10T05:45:00+0000");
        dip = Fixtures.sdf.parse("2018-07-10T06:00:00+0000");

        before_spike = Fixtures.sdf.parse("2018-07-10T06:45:00+0000");
        spike = Fixtures.sdf.parse("2018-07-10T07:00:00+0000");
        reset = Fixtures.sdf.parse("2018-07-10T12:00:00+0000");
        end = Fixtures.sdf.parse("2018-07-11T00:00:00+0000");
    }

    @Before
    public void setUp() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("EliminateNegativePrecip");
        dbca.setExecClass("spk.algo.EliminateNegativePrecip");
        comp = new DbComputation(DbKey.NullKey, "ElimNegPrecipTest");
        comp.setAlgorithm(dbca);

        comp.setProperty("resetDate", "10Jul2018 1200");
        comp.setProperty("aggregateTimeZone", "");

        DbCompParm parm = new DbCompParm("BadRain", fixtures.getTimeSeriesKey("TEST.Precip-Cum.Inst.15Minutes.0.raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("FixedRain", fixtures.getTimeSeriesKey("TEST.Precip-Cum.Inst.15Minutes.0.calc"), "15Minutes", null, 0);
        comp.addParm(parm);

        comp.prepareForExec(db);
        instance = (EliminateNegativePrecip) comp.getExecutive();
        DataCollection dc = UnitHelpers.getCompData(comp, tsdai, start, end);

        UnitHelpers.prepForApply(instance, dc);

    }

    @Test
    public void testInitAlgorithm() throws Exception {
        instance.initAWAlgorithm();
    }

    @Test
    public void testGetPrevRain() throws Exception {
        instance.initAWAlgorithm();
        instance.BadRain = Double.NEGATIVE_INFINITY;
        double val = instance.getPrevValue("FixedRain", missing);
        DataCollection dc = instance.getDataCollection();
        CTimeSeries cts = dc.getTimeSeriesByUniqueSdi(instance.getParmTsId("FixedRain").getKey());
        double expected = cts.findWithin(start, 100).getDoubleValue();
        assertEquals("Date doesn't match", expected, val, .0001);

    }

    @Test
    public void testDiffGreaterThan2() throws Exception {
        instance.initAWAlgorithm();
        instance.BadRain = Double.NEGATIVE_INFINITY;
        double val = instance.getPrevValue("FixedRain", missing);
        DataCollection dc = instance.getDataCollection();
        CTimeSeries fixed_rain = dc.getTimeSeriesByUniqueSdi(instance.getParmTsId("FixedRain").getKey());
        CTimeSeries bad_rain = dc.getTimeSeriesByUniqueSdi(instance.getParmTsId("BadRain").getKey());
        UnitHelpers.setBaseTime(instance, spike);
        instance.BadRain = bad_rain.findWithin(spike, 100).getDoubleValue();
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        double expected = fixed_rain.findWithin(before_spike, 100).getDoubleValue();
        assertEquals("Date doesn't match", expected, instance.FixedRain.getDoubleValue(), .0001);
    }

    @Test
    public void testNewValueGood() throws Exception {
        instance.initAWAlgorithm();
        instance.BadRain = Double.NEGATIVE_INFINITY;
        DataCollection dc = instance.getDataCollection();
        CTimeSeries cts = dc.getTimeSeriesByUniqueSdi(instance.getParmTsId("BadRain").getKey());
        instance.BadRain = cts.findWithin(normal_day, 100).getDoubleValue();
        UnitHelpers.setBaseTime(instance, normal_day);
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertEquals("Output was not the input value", instance.BadRain, instance.FixedRain.getDoubleValue(), .0001);

    }

    @Test
    public void testPreviousAndNewMissingOutputsNothing() throws Exception {
        instance.initAWAlgorithm();
        instance.BadRain = Double.NEGATIVE_INFINITY;
        DataCollection dc = instance.getDataCollection();
        CTimeSeries cts = dc.getTimeSeriesByUniqueSdi(instance.getParmTsId("BadRain").getKey());
        instance.BadRain = cts.findWithin(missing2, 100).getDoubleValue();
        UnitHelpers.setBaseTime(instance, missing2);
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertFalse("Output was changed", instance.FixedRain.isChanged());
        /*
         double expected = cts.findWithin(start, 100).getDoubleValue();
         assertEquals("Date doesn't match", expected, val, .0001);
         */
    }

    @Test
    public void testPrevousValueGreaterThanNew() throws Exception {
        instance.initAWAlgorithm();
        instance.BadRain = Double.NEGATIVE_INFINITY;
        DataCollection dc = instance.getDataCollection();
        CTimeSeries bad_rain = dc.getTimeSeriesByUniqueSdi(instance.getParmTsId("BadRain").getKey());
        CTimeSeries fixed_rain = dc.getTimeSeriesByUniqueSdi(instance.getParmTsId("FixedRain").getKey());
        instance.BadRain = bad_rain.findWithin(dip, 100).getDoubleValue();
        UnitHelpers.setBaseTime(instance, dip);
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        double expected = fixed_rain.findWithin(before_dip, 100).getDoubleValue();
        assertEquals("0utput should be previous value", expected, instance.FixedRain.getDoubleValue(), .0001);

    }
    
    @Ignore // NOTE: need to actually implement, needs rewrite of beforeTimeSlice
    @Test
    public void testResetDate() throws Exception {
        instance.initAWAlgorithm();
        instance.BadRain = Double.NEGATIVE_INFINITY;
        DataCollection dc = instance.getDataCollection();
        CTimeSeries bad_rain = dc.getTimeSeriesByUniqueSdi(instance.getParmTsId("BadRain").getKey());
        CTimeSeries fixed_rain = dc.getTimeSeriesByUniqueSdi(instance.getParmTsId("FixedRain").getKey());
        instance.BadRain = bad_rain.findWithin(reset, 100).getDoubleValue();
        UnitHelpers.setBaseTime(instance, reset);
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        double expected = fixed_rain.findWithin(reset, 100).getDoubleValue();
        assertEquals("Prevous value should have been used", expected, instance.FixedRain.getDoubleValue(), .0001);

    }
    
    
}
