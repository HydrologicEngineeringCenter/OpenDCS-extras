/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author L2EDDMAN
 */
public class HADATest {
    
    public HADATest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of subup method, of class HADA.
     */
    @Test
    @Ignore
    public void testSubup() {
        System.out.println("subup");
        int day = 0;
        double[] upstream_storage = new double[2];
        upstream_storage[0] = 231158.0;
        upstream_storage[1] = 241786.0;
        double space_available = 0.0;
        double transferable_snow_space = 0.0;
        HADA instance = new HADA();
        
        
        
        double[] expResult = new double[4];
        expResult[0] = 0.0;
        expResult[1] = 0.0;
        expResult[2] = 187477.12;
        expResult[3] = 0.0;
        double[] result = instance.subup(day, upstream_storage, space_available, transferable_snow_space);
        //assertArrayEquals(expResult, result, 1000);
        assertEquals( expResult[2],result[2], 1000);
        // TODO review the generated test code and remove the default call to fail.
        
    }
}