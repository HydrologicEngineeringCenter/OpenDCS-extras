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
/**
 *
 * @author L2EDDMAN
 */
public class DateTimeTest {
    public static SimpleDateFormat sdf = null;


    public DateTimeTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        sdf = new SimpleDateFormat("ddMMMyyyy HHmm");
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
     * Test of to_wy_julian method, of class DateTime.
     */
    @Test
    public void testTo_wy_julian() {
        System.out.println("to_wy_julian");
        Date t = new GregorianCalendar(2011, Calendar.OCTOBER, 1).getTime();
        System.out.println( sdf.format(t));
        int expResult = 0;
        int result = DateTime.to_wy_julian(t);
        System.out.println( "Julian -> " + result );
        assertEquals(1, result);

        t = new GregorianCalendar( 2011, Calendar.NOVEMBER, 1 ).getTime();
        System.out.println( sdf.format(t));
        result = DateTime.to_wy_julian(t);
        System.out.println( "Julian -> " + result );
        assertEquals(32, result);

        t = new GregorianCalendar( 2012, Calendar.JANUARY, 5 ).getTime();
        System.out.println( sdf.format(t));
        result = DateTime.to_wy_julian(t);
        System.out.println( "Julian -> " + result );
        assertEquals(97, result );

        t = new GregorianCalendar( 2011, Calendar.MARCH, 5 ).getTime();
        System.out.println( sdf.format(t));
        result = DateTime.to_wy_julian(t);
        System.out.println( "Julian -> " + result );
        assertEquals(156, result );


        t = new GregorianCalendar( 2012, Calendar.MARCH, 5 ).getTime();
        System.out.println( sdf.format(t));
        result = DateTime.to_wy_julian(t);
        System.out.println( "Julian -> " + result );
        assertEquals(157, result );

        t = new GregorianCalendar(2012, Calendar.OCTOBER, 1).getTime();
        System.out.println( sdf.format(t));
        result = DateTime.to_wy_julian(t);
        System.out.println( "Julian -> " + result );
        assertEquals(1, result);

        t = new GregorianCalendar(2011, Calendar.SEPTEMBER, 30).getTime();
        System.out.println( sdf.format(t));
        result = DateTime.to_wy_julian(t);
        System.out.println( "Julian -> " + result );
        assertEquals(365, result);

        t = new GregorianCalendar(2012, Calendar.SEPTEMBER, 30).getTime();
        System.out.println( sdf.format(t));
        result = DateTime.to_wy_julian(t);
        System.out.println( "Julian -> " + result );
        assertEquals(366, result);

        t = new GregorianCalendar(2013, Calendar.JANUARY, 15).getTime();
        System.out.println( sdf.format(t));
        result = DateTime.to_wy_julian(t);
        System.out.println( "Julian -> " + result );
        assertEquals(107, result);
        
    }

}