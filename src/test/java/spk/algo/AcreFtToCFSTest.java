/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;

import decodes.sql.DbKey;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.TimeSeriesDb;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import opendcs.db.test.Fixtures;
import opendcs.db.test.TestDatabase;
import opendcs.db.test.UnitHelpers;

/**
 *
 * @author L2EDDMAN
 */
public class AcreFtToCFSTest {

    AcreFtToCFS instance = null;

    public AcreFtToCFSTest() {
    }

    @Before
    public void setup() throws Exception {
        TimeSeriesDb db = new TestDatabase();
        DbComputation comp = new DbComputation(DbKey.NullKey, "test");
        DbCompAlgorithm dbca = new DbCompAlgorithm("AcreFtToCFS");
        dbca.setExecClass("spk.algo.AcreFtToCFS");
        comp.setAlgorithm(dbca);
        comp.setProperty("aggregateTimeZone", "PST8PDT");
        comp.prepareForExec(db);
        instance = (AcreFtToCFS) comp.getExecutive();
    }

    @Test
    public void testNormalDay() throws Exception {

        UnitHelpers.setBaseTime(instance, Fixtures.sdf.parse("2018-07-01T07:00:00+0000"));
        //UnitHelpers.setCompProperty(instance, "aggregateTimeZone", "PST8PDT");
        instance.acreft = 1;
        instance.doAWTimeSlice();
        assertEquals("calculation incorrect", 0.50417, instance.cfs.getDoubleValue(), .000001);
    }

    @Test
    public void testShortDay() throws Exception {
        UnitHelpers.setBaseTime(instance, Fixtures.sdf.parse("2018-03-12T07:00:00+0000"));
        //UnitHelpers.setCompProperty(instance, "aggregateTimeZone", "PST8PDT");
        instance.acreft = 1;
        instance.doAWTimeSlice();
        assertEquals("calculation incorrect", 0.52609, instance.cfs.getDoubleValue(), .000001);
    }

    @Test
    public void testLongDay() throws Exception {
        UnitHelpers.setBaseTime(instance, Fixtures.sdf.parse("2018-11-05T08:00:00+0000"));
        UnitHelpers.setCompProperty(instance, "aggregateTimeZone", "PST8PDT");
        instance.acreft = 1;
        instance.doAWTimeSlice();
        assertEquals("calculation incorrect", 0.48400, instance.cfs.getDoubleValue(), .000001);
    }

}
