/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;

import java.text.ParseException;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import spk.db.test.Fixtures;

/**
 *
 * @author L2EDDMAN
 */
public class EliminateNegativePrecipTest {
    
    Date start = null;
    Date dip = null;
    Date spike = null;
    Date reset = null;
    
    public EliminateNegativePrecipTest() throws ParseException {
        
        start = Fixtures.sdf.parse("2018-07-10T00:00:00+0000");
        dip = Fixtures.sdf.parse("2018-07-10T06:00:00+0000");
        spike = Fixtures.sdf.parse("2018-07-10T07:00:00+0000");
        reset = Fixtures.sdf.parse("2018-07-10T12:00:00+0000");
        
    }
    
    @Before
    public void setUp() {
    }

    @Test
    public void testInitAlgorithm() {
        fail("not yet implemented");
    }
    
    @Test
    public void testGetPrevRain(){
        fail("not yet implemented");
    }
    
    @Test
    public void testDiffGreaterThan2(){
        fail("not yet implemented");
    }
    
    @Test
    public void testNewValueGood(){
        fail("not yet implemented");
    }
    
    @Test
    public void testMissingOutputsPrevious(){
        fail("not yet implemented");
    }
    
    @Test
    public void testPreviousAndNewMissingOutputsNothing(){
        fail("not yet implemented");
    }
    
    @Test
    public void testPrevousValueGreaterThanNew(){
        fail("not yet implemented");
    }
}
