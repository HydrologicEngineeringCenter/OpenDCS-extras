/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waives copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.support.Alarms;

import decodes.tsdb.BadTimeSeriesException;
import decodes.tsdb.CTimeSeries;
import decodes.tsdb.DbIoException;
import decodes.tsdb.TimeSeriesDb;
import ilex.var.NoConversionException;
import ilex.var.TimedVariable;
import java.util.ArrayList;
import java.util.Date;
import spk.algo.AWAggTemplate;



/**
 *
 * @author L2EDDMAN
 */
public class AlarmCondition {
    
    
    private int check;
    private String check_str;
    private double a;
    private double b;
    
    private int duration; // duration in seconds
    
    public AlarmCondition( String condition ){
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
            if( check == CHECK_MISSING){
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
            ts.sampleAt(0);
        } else{
            index_of_first = indexOfDurationStart(duration, last_date, ts, check);
            if( index_of_first >=0){
                ts.sampleAt(index_of_first);
            } else{
                //insufficient data
                return false;
            }
        }
        
        
        double first_val = first.getDoubleValue();
        Date first_date = first.getTime();
        
        
        
        switch( check ){        
            case CHECK_GREATER:{
                    if( last_val > a && first_val > a){
                        return true;
                    }                
                break;
            }
            case CHECK_LESS:{                
                if( last_val < a && first_val < a ){
                    return true;
                }
                break;                
            }
            case CHECK_EQUALS:{                
                if( last_val == a && first_val == a){
                    return true;
                }
                
                break;
            }            
            case CHECK_STATIC:{
                double max = Double.NEGATIVE_INFINITY;
                double min = Double.POSITIVE_INFINITY;
                for(int i=0;i<ts.size(); i++ ){
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
                break;
            }
            case CHECK_ROC:{
                                
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
                
                break;
            }            
            default: return false;
        }
        return false;
    }
    
    public String get_check(){
        return this.check_str;
    }
    
    
    
    public int get_duration(){
        return this.duration;
    }
    
    protected boolean isMissing(double var) {
	return var == 4.9E-324 || var == Double.NEGATIVE_INFINITY;
    }
    
    protected boolean isMissing(long var) {
	return var == -9223372036854775808L;
    }
    
    protected boolean isMissing(String var) {
	return var == null;
    }
    
    
    final public static int CHECK_MISSING = 1;
    final public static int CHECK_GREATER = 1 << 2;
    final public static int CHECK_LESS    = 1 << 3;
    final public static int CHECK_EQUALS  = 1 << 4;
    final public static int CHECK_ROC     = 1 << 5;
    final public static int CHECK_STATIC  = 1 << 6;
    
}
