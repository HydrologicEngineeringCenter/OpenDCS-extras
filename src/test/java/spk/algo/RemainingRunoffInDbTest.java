/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spk.algo;

import decodes.db.Database;
import decodes.db.TestDatabaseIO;
import decodes.sql.DbKey;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbCompAlgorithm;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import ilex.var.TimedVariable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.TimeZone;
import opendcs.dai.TimeSeriesDAI;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import spk.db.test.Fixtures;
import spk.db.test.TestDatabase;
import spk.db.test.UnitHelpers;

/**
 *
 * @author L2EDDMAN
 */
@RunWith(MockitoJUnitRunner.class)
public class RemainingRunoffInDbTest {

    TestDatabase db = null;
    TimeSeriesDAI tsdai = null;
    Fixtures fixtures = null;
    RemainingRunoffInDb instance = null;
    CTimeSeries output_ts = null;
    Date before_season  = null;    
    Date after_season = null;
    Date flow_trigger = null;
    Date runoff_trigger = null;
    Date negative = null;
    Date start = null;
    Date end = null;
    Date would_be_negative = null;
    private final String inflow_ts_name = "TEST.Stor-In.Ave.1Day.1Day.raw";
    private final String forecast_ts_name = "TEST.Stor-Runoff.Inst.0.0.raw";
    private final String output_ts_name = "TEST.Stor-Runoff.Inst.1Day.0.calc";
    
    @Mock
    private Connection conn;

    @Mock
    private DatabaseMetaData md;
    
    @Mock
    private Statement stmt;
    
    @Mock
    private PreparedStatement stmt_ts_id;
    
    @Mock
    private ResultSet rs_decodes_ver;
    
    @Mock
    private ResultSet rs_data;

    public RemainingRunoffInDbTest() throws Exception {
        Fixtures.sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        System.setProperty("user.timezone", "UTC");
        start = Fixtures.sdf.parse("2018-01-28T00:00:00+0000");
        end = Fixtures.sdf.parse("2018-08-05T00:00:00+0000");
        before_season = Fixtures.sdf.parse("2018-01-28T00:00:00+0000");
        after_season = Fixtures.sdf.parse("2018-08-02T00:00:00+0000");
        flow_trigger = Fixtures.sdf.parse("2018-02-05T00:00:00+0000");
        runoff_trigger = Fixtures.sdf.parse("2018-02-01T00:00:00+0000");
        would_be_negative = Fixtures.sdf.parse("2018-7-29T00:00:00+0000");
    }
    
    @Before
    public void setUp() throws Exception {
        assertNotNull(conn);
        when( conn.getMetaData() ).thenReturn(md);
        when( md.getDatabaseProductName() ).thenReturn("unitestdb");
        when( md.getDatabaseProductVersion()).thenReturn("1");
        when( rs_decodes_ver.getInt(1)).thenReturn(5);
        when( conn.createStatement()).thenReturn(stmt);
        when( stmt.executeQuery("SELECT * FROM DecodesDatabaseVersion")).thenReturn(rs_decodes_ver);
        
        db = new TestDatabase();
        db.setConnection(conn);
        tsdai = db.makeTimeSeriesDAO();
        fixtures = Fixtures.getFixtures(db);
        Database _db = new Database();
        _db.setDbIo(new TestDatabaseIO("classpath:/decodes/xml"));
        _db.read();
        _db.unitConverterSet.prepareForExec();
        
        
        DbComputation comp = new DbComputation(DbKey.NullKey, "test");
        DbCompAlgorithm dbca = new DbCompAlgorithm("RemainingRunoffInDb");
        dbca.setExecClass("spk.algo.RemainingRunoffInDb");
        comp.setAlgorithm(dbca);
        comp.addParm(new DbCompParm("forecasted_runoff",fixtures.getTimeSeriesKey(forecast_ts_name),"0","Inst.0.raw",0));
        comp.addParm(new DbCompParm("inflow",fixtures.getTimeSeriesKey(inflow_ts_name),"1Day","Inst.1Day.raw",0));
        comp.addParm(new DbCompParm("runoff_remaining",fixtures.getTimeSeriesKey(output_ts_name),"1Day","Inst.1Day.raw",0));
        comp.setProperty("aggregateTimeZone", "UTC");
        comp.setProperty("inflow_EU", "ac-ft");
        comp.setProperty("forecasted_runoff","ac-ft");
        comp.setProperty("runoff_remaining","ac-ft");
        
        comp.prepareForExec(db);
  
        instance = (RemainingRunoffInDb) comp.getExecutive();

        DataCollection dc = UnitHelpers.getCompData(comp, tsdai, start, end);
        
        UnitHelpers.prepForApply(instance, dc);
        
        
        output_ts = tsdai.makeTimeSeries( tsdai.getTimeSeriesIdentifier(output_ts_name) );
        tsdai.fillTimeSeries(output_ts, start, end);
    }

    @Test
    public void testBeforeTimeSlices() throws Exception {        
        instance.beforeTimeSlices();
    }

    @Test
    public void testTriggeredByInflow() throws Exception {
        CTimeSeries input = instance.getDataCollection().getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(inflow_ts_name));
        TimedVariable tv = input.findWithin(flow_trigger, 200);
        UnitHelpers.setBaseTime(instance, flow_trigger);
        instance.inflow = tv.getDoubleValue();
        instance.forecasted_runoff = Double.NEGATIVE_INFINITY;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        instance.afterTimeSlices();
        
