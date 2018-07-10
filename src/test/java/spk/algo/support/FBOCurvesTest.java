/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo.support;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;

/**
 *
 * @author L2EDDMAN
 */
public class FBOCurvesTest {
    
    public FBOCurvesTest() {
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
     * Test of get_topcon method, of class FBOCurves.
     * @throws java.io.UnsupportedEncodingException
     */
    @Test
    public void testGet_topcon() throws UnsupportedEncodingException, IOException {
        System.out.println("get_topcon");
        int window = 0;
        double volume = 0.0;
        StringBuilder builder = new StringBuilder();
        builder.append("fbo_curve: 120, 0|566934 150000|566934 300000|566934 1432992|366934 1670547|325000\n");
        builder.append("fbo_curve:  72, 0|566934 300000|566934 319471|457463 1155749|366934 1350002|325000\n");
        builder.append("fbo_curve:  48, 0|566934 300000|566934 347937|518992  891879|366934 1041800|325000\n");
        builder.append("fbo_curve:  24, 0|566934 300000|566934 459419|407515  549256|366934  642089|325000\n");
        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(builder.toString().getBytes("UTF8")));
                
        
        FBOCurves instance = new FBOCurves(reader);
        double expResult = Double.NEGATIVE_INFINITY;
        double result = instance.get_topcon(window, volume);
        assertEquals(expResult, result, 0.0);
        
        expResult = 566934;
        result = instance.get_topcon(120, volume);
        assertEquals(expResult, result, 0.001);
        
        expResult = 518992;
        result = instance.get_topcon(48, 347937);
        assertEquals(expResult, result, 0.001);
        
        expResult = 325000;
        result = instance.get_topcon(120, 1670547);
        assertEquals(expResult, result, 0.001);
        
        result = instance.get_topcon(120, 2000000);
        assertEquals(expResult, result, 0.001);
        
        expResult= 566934;
        result = instance.get_topcon(120, -1 );
        assertEquals(expResult, result, 0.001);
        // TODO review the generated test code and remove the default call to fail.
        
    }
    
}
