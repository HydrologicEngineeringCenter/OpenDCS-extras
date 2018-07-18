/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;

import decodes.cwms.CwmsFlags;
import decodes.sql.DbKey;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbComputation;
import decodes.tsdb.TimeSeriesDb;
import ilex.var.NoConversionException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import spk.db.test.TestDatabase;

/**
 *
 * @author L2EDDMAN
 */
public class BasinPrecipTest {

    TimeSeriesDb db = null;

    BasinPrecip instance = null;
    DbComputation comp = null;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public BasinPrecipTest() {
        db = new TestDatabase();

    }

    @Before
    public void setUp() throws DbCompException {
        DbCompAlgorithm dbca = new DbCompAlgorithm("BasinPrecip");
        dbca.setExecClass("spk.algo.BasinPrecip");
        instance = new BasinPrecip();
        comp = new DbComputation(DbKey.NullKey, "BasinPrecipTest");
        comp.setAlgorithmName("spk.algo.BasinPrecip");
        comp.setAlgorithm(dbca);
    }

    @Test
    public void testFailWithBNAP_0() throws DbCompException {
        //instance = new BasinPrecip();
        thrown.expect(DbCompException.class);
        thrown.expectMessage("You must specify a non zero bnap value.");
        instance.initAWAlgorithm();

    }

    @Test
    public void testFailWithALLNAP_0() throws DbCompException {
        //instance = new BasinPrecip();
        instance.bnap = 10;
        thrown.expect(DbCompException.class);
        thrown.expectMessage("You must specifiy at least 1 non zero nap value");
        instance.initAWAlgorithm();

    }

    @Test
    public void testInitAWAlgorithmMinReqs() throws DbCompException {
        //instance = new BasinPrecip();
        instance.bnap = 10;
        instance.nap1 = 10;
        instance.initAWAlgorithm();
        assertTrue(true);
    }

    @Test
    public void testTimeSliceAllInputMissing() throws DbCompException {

        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");

        instance.init(comp, db);
        instance.rain1 = Double.NEGATIVE_INFINITY;
        instance.rain2 = Double.NEGATIVE_INFINITY;
        instance.rain3 = Double.NEGATIVE_INFINITY;
        instance.rain4 = Double.NEGATIVE_INFINITY;
        instance.rain5 = Double.NEGATIVE_INFINITY;
        instance.rain6 = Double.NEGATIVE_INFINITY;
        instance.rain7 = Double.NEGATIVE_INFINITY;
        instance.rain8 = Double.NEGATIVE_INFINITY;
        instance.doAWTimeSlice();
        assertFalse("Algorithm attempted to make an output", instance.BasinPrecip.isChanged());
    }

    @Test
    public void testTimeSliceOneInput() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");