        CTimeSeries output = instance.getParmRef("runoff_remaining").timeSeries;
        TimedVariable tv_out = output.findWithin(flow_trigger, 200);
        assertNotNull("Calculated output was null",tv_out);
        assertTrue("Calculated output was never saved by computation", tv_out.isChanged());
        
        TimedVariable tv_output_ts = output_ts.findWithin(flow_trigger, 200);
        assertNotNull("TestData output was null",tv_out);
                
        assertEquals("Remaining runoff not calculated correctly",tv_output_ts.getDoubleValue(),tv_out.getDoubleValue(),0.001);
        
    }

    @Test
    public void testTriggeredByRunoff() throws Exception {
        //CTimeSeries input = instance.getDataCollection().getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey("TEST.Stor-In.Ave.1Day.1Day.raw"));
        CTimeSeries forecast = instance.getDataCollection().getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(forecast_ts_name));
        
        // setup Mock results for triggered by inflow
        DbComputation comp = UnitHelpers.getComp(instance);
        comp.getTriggeringRecNums().add(1);
        
        when(conn.prepareStatement("select site_datatype_id from ccp.cp_comp_tasklist where record_num = ?")).thenReturn(stmt_ts_id);
        when(stmt_ts_id.executeQuery()).thenReturn(rs_data);
        when(rs_data.next()).thenReturn(Boolean.TRUE);
        when(rs_data.getLong(1)).thenReturn(fixtures.getTimeSeriesKey(forecast_ts_name).getValue());
        
        
        TimedVariable tv = forecast.findWithin(runoff_trigger, 200);
        UnitHelpers.setBaseTime(instance, runoff_trigger);
        instance.inflow = Double.NEGATIVE_INFINITY;
        instance.forecasted_runoff = tv.getDoubleValue();
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        instance.afterTimeSlices();
        
        CTimeSeries output = instance.getParmRef("runoff_remaining").timeSeries;
        TimedVariable tv_out = output.findWithin(flow_trigger, 200);
        assertNotNull("Calculated output was null",tv_out);
        assertTrue("Calculated output was never saved by computation", tv_out.isChanged());
        
        TimedVariable tv_output_ts = output_ts.findWithin(flow_trigger, 200);
        assertNotNull("TestData output was null",tv_out);
                
        assertEquals("Remaining runoff not calculated correctly",tv_output_ts.getDoubleValue(),tv_out.getDoubleValue(),0.001);
        
    }

    @Test
    public void testBeforeSeason() throws Exception{
        CTimeSeries inflow = instance.getDataCollection().getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(inflow_ts_name));
        TimedVariable tv = inflow.findWithin(before_season, 200);
        UnitHelpers.setBaseTime(instance, before_season);
        instance.inflow = tv.getDoubleValue();
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        instance.afterTimeSlices();
        
        CTimeSeries output = instance.getParmRef("runoff_remaining").timeSeries;
        TimedVariable tv_out = output.findWithin(before_season, 200);
        assertNotNull(tv_out);
        assertTrue(tv_out.isChanged());
        assertEquals("Remaining runoff should be zero before February",0,tv_out.getDoubleValue(),0.01);
    }
    
    @Test
    public void testAfterSeason() throws Exception{
        CTimeSeries input = instance.getDataCollection().getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(inflow_ts_name));
        TimedVariable tv = input.findWithin(after_season, 200);
        UnitHelpers.setBaseTime(instance, after_season);
        instance.inflow = tv.getDoubleValue();
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        instance.afterTimeSlices();
                
        CTimeSeries output = instance.getParmRef("runoff_remaining").timeSeries;
        TimedVariable tv_out = output.findWithin(after_season, 200);
        assertNotNull(tv_out);
        assertTrue(tv_out.isChanged());
        assertEquals("Remaining runoff should be zero before August",0,tv_out.getDoubleValue(),0.01);
    }
    
    @Test
    public void testFloorValue() throws Exception {
        CTimeSeries input = instance.getDataCollection().getTimeSeriesByUniqueSdi(fixtures.getTimeSeriesKey(inflow_ts_name));
        TimedVariable tv = input.findWithin(would_be_negative, 200);
        UnitHelpers.setBaseTime(instance, would_be_negative);
        instance.inflow = tv.getDoubleValue();
        instance.forecasted_runoff = Double.NEGATIVE_INFINITY;
        instance.beforeTimeSlices();
        instance.doAWTimeSlice();
        instance.afterTimeSlices();
        
        CTimeSeries output = instance.getParmRef("runoff_remaining").timeSeries;
        TimedVariable tv_out = output.findWithin(would_be_negative, 200);
        assertNotNull("Calculated output was null",tv_out);
        assertTrue("Calculated output was never saved by computation", tv_out.isChanged());
        
        TimedVariable tv_output_ts = output_ts.findWithin(would_be_negative, 200);
        assertNotNull("TestData output was null",tv_out);
                
        assertEquals("Remaining Runoff should never be below zero",tv_output_ts.getDoubleValue(),tv_out.getDoubleValue(),0.001);
        
    
    }
    
}
