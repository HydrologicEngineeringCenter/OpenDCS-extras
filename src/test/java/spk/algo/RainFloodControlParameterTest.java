/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;

import decodes.sql.DbKey;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.TimeSeriesDb;
import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import spk.db.test.Fixtures;
import spk.db.test.TestDatabase;
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
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    public RainFloodControlParameterTest() throws Exception {
        db = new TestDatabase();
        fixtures = Fixtures.getFixtures(db);
    }
    
    @Before
    public void setUp() throws Exception{
        DbCompAlgorithm dbca = new DbCompAlgorithm("RainFloodControlParameter");
        dbca.setExecClass("spk.algo.RainFloodControlParameter");
        comp = new DbComputation(DbKey.NullKey, "RainFloodControlParameterTest");
        comp.setProperty("aggregateTimeZone", "UTC");
        comp.setProperty("Decay", "0.5");
        comp.setAlgorithm(dbca);
        comp.addParm(new DbCompParm("BasinPrecip", fixtures.getTimeSeriesKey("TEST.Basin-Precip.Total.1Day.1Day.raw"), "1Day", null, 0));        
        comp.addParm(new DbCompParm("FloodControlParameter", fixtures.getTimeSeriesKey("TEST.Precip-FCP.Inst.1Day.0.calc"), "1Day", null, 0));
        
        comp.prepareForExec(db);
        
        instance = (RainFloodControlParameter) comp.getExecutive();
        
    }

    @Test
    public void testPreviousValueRetrieved() {
        fail("not yet implemented");
    }
    
    @Test
    public void testDecay() {
        fail("not yet implemented");
    }
    
    @Test
    public void testResetDate() {
        fail("not yet implemented");
    }
    
    @Test 
    public void testDecayMustBeLessThan1() throws Exception{
        exception.expect(DbCompException.class);
        exception.expectMessage(new StringContains("specify a Decay parameter between but not including 0 or 1"));
        instance.Decay = 1.01;
        instance.initAWAlgorithm();
    }
    
    @Test
    public void testDecayMustBeGreaterThan0() throws Exception{
        exception.expect(DbCompException.class);
        exception.expectMessage(new StringContains("specify a Decay parameter between but not including 0 or 1"));
        instance.Decay = 0.0;
        instance.initAWAlgorithm();
        
    }
    
}
