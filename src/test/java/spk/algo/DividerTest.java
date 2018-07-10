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
import decodes.tsdb.DbComputation;
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
public class DividerTest {
    
    Divider instance = null;
    
    public DividerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws DbCompException {
        DbCompAlgorithm dbca = new DbCompAlgorithm("Divider");
        dbca.setExecClass("spk.algo.Divider");        
        instance = new Divider();
        DbComputation comp = new DbComputation(DbKey.NullKey,"DividerTest");
        comp.setAlgorithmName("spk.algo.Divider");
        comp.setAlgorithm(dbca);
        instance.init(comp , null);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of initAWAlgorithm method, of class Divider.
     */
  

    /**
     * Test of doAWTimeSlice method, of class Divider.
     */
    @Test
    public void testTenDivide5() throws Exception {
        instance.a = 10;
        instance.b = 5;
        
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        instance.afterTimeSlices();
        
        assertEquals("Failed to divided 10 by 5", 2, instance.y.getDoubleValue(), 0.0001);
    }
    
    @Test
    public void testDivideByZero() throws Exception{     
        instance.a = 10;
        instance.b = 0;
       
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        instance.afterTimeSlices();
        
        assertEquals("Code actually tried to divide by zero", 0, instance.y.getDoubleValue(),0.0001);    
        
    }

    
    
}
