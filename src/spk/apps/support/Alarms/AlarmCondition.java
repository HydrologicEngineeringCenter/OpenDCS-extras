/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waives copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.support.Alarms;

import decodes.tsdb.CTimeSeries;
import java.util.Date;



/**
 *
 * @author L2EDDMAN
 */
public class AlarmCondition {
    
    
    private int check;
    private String check_str;
    private double a;
    private double b;
    private int duration;
    
    public AlarmCondition( String condition ){
        // give a string like >5:5HOURS
        check_str = condition;
    }
    
    
    public boolean check( CTimeSeries ts, Date current_date ){
        switch( check ){
//            case CHECK_MISSING:{
                
//            }
            
            default: return false;
        }
        
    }
    
    public String get_check(){
        return this.check_str;
    }
    
    
    public static int CHECK_MISSING = 0;
    public static int CHECK_GREATER = 1;
    public static int CHECK_LESS = 2;
    public static int CHECK_EQUALS = 3;
    public static int CHECK_ROC = 4;
    
}
