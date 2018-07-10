/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spk.algo.support;

import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.Date;
import spk.algo.support.StationData;
import static org.junit.Assert.*;

/**
 *
 * @author L2EDDMAN
 */
public class StationDataTest {

    public StationDataTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of loadData method, of class StationData.
     */
    @Test
    public void testLoadData() {
        System.out.println("loadData");
        String file = "C:\\ccptestdata\\ALS.station";
        TreeMap expResult = null;
        TreeMap<Date, StationData> result = StationData.loadData(file);
        assertNotNull(result);
        System.out.println( result.keySet() );        
        StationData d = result.floorEntry(new Date() ).getValue();
        assertNotNull(d);
        System.out.println("Shift is " + d.shift + " created by " + d.who);
        assertEquals(0.2, d.shift, 0.05);
        //assertEquals( "l2eddman", d.who);
        

        
    }

}