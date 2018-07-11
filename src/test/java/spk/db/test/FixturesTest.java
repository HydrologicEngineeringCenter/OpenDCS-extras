/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.db.test;

import decodes.tsdb.CTimeSeries;
import decodes.tsdb.TimeSeriesDb;
import opendcs.dai.TimeSeriesDAI;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author L2EDDMAN
 */
public class FixturesTest {
    
    public FixturesTest() {
    }

    /**
     * Test of getFixtures method, of class Fixtures.
     */
    @Test
    public void testGetFixtures() throws Exception {
        TimeSeriesDb tsdb = new TestDatabase();
        TimeSeriesDAI tsdai = tsdb.makeTimeSeriesDAO();
        Fixtures fixtures = Fixtures.getFixtures(tsdai);

    }

    
    
}
