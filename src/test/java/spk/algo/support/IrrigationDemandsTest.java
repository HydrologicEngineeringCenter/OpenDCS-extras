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
    double delta = 0.0001;
    Date Feb4_2017 = null;
    Date Jan1_2010 = null;
    Date Feb1_2016 = null;
            
    public IrrigationDemandsTest() throws ParseException {
        df = new SimpleDateFormat("MM/dd/yyyy");
        Feb4_2017 = df.parse("02/04/2017");
        Jan1_2010 = df.parse("01/01/2010");
        Feb1_2016 = df.parse("02/01/2016");
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
    public void testLoadFile() throws Exception {
        IrrigationDemands d = new IrrigationDemands("classpath:/data/irrigation_demands/DNP.irr");
    }

    @Test
    public void testGetDemandsSimple() throws Exception {
        IrrigationDemands d = new IrrigationDemands("classpath:/data/irrigation_demands/DNP.irr");
        Date t1 = df.parse("02/04/2017");
        Dates dates = new Dates(t1);
        double list[] = d.getDemands(t1);
        assertEquals("List value doesn't match required value",180.0,list[dates.February04],.0001);
    }
    
    /**
     * Test that January is 0 (provided files should not have a january flow defined)
     */
    @Test
    public void testJanuaryIs0() throws Exception {
        IrrigationDemands d = new IrrigationDemands("classpath:/data/irrigation_demands/DNP.irr");
        Dates datesT1 = new Dates(Feb4_2017);
        double list[] = d.getDemands(Feb4_2017);
        assertEquals("first value not 0", list[datesT1.January01], 0.0, delta);
    }
    
    /**
     * Test of getDemands method, of class IrrigationDemands.
     */
    @Test
    public void testGetDemandsComplexFile() throws Exception {
        IrrigationDemands d = new IrrigationDemands("classpath:/data/irrigation_demands/PNF.irr");
        Dates datesT1 = new Dates(Jan1_2010);
        double[] list = d.getDemands(Jan1_2010);
        assertEquals("July 31st not 5250.0", list[datesT1.July31], 5250.0, delta);
    }
    
    @Test
    public void testGetDemandsFeb29() throws Exception{
        IrrigationDemands d = new IrrigationDemands("classpath:/data/irrigation_demands/PNF.irr");        
        Dates datesT2 = new Dates(Feb1_2016);
        double[] list = d.getDemands(Feb1_2016);        
        assertEquals("Feb 29 is the february value", 150, list[datesT2.February29], delta);
    }
    
    @Test
    public void testGetDemandsMar1() throws Exception{
        IrrigationDemands d = new IrrigationDemands("classpath:/data/irrigation_demands/PNF.irr");        
        Dates datesT2 = new Dates(Feb1_2016);
        double[] list = d.getDemands(Feb1_2016);        
        assertEquals("Mar 01 is the march value ", 400, list[datesT2.March01], delta);        
    }
    
    
}
