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
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import opendcs.db.test.TestDatabase;

/**
 *
 * @author L2EDDMAN
 */
public class SumModuloTest {
    
    SumModulo instance = null;
    
    public SumModuloTest() {
    }
    
    @Before
    public void setUp() throws Exception {
        TimeSeriesDb db = new TestDatabase();
        DbComputation comp = new DbComputation(DbKey.NullKey, "test");
        DbCompAlgorithm dbca = new DbCompAlgorithm("SumModulo");
        dbca.setExecClass("spk.algo.SumModulo");
        comp.setAlgorithm(dbca);
        comp.setProperty("aggregateTimeZone", "PST8PDT");
        comp.prepareForExec(db);
        instance = (SumModulo) comp.getExecutive();
    }

    @Test
    public void testSumNoWrap() throws Exception {
        instance.a = 10;
        instance.b = 10;
        instance.modulus = 50;
        instance.doAWTimeSlice();
        assertEquals("sumation failed", 20.0, instance.y.getDoubleValue(),.0001);
    }
    
    @Test
    public void testSumWrap() throws Exception {
        instance.a = 10;
        instance.b = 10;
        instance.modulus = 15;
        instance.doAWTimeSlice();
        assertEquals("modulus failed", 5.0, instance.y.getDoubleValue(),.0001);
    }
    
}
