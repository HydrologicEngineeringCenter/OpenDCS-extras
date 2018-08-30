/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spk.algo.support;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.util.TimeZone;
import static spk.algo.support.Logging.*;
/**
 * SPK Date/Time Helper functions
 * @author L2EDDMAN
 */
public class DateTime {

    private static int days_in_month[] = { 31, 28, 31,30,31,30,31,31,30,31,30,31 };
    
    
    public static int to_wy_julian( Date t ){
        return to_wy_julian(t, TimeZone.getDefault());
    }
    
    /**
     * This will return the correct Julian date even during a leap year.
     * starts at 1, for the Water Control Diagrams you should subtract 1 when
     * calling the interpolation routines
     * @param t  input date to return
     * @return   water year Julian day
     */
    public static int to_wy_julian( Date t, TimeZone tz)
    {
        GregorianCalendar cal = new GregorianCalendar(tz);
        cal.setTime(t);
        
        int cur_day = cal.get(cal.DAY_OF_MONTH);
        int julian = cur_day;
        int month = cal.get(cal.MONTH);
        int year = cal.get(cal.YEAR );
        boolean is_leap = cal.isLeapYear(year);
        debug3( "to_wy_julian:");
        debug3( "  cur_day    = " + julian);
        debug3( "  month      = " + month);
        debug3( "  year       = " + year);
        debug3( "  is leap    = " + is_leap );
        // now we brute force the wateryear day
        if( month == Calendar.OCTOBER )
        {
        }
        else if( month == Calendar.NOVEMBER )
        {
            julian += days_in_month[Calendar.OCTOBER];
        }
        else if( month == Calendar.DECEMBER)
        {
            for( int i = Calendar.OCTOBER; i <= Calendar.NOVEMBER; i++)
            {
                julian += days_in_month[i];
            }
        }
        else if( month < Calendar.OCTOBER)
        {
            julian += days_in_month[Calendar.OCTOBER] + days_in_month[Calendar.NOVEMBER] +days_in_month[Calendar.DECEMBER];
            for( int i = 0; i < month; i++)
            {
                julian += days_in_month[i];
            }
            if( is_leap && month > Calendar.FEBRUARY )
            {
                julian += 1; // for the 29th day in February for the leap year
            }
        }

        /*
        //cal.setGregorianChange( new Date(Long.MAX_VALUE) );
        int julian = (cal.get( Calendar.DAY_OF_YEAR) + 92 ) % 365;
        boolean leapyr = cal.isLeapYear(cal.get( Calendar.YEAR));
        //boolean prev_yr = cal.isLeapYear( cal.get( Calendar.YEAR) - 1 );
        if( julian == 0 && !leapyr)
        {
            return 365; // september 30th in a non leap year
        }
        else if( julian == 1 && leapyr)
        {
            return 366; // septermber 30th in a leap year
        }
        else if( leapyr && cal.get( cal.MONTH) >= cal.OCTOBER)
        {
            return julian - 1; //
        }        
        */
        debug3( "  julian day = " + julian);
        return julian;
    }
}
