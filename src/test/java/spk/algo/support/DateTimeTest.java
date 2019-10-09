/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import spk.algo.support.DateTime;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import opendcs.db.test.Fixtures;

/**
 *
 * @author L2EDDMAN
 */
public class DateTimeTest {

    public static SimpleDateFormat sdf = null;
    long expResult = -1;
    long result = -1;
    Date t = null;
    private Date october_1_utc = null;
    private Date november_1_utc = null;
    private final Date january_5_utc;
    TimeZone utc = TimeZone.getTimeZone("UTC");
    TimeZone default_tz = TimeZone.getDefault();
    private final Date october_1_default_tz;

    public DateTimeTest() throws Exception {
        SimpleDateFormat notz = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        october_1_utc = Fixtures.sdf.parse("2011-10-01T00:00:00+0000");
        november_1_utc = Fixtures.sdf.parse("2011-11-01T00:00:00+0000");
        january_5_utc = Fixtures.sdf.parse("2012-01-05T00:00:00+0000");

        october_1_default_tz = notz.parse("2011-10-01T00:00:00");

    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        sdf = new SimpleDateFormat("ddMMMyyyy HHmm");
        System.setProperty("user.timezone", "UTC");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        expResult = -1;
        result = -2;
        t = null;
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of to_wy_julian method, of class DateTime.
     */
    @Test
    public void testOctober1_withtz() {
        t = new GregorianCalendar(2011, Calendar.OCTOBER, 1).getTime();
        expResult = 1;
        result = DateTime.to_wy_julian(october_1_utc, utc);
        assertEquals("October 1st Julian date not determined correctly", expResult, result);
    }

    @Test
    public void testOctober1_defaulttz() {
        expResult = 1;
        result = DateTime.to_wy_julian(october_1_default_tz);
        assertEquals("October 1st Julian date not determined correctly", expResult, result);
    }

    @Test
    public void testNovember1() {
        result = DateTime.to_wy_julian(november_1_utc, utc);
        expResult = 32;
        assertEquals("November 1 Julian date not deteremined correctly", expResult, result);
    }

    @Test
    public void testJanuary5() {
        result = DateTime.to_wy_julian(january_5_utc, utc);
        expResult = 97;
        assertEquals("January 5 Julian date not determined correctly", expResult, result);
    }

    @Test
    public void testMarch5NoLeapYear() {
        t = new GregorianCalendar(2011, Calendar.MARCH, 5).getTime();
        result = DateTime.to_wy_julian(t);
        expResult = 156;
        assertEquals("Non Leap year march 5 julian date not determined correctly", expResult, result);
    }

    @Test
    public void testMarch5Leapyear() {
        t = new GregorianCalendar(2012, Calendar.MARCH, 5).getTime();
        result = DateTime.to_wy_julian(t);
        expResult = 157;
        assertEquals("Leap year march 5 julian date not determined correctly", expResult, result);
    }

    @Test
    public void testSep30NonLeapYear() {
        t = new GregorianCalendar(2011, Calendar.SEPTEMBER, 30).getTime();
        result = DateTime.to_wy_julian(t);
        expResult = 365;
        assertEquals("Non Leap Year Sep 30th julian day not determined correctly", expResult, result);
    }

    @Test
    public void testSep30LeapYear() {
        t = new GregorianCalendar(2012, Calendar.SEPTEMBER, 30).getTime();
        result = DateTime.to_wy_julian(t);
        expResult = 366;
        assertEquals("Leap year Sep 30th julian day not determined correclty", expResult, result );                
    }

}
