/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waives copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.support.Alarms;

import decodes.db.DatabaseException;
import decodes.db.IdDatabaseObject;
import decodes.db.IncompleteDatabaseException;
import decodes.db.InvalidDatabaseException;
import decodes.sql.DbKey;
import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DbIoException;
import decodes.tsdb.TimeSeriesDb;
import decodes.tsdb.TimeSeriesIdentifier;
import ilex.util.HasProperties;
import ilex.var.NoConversionException;
import ilex.var.TimedVariable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import opendcs.dao.CachableDbObject;
import spk.algo.AWAggTemplate;



/**
 * Implements the various condition checks
 * greater than
 * less than
 * is equal
 * within range
 * rate of charge
 * 
 * all checks can have an arbitrary duration, e.g. for greater than 
 * the condition could be true only if the current value and value from the previous 
 * hour is greater than some number, or the previous 3 hours, etc.
 * 
 * @author L2EDDMAN
 */
public class Threshold 
        extends IdDatabaseObject
	implements HasProperties, CachableDbObject
{
    
    
    private String check;
    private String check_str;
    private double a;
    private double b;
    
    //private ArrayList actions
    private String color;
    private int priority;
    private String units;
    private DbKey site;
    private int duration; // duration in seconds
    public Threshold(){
        
    }
    
    public Threshold( String condition ){
        // give a string like >5:5H
        check_str = condition;
        String parts[] = condition.split(":");
        
        // get the duration
        String str = parts[1];
        if( str.charAt(0) == '0'){
            duration = 0;
        } else{
            String number="";
            char interval='S';
            for( int i = 0; i < str.length(); i++){
                if( Character.isDigit( str.charAt(i)) ){
                    number = number + str.charAt(i);
                }else{
                    interval = str.charAt(i);
                    break;// where done when we get here
                }
            }
            
            int num = Integer.parseInt(number);
            switch( interval ){
                case 'H':{
                    duration = num *3600;
                    break;
                }
                case 'D':{
                    duration = num * 3600*24;
                    break;
                }
                case 'W':{
                    duration = num * 3600*24*7;
                    break;
                }
                case 'S':{                   
                }
                default:{
                    duration = num;
                }
            }
        }
        
        str = parts[0];
        
        switch( str.charAt(0) ){
            case 'm':{
                this.check = CHECK_MISSING;
                break;
            }
            case '>':{
                this.check = CHECK_GREATER;
                this.a = Double.parseDouble(str.substring(1));
                break;
            }
            case '<':{
                this.check = CHECK_LESS;
                this.a = Double.parseDouble(str.substring(1));
                break;
            }
            case '=':{
                if( str.charAt(1) != '='){
                    this.check = CHECK_EQUALS;
                    this.a = Double.parseDouble(str.substring(1));
                } else{
                    this.check = CHECK_STATIC;
                    this.a = Double.parseDouble( str.substring(2));
                    /*
                    TODO: if a is < 0. Check will be if outside the tolerance zone.
                    as in, this value should stay static, or increase/decrease very slowly.
                    Or it just has a specific range it should stay within.
                    (this would be shorthand for a combined greater than, less than check
                    */
                }
                break;
            }
            case 'c':{
                this.check = CHECK_ROC;
                parts = str.substring(1).split(";");
                a = Double.parseDouble( parts[0] );
                b = Double.parseDouble( parts[1] );
                break;
            }
                
            
        }
    }

    public static int indexOfDurationStart( int duration, Date end, CTimeSeries ts, int fudge ){
        Date start = new Date( end.getTime() - duration*1000);
        for( int i = 0; i < ts.size(); i++){
            Date cur = ts.sampleAt(i).getTime();
            long diff = Math.abs( start.getTime() - cur.getTime() ) / 1000L;
            if( diff <= fudge){
                return i;
            }
            
        }
        
        return -1;
    }
    
    
    
    public boolean check( CTimeSeries ts ) throws BadTimeSeriesException, NoConversionException{                
        
        if( ts.size() == 0){
            if( check.equalsIgnoreCase("M")){
                return true;
            }
            return false; // no alarm, if a missing check isn't defined somewhere else, we don't care.
        }
        int index_of_first = -1;
        TimedVariable first = null;
        
        
        TimedVariable last = ts.sampleAt( ts.size() - 1 );
        
        double last_val = last.getDoubleValue();
        Date last_date = last.getTime();
        
        if( duration == 0){
            first = last;
        } else{
            index_of_first = indexOfDurationStart(duration, last_date, ts, 60);
            if( index_of_first >=0){
                first = ts.sampleAt(index_of_first);
            } else{
                //insufficient data
                return false;
            }
        }
        
        
        double first_val = first.getDoubleValue();
        Date first_date = first.getTime();
        
        
        
        if( check.equalsIgnoreCase(CHECK_GREATER)){
            if( last_val > a && first_val > a){
                return true;
            }                

        }
        else if( check.equalsIgnoreCase(CHECK_LESS) ){                
            if( last_val < a && first_val < a ){
                return true;
            }

        }
        else if( check.equalsIgnoreCase(CHECK_EQUALS) ){
            
            if( last_val == a && first_val == a){
                return true;
            }


        }            
        else if( check.equalsIgnoreCase(CHECK_STATIC) ){
            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;
            for(int i=index_of_first;i<ts.size(); i++ ){
                TimedVariable tv2 = ts.sampleAt(i);
                double v = tv2.getDoubleValue();
                if( v > max){
                    max = v;
                }
                if( v < min){
                    min = v;
                }
            }
            if( Math.abs(max-min) < a){
                return true;
            }
                // a is tolerance for this check
                // need to do some sort of variance check or something
        
        }
        else if( check.equalsIgnoreCase(CHECK_ROC) ){
                                
            double diff = last_val - first_val;
            if( diff >= 0){
                if( diff > a){
                    return true;
                }
            } else{
                if( Math.abs(diff) > b ){
                    return true;
                }
            }

            
        }            
        else{
           return false;
        }
        return false;
    }
    
    public String get_check_str(){
        return String.format("%s%f,%f:%d", this.check,this.a,this.b,this.priority );
    }
    
    public String get_check_type(){
        return this.check;
    }
    
    public void set_check_type(String type){
        this.check = type;
    }
    
    public int get_duration(){
        return this.duration;
    }
    
    public void set_duration( int duration ){
        this.duration = duration;
    }
    
    public double get_a( ){ return a; }
    public void set_a( double a ){ this.a = a; }
    
    public double get_b(){ return b; }
    public void get_b( double b ){ this.b = b;}
    
    public void set_color( String color ){ this.color = color;}
    public String get_color( ){ return color; }
    
    public int get_priority(){return priority;}
    public void set_priority( int priority ){ this.priority = priority;}
    public String get_units( ){ return units;}
    public void set_units( String units ) { this.units = units;}
    
    public DbKey get_sdi(){return site;}
    public void set_sdi(DbKey site){ this.site = site;}
    
    
    
    
    
    
    
    
    protected boolean isMissing(double var) {
	return var == 4.9E-324 || var == Double.NEGATIVE_INFINITY;
    }
    
    protected boolean isMissing(long var) {
	return var == -9223372036854775808L;
    }
    
    protected boolean isMissing(String var) {
	return var == null;
    }
    
    
    
    
    
    final public static String CHECK_MISSING = "m";
    final public static String CHECK_GREATER = ">";
    final public static String CHECK_LESS    = "<";
    final public static String CHECK_EQUALS  = "=";
    final public static String CHECK_ROC     = "c";
    final public static String CHECK_STATIC  = "==";

    @Override
    public String getObjectType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void prepareForExec() throws IncompleteDatabaseException, InvalidDatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isPrepared() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void read() throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void write() throws DatabaseException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProperty(String name, String value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getProperty(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Enumeration getPropertyNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void rmProperty(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getUniqueName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
