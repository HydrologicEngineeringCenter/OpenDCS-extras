/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opendcs.db.test;

import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DataCollection;
import decodes.tsdb.DbAlgorithmExecutive;
import decodes.tsdb.DbCompException;
import decodes.tsdb.DbCompParm;
import decodes.tsdb.DbComputation;
import decodes.tsdb.DbIoException;
import decodes.tsdb.DuplicateTimeSeriesException;
import decodes.tsdb.algo.AW_AlgorithmBase;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashSet;
import java.util.TreeSet;
import opendcs.dai.TimeSeriesDAI;
import spk.algo.support.AlgBaseNew;

/**
 * There's a lot of abstraction in the OpenDCS code base
 * This set of helpers is for when unit tests need to force
 * their way into private members and methods to get things 
 * setup for the test
 * @author L2EDDMAN
 */
public class UnitHelpers {
    /**
     * 
     * @param instance
     * @param dt
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    public static void setBaseTime( AW_AlgorithmBase instance, Date dt ) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        Class<?> clz = instance.getClass().getSuperclass();
        Field tsbt = clz.getDeclaredField("_timeSliceBaseTime");
        tsbt.setAccessible(true);
        tsbt.set(instance, dt);
    }
    /**
     * version of setBaseTime for SPK custom alg base
     * @param instance
     * @param dt
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    public static void setBaseTime( AlgBaseNew instance, Date dt ) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        Class<?> clz = instance.getClass().getSuperclass();
        Field tsbt = clz.getDeclaredField("_timeSliceBaseTime");
        tsbt.setAccessible(true);
        tsbt.set(instance, dt);
    }
    
    /**
     * 
     * @param instance
     * @param dt
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    public static void determineBaseTimes( AW_AlgorithmBase instance ) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
        Class<?> clz = instance.getClass().getSuperclass();
        Field tsbt = clz.getDeclaredField("baseTimes");
        Method determineBaseTimes = clz.getSuperclass().getDeclaredMethod("determineInputBaseTimes");
        tsbt.setAccessible(true);
        determineBaseTimes.setAccessible(true);
        TreeSet<Date> baseTimes = (TreeSet<Date>) determineBaseTimes.invoke(instance);
        tsbt.set(instance, baseTimes);
    }
    
    /**
     * 
     * @param instance
     * @param dt
     * @throws NoSuchFieldException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException 
     */
    public static void determineBaseTimes( AlgBaseNew instance ) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
        Class<?> clz = instance.getClass().getSuperclass();
        Field tsbt = clz.getDeclaredField("baseTimes");
        Method determineBaseTimes = clz.getSuperclass().getDeclaredMethod("determineInputBaseTimes");
        determineBaseTimes.setAccessible(true);
        tsbt.setAccessible(true);
        TreeSet<Date> baseTimes = (TreeSet<Date>) determineBaseTimes.invoke(instance);
        tsbt.set(instance, baseTimes);
    }
    
    
    
    /**
     * 
     * @param instance assumed to be a sub class of AW_AlgorithmBase, or at least 2 classes away from DbAlgorithmExecutive
     * @param dc 
     */
    public static void prepForApply( DbAlgorithmExecutive /*AW_AlgorithmBase*/ instance, DataCollection dc ) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException{
        
        Class<?> clz = instance.getClass().getSuperclass().getSuperclass();
        Field fldDC = clz.getDeclaredField("dc");
        //Field fldBaseTimes = clz.getField("baseTimes");
        fldDC.setAccessible(true);
        //fldBaseTimes.setAccessible(true);
        fldDC.set(instance, dc);
        
        Method evalRange = clz.getDeclaredMethod("evaluateEffectiveRange");
        Method modelID = clz.getDeclaredMethod("determineModelRunId", DataCollection.class);
        Method addTsToParmRef = clz.getDeclaredMethod("addTsToParmRef", String.class, boolean.class);
        Method determineInputBaseTimes = clz.getDeclaredMethod("determineInputBaseTimes");
        
        evalRange.setAccessible(true);
        modelID.setAccessible(true);
        addTsToParmRef.setAccessible(true);
        determineInputBaseTimes.setAccessible(true);
        
        evalRange.invoke(instance);
        modelID.invoke(instance, dc);
        
        
        for(String role : instance.getInputNames())
			addTsToParmRef.invoke(instance, role, false);
		for(String role : instance.getOutputNames())
			addTsToParmRef.invoke(instance, role, true);
        
       //TreeSet<Date> baseTimes = (TreeSet<Date>) determineInputBaseTimes.invoke(instance);
       //fldBaseTimes.set(instance, baseTimes);
        
    }

    public static DataCollection getCompData(DbComputation comp,TimeSeriesDAI tsdai, Date start, Date end) throws DuplicateTimeSeriesException, DbIoException, BadTimeSeriesException {
        DataCollection dc = new DataCollection();
        for (DbCompParm p : comp.getParmList()) {
            CTimeSeries cts = new CTimeSeries(p);
            tsdai.fillTimeSeries(cts, start, end);
            try{
                dc.addTimeSeries(cts);
            } catch ( DuplicateTimeSeriesException e ){
                // just don't add it...though figure out why it was happening in the remaining runoff after a minor change
            }
        }
        return dc;
    }

    
    public static void setCompProperty(DbAlgorithmExecutive instance, String prop, String val) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clz = instance.getClass().getSuperclass().getSuperclass();
        Field fldDC = clz.getDeclaredField(prop);
        fldDC.setAccessible(true);
        fldDC.set(instance, val);
    }
    
    public static DbComputation getComp(DbAlgorithmExecutive instance) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException
    {
        Class<?> clz = instance.getClass().getSuperclass().getSuperclass();
        Field fldComp = clz.getDeclaredField("comp");
        fldComp.setAccessible(true);
        return (DbComputation) fldComp.get(instance);
    }

    public static void setAggPeriodEnd(DbAlgorithmExecutive instance, Date date) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Class<?> clz = instance.getClass().getSuperclass();//.getSuperclass();
        Field fld = clz.getDeclaredField("_aggregatePeriodEnd");
        fld.setAccessible(true);
        fld.set(instance, date);
    }

   
    
}
