/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;

import decodes.sql.DbKey;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbComputation;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author L2EDDMAN
 */
public class TrapezoidalIntegrationAverageTest {
    
    TrapezoidalIntegrationAverage instance = null;
    
    public TrapezoidalIntegrationAverageTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws DbCompException {
        DbCompAlgorithm dbca = new DbCompAlgorithm("TrapezoidalIntegrationAverage");
        dbca.setExecClass("spk.algo.TrapezoidalIntegrationAverage");
        
        instance = new TrapezoidalIntegrationAverage();
        instance.initAWAlgorithm();
        DbComputation comp = new DbComputation(DbKey.NullKey,"TrapIntTest");
        
        comp.setAlgorithmName("TrapIntegration");
        comp.setAlgorithm(dbca);
        instance.init(comp , null);
        
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
        assertEquals(1,instance.count);                
    }

    /**
     * Test of afterTimeSlices method, of class TrapezoidalIntegrationAverage.
     */
    @Test
    public void testAfterTimeSlices() throws Exception {
        instance.beforeTimeSlices();
        // loop
        instance.input = 10;
        instance.setTimeSliceBaseTime(new Date(0));
        instance.doAWTimeSlice();
        instance.setTimeSliceBaseTime(new Date(900*1000));
        instance.doAWTimeSlice();
        instance.afterTimeSlices();        
        assertEquals(10, instance.average.getDoubleValue(), 0.0001);
    }

    /**
     * Test of more data?
     */
    
    
}
