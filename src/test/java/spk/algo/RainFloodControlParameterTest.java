/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;

import decodes.sql.DbKey;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.TimeSeriesDb;
import java.util.Date;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import opendcs.db.test.Fixtures;
import opendcs.db.test.TestDatabase;
import opendcs.db.test.UnitHelpers;
import sun.security.util.PropertyExpander;

/**
 *
 * @author L2EDDMAN
 */
public class RainFloodControlParameterTest {

    RainFloodControlParameter instance = null;
    DbComputation comp = null;
    TimeSeriesDb db = null;
    Fixtures fixtures = null;
    Date t1 = Fixtures.sdf.parse("2018-07-11T00:00:00+0000");
    Date reset = Fixtures.sdf.parse("2018-07-17T00:00:00+0000");
    Date start = Fixtures.sdf.parse("2018-07-10T00:00:00+0000");
    Date end = Fixtures.sdf.parse("2018-07-25T00:00:00+0000");

    @Rule
    public ExpectedException exception = ExpectedException.none();

    public RainFloodControlParameterTest() throws Exception {
        db = new TestDatabase();
        fixtures = Fixtures.getFixtures(db);
    }

    @Before
    public void setUp() throws Exception {
        DbCompAlgorithm dbca = new DbCompAlgorithm("RainFloodControlParameter");
        dbca.setExecClass("spk.algo.RainFloodControlParameter");
        comp = new DbComputation(DbKey.NullKey, "RainFloodControlParameterTest");
        comp.setProperty("aggregateTimeZone", "UTC");
        comp.setProperty("Decay", "0.5");
        comp.setProperty("ResetDate", "");
        comp.setAlgorithm(dbca);
        comp.addParm(new DbCompParm("BasinPrecip", fixtures.getTimeSeriesKey("TEST.Precip-Basin.Total.1Day.1Day.raw"), "1Day", null, 0));
        comp.addParm(new DbCompParm("FloodControlParameter", fixtures.getTimeSeriesKey("TEST.Precip-FCP.Inst.1Day.0.calc"), "1Day", null, 0));

        comp.prepareForExec(db);

        instance = (RainFloodControlParameter) comp.getExecutive();

    }

    /**
     * This check should actually be in AlgBaseNew
     *
     * @throws Exception
     */
    @Test
    public void testPreviousValueRetrieved() throws Exception {
        DataCollection dc = UnitHelpers.getCompData(comp, db.makeTimeSeriesDAO(), start, end);
        UnitHelpers.prepForApply(instance, dc);
        instance.initAWAlgorithm();
        double prev = instance.getPrevValue("FloodControlParameter", t1);

        assertEquals("Declay not correct", 10, prev, .0001);
    }

    @Test
    public void testDecay() throws Exception {
        DataCollection dc = UnitHelpers.getCompData(comp, db.makeTimeSeriesDAO(), start, end);
        UnitHelpers.prepForApply(instance, dc);
        instance.initAWAlgorithm();
        instance.previous_fcp = 10;
        instance.Decay = .5;
        UnitHelpers.setBaseTime(instance, t1);
        instance.beforeTimeSlices();
        instance.first_run = false;
        instance.BasinPrecip = 0;
        instance.resetDate = null;
        instance.doAWTimeSlice();
        assertEquals("Declay not correct", 5, instance.FloodControlParameter.getDoubleValue(), .0001);

    }

    @Test
    public void testResetDate() throws Exception {
        comp.setUnPrepared();
        comp.setProperty("ResetDate", "17Jul");
        comp.prepareForExec(db);
        instance = (RainFloodControlParameter) comp.getExecutive();
        DataCollection dc = UnitHelpers.getCompData(comp, db.makeTimeSeriesDAO(), start, end);
        UnitHelpers.prepForApply(instance, dc);
        //instance.initAWAlgorithm();
        instance.beforeTimeSlices();
        instance.previous_fcp = 10;
        instance.first_run = false;
        instance.Decay = .5;
        instance.ResetValue = 100;
        UnitHelpers.setBaseTime(instance, reset);
        instance.BasinPrecip = 1;
        instance.doAWTimeSlice();
        
        assertEquals("reset date and value were not correctly used",51,instance.FloodControlParameter.getDoubleValue(), .0001);
        
    }

    @Test
    public void testDecayMustBeLessThan1() throws Exception {
        exception.expect(DbCompException.class);
        exception.expectMessage(new StringContains("specify a Decay parameter between but not including 0 or 1"));
        instance.Decay = 1.01;
        instance.initAWAlgorithm();
    }

    @Test
    public void testDecayMustBeGreaterThan0() throws Exception {
        exception.expect(DbCompException.class);
        exception.expectMessage(new StringContains("specify a Decay parameter between but not including 0 or 1"));
        instance.Decay = 0.0;
        instance.initAWAlgorithm();

    }

}
