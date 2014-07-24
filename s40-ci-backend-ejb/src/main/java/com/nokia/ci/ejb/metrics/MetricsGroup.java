/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
public abstract class MetricsGroup<T> {
    /**
     * Logger.
     */
    private static org.slf4j.Logger log = LoggerFactory.getLogger(MetricsGroup.class);
    private List<T> items = new ArrayList<T>();
    private Date startTime;
    private Date endTime;

    /**
     * @return the objects
     */
    public List<T> getItems() {
        return items;
    }

    public long getItemCount() {
        return items.size();
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Create groups. One for each day, week or month depending on given
     * timespan.
     *
     * @param startCal
     * @param endCal
     * @param timespan
     * @param type
     * @return
     */
    protected static <K extends MetricsGroup> List<K> createGroups(Calendar startCal, 
            Calendar endCal, MetricsTimespan timespan, Class<K> type) {
        
        List<K> groups = new ArrayList<K>();

        Calendar incrementor = (Calendar) startCal.clone();

        while (incrementor.before(endCal)) {
            // Create group.
            K group;
            try {
                group = type.newInstance();
            } catch (InstantiationException ex) {
                log.error("serious error with creating metrics group {}", ex.getMessage());
                return groups;
            } catch (IllegalAccessException ex) {
                log.error("serious error with creating metrics group {}", ex.getMessage());
                return groups;
            }

            // Set start accordingly.
            group.setStartTime(incrementor.getTime());

            // Increment to next group timespan.
            if (timespan == MetricsTimespan.DAILY) {
                incrementor.add(Calendar.DAY_OF_YEAR, 1);
            } else if (timespan == MetricsTimespan.WEEKLY) {
                incrementor.set(Calendar.DAY_OF_WEEK, incrementor.getFirstDayOfWeek());
                incrementor.add(Calendar.WEEK_OF_YEAR, 1);
            } else if (timespan == MetricsTimespan.MONTHLY) {
                incrementor.set(Calendar.DAY_OF_MONTH, 1);
                incrementor.add(Calendar.MONTH, 1);
            }

            // Determine end for group timespan.
            Calendar endOfTimespan = (Calendar) incrementor.clone();
            endOfTimespan.add(Calendar.SECOND, -1);
            group.setEndTime(endOfTimespan.getTime());

            // Add newly created group to list of groups.
            groups.add(group);
        }

        return groups;
    }            
}
