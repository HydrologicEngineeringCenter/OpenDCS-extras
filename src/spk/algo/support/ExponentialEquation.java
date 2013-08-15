/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package spk.algo.support;
/**
 * Used for calculating the snowmelt top con for snowmelt reservoirs
 * the base equation is: ( wyday is the current julian day )
 * P = A - exp( B*(C-wyday) )
 *
 * or towards the end
 *
 * if last_day_to_fill - wyday < min_days_to_fill
 *
 *   P = max_percent - ( (max_percent - min_percent)/min_days_to_fill)*(last_day_to_fill - wyday )
 *
 * 
 * @author L2EDDMAN
 */
public class ExponentialEquation {
    /*
     * Part of the base equation     *
     */
    private double A;
    /*
     * part of the base equation
     */
    private double B;
    /*
     * part of the base equation
     */
    private double C;
    /*
     * Minimum number of days to assume is available before spilling.
     */
    private double min_days_to_fill;
    /*
     * lowest amount of runoff to use
     */
    private double min_percent;
    /*
     * highest amount of runoff to use at the end of the season
     */
    private double max_percent;
    /*
     * Day to assume the reservoir will be filled by
     */
    private double last_day_to_fill;
    /*
     * Minimum number of days to assume before filling at the end of the season when
     * the if condition in the main summary is met.
     *
     * Note: this is used in a supplimental release calculation not being used at this
     * time. it is added for consistency and because it may be used in the
     * future.
     */
    private double min_days_to_assume;

    /**
     * A space seperating string containing all of the variables
     *
     * @param equation
     */
    ExponentialEquation( String equation )
    {
        String parts[] = equation.split("\\s+");
        A = Double.parseDouble(parts[0]);
        B = Double.parseDouble(parts[1]);
        C = Double.parseDouble(parts[2]);
        min_days_to_fill = Double.parseDouble(parts[3]);
        min_percent = Double.parseDouble(parts[4]);
        max_percent = Double.parseDouble(parts[5]);
        last_day_to_fill = Double.parseDouble(parts[6]);
        min_days_to_assume = Double.parseDouble(parts[7]);
    }

    /*
     * Calculated the value for the given wateryear day
     */
    public double calculate( int wyday )
    {
        double percent;
        if( last_day_to_fill - wyday < min_days_to_fill )
        {
            percent = max_percent - ( (max_percent - min_percent)/min_days_to_fill)*(last_day_to_fill - wyday );
        }
        else
        {
            percent = A - Math.exp(B*( C- wyday ) );
            percent = Math.max(percent,min_percent);
        }
        return percent;
    }

}
