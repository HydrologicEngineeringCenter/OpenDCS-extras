/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class IrrigationDemandsTest {
        private SimpleDateFormat df;
    public IrrigationDemandsTest() {
        df = new SimpleDateFormat("MM/dd/yyyy");
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

    @Test
    public void testSomeMethod() throws ParseException {
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
        System.out.println("reading file");
        try{
         IrrigationDemands d = new IrrigationDemands( "C:\\tests\\PNF.irr");
         
        }
        catch( Exception e){
            e.printStackTrace();
            fail("exception thrown");
        }
    }

    /**
     * Test of getDemands method, of class IrrigationDemands.
     */
    @Test
    public void testGetDemands() throws Exception {
        double delta = 0.0001;
        System.out.println("getDemands");
        IrrigationDemands d = new IrrigationDemands( "C:\\tests\\PNF.irr");
        Date t1 = df.parse("01/01/2010");
        Date t2 = df.parse("01/01/2012");
        double list[] = d.getDemands(t1);
        assertEquals( "first value not 0", list[0], 0.0, delta );
        assertEquals( "July 31st not 5250.0" ,list[304], 5250.0, delta );
         
        d = new IrrigationDemands( "C:\\tests\\TRM.irr");
        list = d.getDemands(t2);
         
        assertEquals( "first value not 0", list[0], 0.0, delta);
        assertEquals( "Feb 29 is the february value", list[152], 30.0, delta );
        assertEquals( "Mar 01 is the march value ",   list[153], 200.0, delta );
         
        // TODO review the generated test code and remove the default call to fail.
        
    }
}