        instance.init(comp, db);
        instance.rain1 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", 1, instance.BasinPrecip.getDoubleValue(), .0001);
    }

    @Test
    public void testTimeSliceTwoInputs() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+10.0))*(1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }

    @Test
    public void testTimeSliceThreeInputs() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+10.0+10.0))*(1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }

    @Test
    public void testTimeSliceFourInputs() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+10.0+10.0+10.0))*(1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }

    @Test
    public void testTimeSliceFiveInputs() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+10.0+10.0+10.0+10.0))*(1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }

    @Test
    public void testTimeSliceSixInputs() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }

    @Test
    public void testTimeSliceSevenInputs() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }

    @Test
    public void testTimeSliceEightInputs() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+10.0+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+1.0+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }

    @Test
    public void testTimeSliceRain1MissingCalculatesCorrectly() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = Double.NEGATIVE_INFINITY;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain2MissingCalculatesCorrectly() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = Double.NEGATIVE_INFINITY;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain3MissingCalculatesCorrectly() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = Double.NEGATIVE_INFINITY;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain4MissingCalculatesCorrectly() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = Double.NEGATIVE_INFINITY;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain5MissingCalculatesCorrectly() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = Double.NEGATIVE_INFINITY;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain6MissingCalculatesCorrectly() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = Double.NEGATIVE_INFINITY;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain7MissingCalculatesCorrectly() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = Double.NEGATIVE_INFINITY;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain8MissingCalculatesCorrectly() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = Double.NEGATIVE_INFINITY;
        instance.doAWTimeSlice();
        assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain1MissingFlagsQuestionable() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = Double.NEGATIVE_INFINITY;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        int flags = instance.BasinPrecip.getFlags();
        int questionable = flags & CwmsFlags.VALIDITY_QUESTIONABLE;
        assertEquals("Data was not flagged as questionable",questionable, CwmsFlags.VALIDITY_QUESTIONABLE);
        //assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain2MissingFlagsQuestionable() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = Double.NEGATIVE_INFINITY;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        int flags = instance.BasinPrecip.getFlags();
        int questionable = flags & CwmsFlags.VALIDITY_QUESTIONABLE;
        assertEquals("Data was not flagged as questionable",questionable, CwmsFlags.VALIDITY_QUESTIONABLE);
        //assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain3MissingFlagsQuestionable() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = Double.NEGATIVE_INFINITY;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        int flags = instance.BasinPrecip.getFlags();
        int questionable = flags & CwmsFlags.VALIDITY_QUESTIONABLE;
        assertEquals("Data was not flagged as questionable",questionable, CwmsFlags.VALIDITY_QUESTIONABLE);
        //assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain4MissingFlagsQuestionable() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = Double.NEGATIVE_INFINITY;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        int flags = instance.BasinPrecip.getFlags();
        int questionable = flags & CwmsFlags.VALIDITY_QUESTIONABLE;
        assertEquals("Data was not flagged as questionable",questionable, CwmsFlags.VALIDITY_QUESTIONABLE);
        //assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain5MissingFlagsQuestionable() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = Double.NEGATIVE_INFINITY;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        int flags = instance.BasinPrecip.getFlags();
        int questionable = flags & CwmsFlags.VALIDITY_QUESTIONABLE;
        assertEquals("Data was not flagged as questionable",questionable, CwmsFlags.VALIDITY_QUESTIONABLE);
        //assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain6MissingFlagsQuestionable() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = Double.NEGATIVE_INFINITY;
        instance.rain7 = 1;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        int flags = instance.BasinPrecip.getFlags();
        int questionable = flags & CwmsFlags.VALIDITY_QUESTIONABLE;
        assertEquals("Data was not flagged as questionable",questionable, CwmsFlags.VALIDITY_QUESTIONABLE);
        //assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain7MissingFlagsQuestionable() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = Double.NEGATIVE_INFINITY;
        instance.rain8 = 1;
        instance.doAWTimeSlice();
        int flags = instance.BasinPrecip.getFlags();
        int questionable = flags & CwmsFlags.VALIDITY_QUESTIONABLE;
        assertEquals("Data was not flagged as questionable",questionable, CwmsFlags.VALIDITY_QUESTIONABLE);
        //assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    @Test
    public void testTimeSliceRain8MissingFlagsQuestionable() throws DbCompException, NoConversionException {
        comp.setProperty("bnap", "10");
        comp.setProperty("nap1", "10");
        comp.setProperty("nap2", "10");
        comp.setProperty("nap3", "10");
        comp.setProperty("nap4", "10");
        comp.setProperty("nap5", "10");
        comp.setProperty("nap6", "10");
        comp.setProperty("nap7", "10");
        comp.setProperty("nap8", "10");
        instance.init(comp, db);
        instance.rain1 = 1;
        instance.rain2 = 1;
        instance.rain3 = 1;
        instance.rain4 = 1;
        instance.rain5 = 1;
        instance.rain6 = 1;
        instance.rain7 = 1;
        instance.rain8 = Double.NEGATIVE_INFINITY;
        instance.doAWTimeSlice();
        int flags = instance.BasinPrecip.getFlags();
        int questionable = flags & CwmsFlags.VALIDITY_QUESTIONABLE;
        assertEquals("Data was not flagged as questionable",questionable, CwmsFlags.VALIDITY_QUESTIONABLE);
        //assertEquals("Calculation incorrected", (10.0/(10.0+/*10.0*/+10.0+10.0+10.0+10.0+10.0+10.0))*(1.0+/*1.0*/+1.0+1.0+1.0+1.0+1.0+1.0), instance.BasinPrecip.getDoubleValue(), .0001);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
