/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
public abstract class MetricsEJB implements Serializable {

    /**
     * Logger.
     */
    private static org.slf4j.Logger log = LoggerFactory.getLogger(MetricsEJB.class);

    protected Calendar getCalendar(TimeZone timeZone) {
        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone("GMT");
        }
        Calendar cal = Calendar.getInstance(timeZone);
        cal.clear();
        return cal;
    }

    protected Calendar getAbsoluteBeginningOfDay(Date date, TimeZone timeZone) {
        Calendar cal = getCalendar(timeZone);
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal;
    }

    protected Calendar getAbsoluteEndOfDay(Date date, TimeZone timeZone) {
        Calendar cal = getCalendar(timeZone);
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // go forward one day...
        cal.add(Calendar.DAY_OF_YEAR, 1);
        // ...and step one second back. Now timestamp is in (almost)absolute end of required day.
        cal.add(Calendar.SECOND, -1);
        return cal;
    }
}
