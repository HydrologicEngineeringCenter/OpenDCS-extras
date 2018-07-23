/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;

import decodes.db.Database;
import decodes.db.DatabaseIO;
import decodes.db.TestDatabaseIO;
import decodes.sql.DbKey;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.TimeSeriesDb;
import decodes.xml.XmlDatabaseIO;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import spk.db.test.Fixtures;
import spk.db.test.TestDatabase;
import spk.db.test.TestDbTimeSeriesDAO;
import spk.db.test.UnitHelpers;

/**
 *
 * @author L2EDDMAN
 */
public class VolumeToFlowTest {
    
    VolumeToFlow instance = null;
    DbComputation comp = null;
    TimeSeriesDb db = new TestDatabase();
    Fixtures fixtures = null;
    
    public VolumeToFlowTest() throws Exception {
        this.fixtures = Fixtures.getFixtures(db);
        System.setProperty("user.timezone", "GMT");
        Fixtures.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    @Before
    public void setUp() throws Exception {
        Database _db = new Database();
        _db.setDbIo(new TestDatabaseIO("classpath:/decodes/xml"));
        _db.read();
        _db.unitConverterSet.prepareForExec();
        //_db.prepareForExec();
        
        comp = new DbComputation(DbKey.NullKey, "test");
        DbCompAlgorithm dbca = new DbCompAlgorithm("AcreFtToCFS");
        dbca.setExecClass("spk.algo.VolumeToFlow");
        comp.setAlgorithm(dbca);
        comp.setProperty("aggregateTimeZone", "PST8PDT");
        comp.setProperty("stor_EU", "ac-ft");
        comp.setProperty("flow_EU", "cfs");

    }

    @Test
    public void test15Minute() throws Exception {
        DbCompParm parm = new DbCompParm("stor", fixtures.getTimeSeriesKey("TEST.Stor.Inst.15Minutes.0.raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("flow", fixtures.getTimeSeriesKey("TEST.Flow.Inst.15Minutes.0.calc"), "15Minutes", null, 0);
        comp.addParm(parm);
        
        comp.prepareForExec(db);
        instance = (VolumeToFlow) comp.getExecutive();
  
        DataCollection dc = new DataCollection();
 
        UnitHelpers.prepForApply(instance, dc);

        instance.stor = 1;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertEquals("15 Minute flow from 15 minute storage incorrect", 1/.020618557,instance.flow.getDoubleValue(), .0000001);
    }

    @Test
    public void test1Hour() throws Exception {
        DbCompParm parm = new DbCompParm("stor", fixtures.getTimeSeriesKey("TEST.Stor.Inst.1Hour.0.raw"), "1Hour", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("flow", fixtures.getTimeSeriesKey("TEST.Flow.Ave.1Hour.1Hour.calc"), "1Hour", null, 0);
        comp.addParm(parm);
        
        comp.prepareForExec(db);
        instance = (VolumeToFlow) comp.getExecutive();
  
        DataCollection dc = new DataCollection();
 
        UnitHelpers.prepForApply(instance, dc);

        instance.stor = 1;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertEquals("1 Hour flow from 1 hour storage incorrect", 12.1,instance.flow.getDoubleValue(), .0000001);
    }

    @Test
    public void test1Day() throws Exception {
        DbCompParm parm = new DbCompParm("stor", fixtures.getTimeSeriesKey("TEST.Stor.Inst.1Day.0.raw"), "1Day", null, 0);
        comp.addParm(parm);
                                                                
        parm = new DbCompParm("flow", fixtures.getTimeSeriesKey("TEST.Flow.Ave.1Day.1Day.calc"), "1Day", null, 0);
        comp.addParm(parm);
        
        comp.prepareForExec(db);
        instance = (VolumeToFlow) comp.getExecutive();
  
        DataCollection dc = new DataCollection();
 
        UnitHelpers.prepForApply(instance, dc);
        UnitHelpers.setBaseTime(instance, Fixtures.sdf.parse("2018-07-10T00:00:00+0000"));
        instance.stor = 1;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertEquals("1 Day flow from 1 Day storage incorrect", 0.50417,instance.flow.getDoubleValue(), .0000001);
    }
    
    @Test
    public void testPsuedo1Day() throws Exception{
        DbCompParm parm = new DbCompParm("stor", fixtures.getTimeSeriesKey("TEST.Stor.Inst.~1Day.0.PST8PDT"), "1Day", null, 0);
        comp.addParm(parm);
                                                                
        parm = new DbCompParm("flow", fixtures.getTimeSeriesKey("TEST.Flow.Ave.~1Day.1Day.PST8PDT"), "1Day", null, 0);
        comp.addParm(parm);
        
        comp.setProperty("aggregateTimeZone", "PST8PDT");
        comp.prepareForExec(db);
        instance = (VolumeToFlow) comp.getExecutive();
  
        DataCollection dc = new DataCollection();
 
        UnitHelpers.prepForApply(instance, dc);
        UnitHelpers.setBaseTime(instance, Fixtures.sdf.parse("2018-07-10T00:00:00+0000"));
        instance.stor = 1;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertEquals("1 Day flow from 1 Day storage incorrect", 0.50417,instance.flow.getDoubleValue(), .0000001);
    }
    @Test
    public void testPsuedo1DayShortDay() throws Exception{
        DbCompParm parm = new DbCompParm("stor", fixtures.getTimeSeriesKey("TEST.Stor.Inst.~1Day.0.PST8PDT"), "1Day", null, 0);
        comp.addParm(parm);
                                                                
        parm = new DbCompParm("flow", fixtures.getTimeSeriesKey("TEST.Flow.Ave.~1Day.1Day.PST8PDT"), "1Day", null, 0);
        comp.addParm(parm);
        
        comp.setProperty("aggregateTimeZone", "PST8PDT");
        comp.prepareForExec(db);
        instance = (VolumeToFlow) comp.getExecutive();
  
        DataCollection dc = new DataCollection();
 
        UnitHelpers.prepForApply(instance, dc);
        UnitHelpers.setBaseTime(instance, Fixtures.sdf.parse("2018-03-12T07:00:00+0000"));
        //UnitHelpers.setCompProperty(instance, "aggregateTimeZone", "PST8PDT");
        instance.stor = 1;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertEquals("calculation incorrect", 0.52609, instance.flow.getDoubleValue(), .000001);
    }
    @Test
    public void testPsuedo1DayLongDay() throws Exception{
        DbCompParm parm = new DbCompParm("stor", fixtures.getTimeSeriesKey("TEST.Stor.Inst.~1Day.0.PST8PDT"), "1Day", null, 0);
        comp.addParm(parm);
                                                                
        parm = new DbCompParm("flow", fixtures.getTimeSeriesKey("TEST.Flow.Ave.~1Day.1Day.PST8PDT"), "1Day", null, 0);
        comp.addParm(parm);
        comp.setProperty("aggregateTimeZone", "PST8PDT");
        comp.prepareForExec(db);
        instance = (VolumeToFlow) comp.getExecutive();
  
        DataCollection dc = new DataCollection();
 
        UnitHelpers.prepForApply(instance, dc);
        UnitHelpers.setBaseTime(instance, Fixtures.sdf.parse("2018-11-05T08:00:00+0000"));
        UnitHelpers.setCompProperty(instance, "aggregateTimeZone", "PST8PDT");
        instance.stor = 1;
        
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertEquals("calculation incorrect", 0.48400, instance.flow.getDoubleValue(), .000001);
    }
    

    @Test
    public void testMetricInput() throws Exception {
        DbCompParm parm = new DbCompParm("stor", fixtures.getTimeSeriesKey("TEST.Stor.Inst.15Minutes.0.raw"), "15Minutes", null, 0);
        comp.addParm(parm);

        parm = new DbCompParm("flow", fixtures.getTimeSeriesKey("TEST.Flow.Inst.15Minutes.0.calc"), "15Minutes", null, 0);
        comp.addParm(parm);
        comp.setProperty("stor_EU", "m^3");
        comp.setProperty("flow_EU", "cfs");
        comp.prepareForExec(db);
        instance = (VolumeToFlow) comp.getExecutive();
  
        DataCollection dc = new DataCollection();
 
        UnitHelpers.prepForApply(instance, dc);

        instance.stor = 1233.4818375; // from http://www.kylesconverter.com/volume/acre--feet-to-cubic-meters
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        assertEquals("15 Minute flow from 15 minute storage incorrect", 1/.020618557,instance.flow.getDoubleValue(), .0001);
    }

}
