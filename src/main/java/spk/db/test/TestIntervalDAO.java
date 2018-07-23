/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.db.test;

import decodes.db.IntervalList;
import decodes.tsdb.DbIoException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import opendcs.dai.IntervalDAI;
import opendcs.opentsdb.Interval;
import opendcs.opentsdb.OpenTsdbIntervalDAO;
import spk.algo.support.Resource;

/**
 *
 * @author L2EDDMAN
 */
class TestIntervalDAO extends OpenTsdbIntervalDAO {

    String validIntervals[] = null;
    
    public TestIntervalDAO(TestDatabase db) {
        super(db);
    }

    @Override
    public void loadAllIntervals() throws DbIoException {
        IntervalList list = IntervalList.instance();

        try ( InputStream is = Resource.fromURI(URI.create("classpath:/database/intervals.txt"));BufferedReader reader = new BufferedReader(new InputStreamReader(is))){
            
            String line = null;
            while( (line = reader.readLine()) != null ){
                
            }
            
        } catch (Exception ex) {
            throw new DbIoException(ex.getLocalizedMessage());
        }
        
        
        // All intervals, including built-ins are valid in OpenTSDB.
        validIntervals = new String[IntervalList.instance().getList().size()];
        int i = 0;
        for (Interval intv : IntervalList.instance().getList()) {
            validIntervals[i++] = intv.getName();
        }
        
    }

    @Override
    public void writeInterval(Interval intv) throws DbIoException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        // intentional no-op
    }

}
