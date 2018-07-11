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

    String uri = "classpath:/shared/stations/TEST.Stage.station";

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
        TreeMap<Date, StationData> result = StationData.loadData(uri);
        assertNotNull(result);
    }

    @Test
    public void testGetRow() {
        TreeMap<Date, StationData> result = StationData.loadData(uri);

        StationData d = result.floorEntry(new Date()).getValue();
        assertNotNull(d);
    }

    @Test
    public void testGetShift() {
        TreeMap<Date, StationData> result = StationData.loadData(uri);
        StationData d = result.floorEntry(new Date()).getValue();
        assertEquals(0.0, d.shift, 0.0001);
        //assertEquals( "l2eddman", d.who);

    }

}
