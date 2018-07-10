/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.support.Alarms;

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
public class AlarmResponseTest {
    
    public AlarmResponseTest() {
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
     * Test of toString method, of class AlarmResponse.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        AlarmResponse instance = new AlarmResponse();
        String expResult = "";
        instance.color = "red";
        instance.condition = ">5:2H";
        instance.project = "Martis";
        instance.site = "P13";
        instance.timeseries = "MRT GEO-P13-Truckee.Elev.Inst.15Minute.0.Calc-geo";
        instance.units= "ft";
        instance.value = 10;
        
        String result = instance.toString();
        System.out.println( result );
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
