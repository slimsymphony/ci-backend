/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.ChangeTracker;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class ProjectMetricsEJB extends MetricsEJB {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ProjectMetricsEJB.class);
    @EJB
    MetricsQueryEJB metricsQueryEJB;

    /**
     * Get all hangtimes from specified time in one group.
     *
     * @param id
     * @param start
     * @param end
     * @return
     * @throws NotFoundException
     */
    public MetricsHangtimeGroup getHangtimes(Long id, Date start, Date end, TimeZone timeZone,
            MetricsHangtimeType htType) {

        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch individual hangtimes for project {}", id);

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();

        List<ChangeTracker> changeTrackers = metricsQueryEJB.getChangeTrackers(id, searchStart,
                searchEnd, htType);

        MetricsHangtimeGroup mhg = convertToHangtimes(changeTrackers, htType);
        mhg.setStartTime(searchStart);
        mhg.setEndTime(searchEnd);

        log.info("completed fetching individual hangtimes for project {}. Completion time:{}ms",
                id, System.currentTimeMillis() - methodStartTime);

        return mhg;
    }

    public List<MetricsHangtimeGroup> getHangtimes(Long id, Date startTime, Date endTime, MetricsTimespan timespan,
            TimeZone timeZone, MetricsHangtimeType htType) {

        long methodStartTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("Starting to retrieve average hangtimes for project: ").append(id);
        sb.append(". startTime: ").append(startTime);
        sb.append(". endTime: ").append(endTime);
        sb.append(". timespan: ").append(timespan);
        sb.append(". type: ").append(htType);
        log.debug(sb.toString());

        Calendar startCal = getAbsoluteBeginningOfDay(startTime, timeZone);
        Calendar endCal = getAbsoluteEndOfDay(endTime, timeZone);

        // Fetch builds for whole timespan.
        List<ChangeTracker> changeTrackers = metricsQueryEJB.getChangeTrackers(id, startCal.getTime(),
                endCal.getTime(), htType);

        // Convert to metrics hangtimes.
        MetricsHangtimeGroup mhg = convertToHangtimes(changeTrackers, htType);

        // Create groups. One for each day, week or month depending on given timespan.
        List<MetricsHangtimeGroup> groups = MetricsGroup.createGroups(startCal,
                endCal, timespan, MetricsHangtimeGroup.class);

        // place builds into their proper groups.
        int index = 0;
        for (MetricsHangtimeGroup group : groups) {
            while (index < mhg.getItemCount()) {
                MetricsHangtime mh = mhg.getItems().get(index);
                if (mh.getTimestamp().after(group.getEndTime())) {
                    break;
                }
                group.add(mh);
                index++;
            }
        }

        // All done.
        log.info("Completed retrieving average hangtimes for project: {}. Completion time:{}ms",
                id, System.currentTimeMillis() - methodStartTime);
        return groups;
    }

    private MetricsHangtimeGroup convertToHangtimes(List<ChangeTracker> changeTrackers, MetricsHangtimeType htType) {
        MetricsHangtimeGroup mhg = new MetricsHangtimeGroup();

        for (ChangeTracker ct : changeTrackers) {
            MetricsHangtime mh = createMetricsHangtime(ct, htType);
            if (mh != null) {
                mhg.add(mh);
            }
        }

        return mhg;
    }

    private MetricsHangtime createMetricsHangtime(ChangeTracker ct, MetricsHangtimeType htType) {
        Date start = null;
        Date end = null;

        if (htType == MetricsHangtimeType.DEVELOPMENT) {
            start = ct.getScvStart();
            end = ct.getDbvEnd();
        } else if (htType == MetricsHangtimeType.INTEGRATION) {
            start = ct.getDbvEnd();
            end = ct.getReleased();
        } else if (htType == MetricsHangtimeType.DELIVERY_CHAIN) {
            start = ct.getScvStart();
            end = ct.getReleased();
        }

        if (start == null || end == null) {
            return null;
        }

        MetricsHangtime mh = new MetricsHangtime();
        mh.setTimestamp(new Date(start.getTime()));
        mh.setCommitId(ct.getCommitId());
        mh.setHangtime(end.getTime() - start.getTime());

        return mh;
    }
}
