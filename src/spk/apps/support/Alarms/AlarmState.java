/*
 *  As a work of the United States Government, this package 
 *  is in the public domain within the United States. Additionally, 
 *  We waive copyright and related rights in the work worldwide
 *  through the CC0 1.0 Universal Public Domain Dedication 
 *  (which can be found at https://creativecommons.org/publicdomain/zero/1.0/).

 */
package spk.apps.support.Alarms;

import java.util.Date;

/**
 *
 * @author Michael Neilson <michael.a.neilson@usace.army.mil>
 */
public class AlarmState {
    public Threshold current;
    public double    current_value;
    public Date      current_time;
    public Threshold previous;
    public double    previous_value;
    public Date      previous_time;
    public Date      email_sent;
}
