/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.support.Alarms;

import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import java.util.Date;
import java.util.Enumeration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class ThresholdTest {
    
    public ThresholdTest() {
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
     * Test of indexOfDurationStart method, of class Threshold.
     */
    @Test
    public void testIndexOfDurationStart() {
        System.out.println("indexOfDurationStart");
        int duration = 0;
        Date end = null;
        CTimeSeries ts = null;
        int fudge = 0;
        int expResult = 0;
        int result = Threshold.indexOfDurationStart(duration, end, ts, fudge);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of check method, of class Threshold.
     */
    @Test
    public void testCheck() throws Exception {
        System.out.println("check");
        CTimeSeries ts = null;
        Threshold instance = new Threshold();
        boolean expResult = false;
        boolean result = instance.check(ts);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_check_str method, of class Threshold.
     */
    @Test
    public void testGet_check_str() {
        System.out.println("get_check_str");
        Threshold instance = new Threshold();
        String expResult = "";
        String result = instance.get_check_str();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_check_type method, of class Threshold.
     */
    @Test
    public void testGet_check_type() {
        System.out.println("get_check_type");
        Threshold instance = new Threshold();
        String expResult = "";
        String result = instance.get_check_type();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_check_type method, of class Threshold.
     */
    @Test
    public void testSet_check_type() {
        System.out.println("set_check_type");
        String type = "";
        Threshold instance = new Threshold();
        instance.set_check_type(type);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_duration method, of class Threshold.
     */
    @Test
    public void testGet_duration() {
        System.out.println("get_duration");
        Threshold instance = new Threshold();
        int expResult = 0;
        int result = instance.get_duration();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_duration method, of class Threshold.
     */
    @Test
    public void testSet_duration() {
        System.out.println("set_duration");
        int duration = 0;
        Threshold instance = new Threshold();
        instance.set_duration(duration);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_a method, of class Threshold.
     */
    @Test
    public void testGet_a() {
        System.out.println("get_a");
        Threshold instance = new Threshold();
        double expResult = 0.0;
        double result = instance.get_a();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_a method, of class Threshold.
     */
    @Test
    public void testSet_a() {
        System.out.println("set_a");
        double a = 0.0;
        Threshold instance = new Threshold();
        instance.set_a(a);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_b method, of class Threshold.
     */
    @Test
    public void testGet_b_0args() {
        System.out.println("get_b");
        Threshold instance = new Threshold();
        double expResult = 0.0;
        double result = instance.get_b();
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_b method, of class Threshold.
     */
    @Test
    public void testGet_b_double() {
        System.out.println("get_b");
        double b = 0.0;
        Threshold instance = new Threshold();
        instance.get_b(b);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_color method, of class Threshold.
     */
    @Test
    public void testSet_color() {
        System.out.println("set_color");
        String color = "";
        Threshold instance = new Threshold();
        instance.set_color(color);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_color method, of class Threshold.
     */
    @Test
    public void testGet_color() {
        System.out.println("get_color");
        Threshold instance = new Threshold();
        String expResult = "";
        String result = instance.get_color();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_priority method, of class Threshold.
     */
    @Test
    public void testGet_priority() {
        System.out.println("get_priority");
        Threshold instance = new Threshold();
        int expResult = 0;
        int result = instance.get_priority();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_priority method, of class Threshold.
     */
    @Test
    public void testSet_priority() {
        System.out.println("set_priority");
        int priority = 0;
        Threshold instance = new Threshold();
        instance.set_priority(priority);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_units method, of class Threshold.
     */
    @Test
    public void testGet_units() {
        System.out.println("get_units");
        Threshold instance = new Threshold();
        String expResult = "";
        String result = instance.get_units();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_units method, of class Threshold.
     */
    @Test
    public void testSet_units() {
        System.out.println("set_units");
        String units = "";
        Threshold instance = new Threshold();
        instance.set_units(units);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of get_sdi method, of class Threshold.
     */
    @Test
    public void testGet_sdi() {
        System.out.println("get_sdi");
        Threshold instance = new Threshold();
        DbKey expResult = null;
        DbKey result = instance.get_sdi();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of set_sdi method, of class Threshold.
     */
    @Test
    public void testSet_sdi() {
        System.out.println("set_sdi");
        DbKey site = null;
        Threshold instance = new Threshold();
        instance.set_sdi(site);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isMissing method, of class Threshold.
     */
    @Test
    public void testIsMissing_double() {
        System.out.println("isMissing");
        double var = 0.0;
        Threshold instance = new Threshold();
        boolean expResult = false;
        boolean result = instance.isMissing(var);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isMissing method, of class Threshold.
     */
    @Test
    public void testIsMissing_long() {
        System.out.println("isMissing");
        long var = 0L;
        Threshold instance = new Threshold();
        boolean expResult = false;
        boolean result = instance.isMissing(var);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isMissing method, of class Threshold.
     */
    @Test
    public void testIsMissing_String() {
        System.out.println("isMissing");
        String var = "";
        Threshold instance = new Threshold();
        boolean expResult = false;
        boolean result = instance.isMissing(var);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getObjectType method, of class Threshold.
     */
    @Test
    public void testGetObjectType() {
        System.out.println("getObjectType");
        Threshold instance = new Threshold();
        String expResult = "";
        String result = instance.getObjectType();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of prepareForExec method, of class Threshold.
     */
    @Test
    public void testPrepareForExec() throws Exception {
        System.out.println("prepareForExec");
        Threshold instance = new Threshold();
        instance.prepareForExec();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isPrepared method, of class Threshold.
     */
    @Test
    public void testIsPrepared() {
        System.out.println("isPrepared");
        Threshold instance = new Threshold();
        boolean expResult = false;
        boolean result = instance.isPrepared();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of read method, of class Threshold.
     */
    @Test
    public void testRead() throws Exception {
        System.out.println("read");
        Threshold instance = new Threshold();
        instance.read();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of write method, of class Threshold.
     */
    @Test
    public void testWrite() throws Exception {
        System.out.println("write");
        Threshold instance = new Threshold();
        instance.write();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setProperty method, of class Threshold.
     */
    @Test
    public void testSetProperty() {
        System.out.println("setProperty");
        String name = "";
        String value = "";
        Threshold instance = new Threshold();
        instance.setProperty(name, value);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getProperty method, of class Threshold.
     */
    @Test
    public void testGetProperty() {
        System.out.println("getProperty");
        String name = "";
        Threshold instance = new Threshold();
        String expResult = "";
        String result = instance.getProperty(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPropertyNames method, of class Threshold.
     */
    @Test
    public void testGetPropertyNames() {
        System.out.println("getPropertyNames");
        Threshold instance = new Threshold();
        Enumeration expResult = null;
        Enumeration result = instance.getPropertyNames();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rmProperty method, of class Threshold.
     */
    @Test
    public void testRmProperty() {
        System.out.println("rmProperty");
        String name = "";
        Threshold instance = new Threshold();
        instance.rmProperty(name);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUniqueName method, of class Threshold.
     */
    @Test
    public void testGetUniqueName() {
        System.out.println("getUniqueName");
        Threshold instance = new Threshold();
        String expResult = "";
        String result = instance.getUniqueName();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
