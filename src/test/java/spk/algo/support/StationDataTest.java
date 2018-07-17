/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import java.text.SimpleDateFormat;
import java.util.TreeMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.Date;
import java.util.TimeZone;
import spk.algo.support.StationData;
import static org.junit.Assert.*;

/**
 *
 * @author L2EDDMAN
 */
public class StationDataTest {

    String uri = "classpath:/shared/stations/TEST.Stage.station";
    String uri2 = "classpath:/shared/stations/TEST-2.Stage.station";

    //07/17/2018 00:00:00
    SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ssZ");

    public StationDataTest() {
        
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.setProperty("user.timezone", "UTC");
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
    public void testLoadData() throws Exception {
        TreeMap<Date, StationData> result = StationData.loadData(uri);
        assertNotNull(result);
    }

    @Test
    public void testGetRow() throws Exception {
        TreeMap<Date, StationData> result = StationData.loadData(uri);

        StationData d = result.floorEntry(new Date()).getValue();
        assertNotNull(d);
    }

    @Test
    public void testGetShift() throws Exception {
        TreeMap<Date, StationData> result = StationData.loadData(uri);
        StationData d = result.floorEntry(new Date()).getValue();
        assertEquals(0.0, d.shift, 0.0001);
        //assertEquals( "l2eddman", d.who);
    }

    @Test
    public void testGetPZF() throws Exception {
        Date dt = df.parse("06/26/2018 23:00:00+0000");
        TreeMap<Date, StationData> result = StationData.loadData(uri2);
        StationData d = result.floorEntry(dt).getValue();

        assertEquals("StationData did not return correct PZF", 1.81, d.point_zero_flow, .00001);
    }

    @Test
    public void testGetPZF_withUnit() throws Exception {
        Date dt = df.parse("07/17/2018 00:00:00+0000");
        TreeMap<Date, StationData> result = StationData.loadData(uri2);
        StationData d = result.floorEntry(dt).getValue();

        assertEquals("StationData did not return correct PZF", 2.0, d.point_zero_flow, .00001);
        assertEquals("StationData does not have correct PZF units", "ft", d.point_zero_flow_units);
    }

}
