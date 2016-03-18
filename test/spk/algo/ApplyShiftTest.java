/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.algo;

import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbComputation;
import decodes.tsdb.DbIoException;
import decodes.tsdb.DuplicateTimeSeriesException;
import ilex.var.TimedVariable;
import java.util.Date;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class ApplyShiftTest {
    
    public ApplyShiftTest() {
    }

    /**
     * Test of initAWAlgorithm method, of class ApplyShift.
     */
    @Test
    public void testInitAWAlgorithm() throws Exception {
    }

    /**
     * Test of beforeTimeSlices method, of class ApplyShift.
     */
    @Test
    public void testBeforeTimeSlices() throws Exception {
    }

    /**
     * Test of doAWTimeSlice method, of class ApplyShift.
     */
    @Test
    public void testDoAWTimeSlice() throws Exception {
        ApplyShift instance = new ApplyShift();
        instance.beforeTimeSlices();
        
        
    }

    /**
     * Test of afterTimeSlices method, of class ApplyShift.
     */
    @Test
    public void testAfterTimeSlices() throws Exception {
    
        
        
    }

    /**
     * Test of getInputNames method, of class ApplyShift.
     */
    @Test
    public void testGetInputNames() {
    }

    /**
     * Test of getOutputNames method, of class ApplyShift.
     */
    @Test
    public void testGetOutputNames() {
    }

    /**
     * Test of getPropertyNames method, of class ApplyShift.
     */
    @Test
    public void testGetPropertyNames() {
    }
       
    /**
     * Test of getShift method, of class ApplyShift.
     */
    @Test
    public void testGetShift() {
    }
    
    
    @Test
    public void testAlgorithm() throws DuplicateTimeSeriesException, DbCompException, DbIoException{
        
        
        
        
    }
    
}
