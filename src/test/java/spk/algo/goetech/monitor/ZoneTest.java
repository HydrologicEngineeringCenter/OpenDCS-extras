/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.algo.goetech.monitor;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class ZoneTest {
    String zonedata;
    String zonedata2;
    public ZoneTest() {
        zonedata = "zone: test1\r\n"
                + "point: 0,0\r\n"
                + "point: 0,5\r\n"
                + "point: 5,5\r\n"
                + "point: 5,0\r\n";
                
        zonedata2 = "zone: test2\r\n"
                + "point: 0,0\r\n"
                + "point: 0,5\r\n"
                + "point: 3,7\r\n"
                + "point: 4,3\r\n"
                + "point: 6,5\r\n"
                + "point: 7,5\r\n"
                + "point: 7,0\r\n";
        
    }

    /**
     * Test of inZone method, of class Zone.
     */
    @Test
    public void testInZone_double_double() {
        Zone zone = new Zone(this.zonedata);
        
        assertTrue( zone.inZone( 2,2 ) );
        assertFalse( zone.inZone( -1,1) );
        assertTrue( zone.inZone( 0,2.5 ) );
        assertTrue( zone.inZone( 1,0 ) );
        
        zone = new Zone(this.zonedata2);
        assertTrue( zone.inZone(2,2));
        assertFalse( zone.inZone(-1,-1));
        assertTrue( zone.inZone(2,5));
        assertTrue( zone.inZone(6,3));
        assertFalse( zone.inZone(4,4));
        assertFalse( zone.inZone(4,3.001));
        assertTrue( zone.inZone(3.99999999,2.999999999));
        
        
    }

    /**
     * Test of inZone method, of class Zone.
     */
    @Test
    public void testInZone_Point() {
        
    }
    
}
