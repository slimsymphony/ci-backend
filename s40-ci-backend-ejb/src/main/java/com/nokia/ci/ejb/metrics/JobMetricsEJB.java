package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.util.Order;
import java.util.ArrayList;
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
 * Business logic implementation job metrics.
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class JobMetricsEJB extends MetricsEJB {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(JobMetricsEJB.class);
    @EJB
    MetricsQueryEJB metricsQueryEJB;

    /**
     * Get all invidual builds from specified time in one group.
     *
     * @param id
     * @param start
     * @param end
     * @return
     * @throws NotFoundException
     */
    public MetricsVerificationGroup getVerifications(Long id, Date start, 
            Date end, TimeZone timeZone, MetricsVerificationResult resultStatus) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch individual builds for job {}", id);                

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();

        List<MetricsVerification> metricsVerifications = metricsQueryEJB.getCompletedVerifications(
                id, searchStart, searchEnd, Order.ASC);
        MetricsVerificationGroup mvg = new MetricsVerificationGroup();

        for (MetricsVerification metricsVerification : metricsVerifications) {
            if (resultStatus == MetricsVerificationResult.ALL || metricsVerification.getResult() == resultStatus){
                mvg.add(metricsVerification);
            }
        }

        log.info("completed fetching individual builds for job {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);

        return mvg;
    }
    
    public MetricsVerificationGroup getVerifications(Long id, Date start, 
            Date end, TimeZone timeZone) throws NotFoundException {
        
        return getVerifications(id, start, end, timeZone, MetricsVerificationResult.ALL);
        
    }


    /**
     * Get all invidual builds from specified time divided to groups by timespan
     *
     * @param id
     * @param startTime
     * @param endTime
     * @param timespan
     * @return
     * @throws NotFoundException
     */
    public List<MetricsVerificationGroup> getVerifications(Long id, Date startTime, 
            Date endTime, MetricsTimespan timespan, TimeZone timeZone) throws NotFoundException {
        
        return getVerifications(id, startTime, endTime, timespan, timeZone, true);
    }
    
    public List<MetricsVerificationGroup> getVerifications(Long id, Date startTime, 
            Date endTime, MetricsTimespan timespan, TimeZone timeZone, boolean noSub) throws NotFoundException {
        
        return getVerifications(id, startTime, endTime, timespan, timeZone, noSub, MetricsVerificationResult.ALL);
    }

    public List<MetricsVerificationGroup> getVerifications(Long id, Date startTime, 
            Date endTime, MetricsTimespan timespan, TimeZone timeZone, boolean noSub, MetricsVerificationResult resultStatus) throws NotFoundException {        

        long methodStartTime = System.currentTimeMillis();        
        StringBuilder sb = new StringBuilder();
        sb.append("Starting to retrieve average runtimes for job: ").append(id);
        sb.append(". startTime: ").append(startTime);
        sb.append(". endTime: ").append(endTime);
        sb.append(". timespan: ").append(timespan);
        log.debug(sb.toString());

        Calendar startCal = getAbsoluteBeginningOfDay(startTime, timeZone);
        Calendar endCal = getAbsoluteEndOfDay(endTime, timeZone);

        // Fetch builds for whole timespan.
        List<MetricsVerification> metricsVerifications;
        
        if (noSub){
            metricsVerifications = metricsQueryEJB.getCompletedVerifications(id, 
                    startCal.getTime(), endCal.getTime(), Order.ASC);
        }else{
            metricsVerifications = metricsQueryEJB.getCompletedSubVerifications(id, 
                    startCal.getTime(), endCal.getTime(), Order.ASC);
        }

        // Create groups. One for each day, week or month depending on given timespan.
        List<MetricsVerificationGroup> groups = MetricsGroup.createGroups(startCal, 
                endCal, timespan, MetricsVerificationGroup.class);

        // place builds into their proper groups.
        int buildIndex = 0;
        for (MetricsVerificationGroup group : groups) {
            while (buildIndex < metricsVerifications.size()) {
                MetricsVerification metricsVerification = metricsVerifications.get(buildIndex);
                if (metricsVerification.getStartTime().after(group.getEndTime())) {
                    break;
                }
                if (resultStatus == MetricsVerificationResult.ALL || metricsVerification.getResult() == resultStatus){
                    group.add(metricsVerification);
                }
                buildIndex++;
            }
        }

        // All done.
        log.info("Completed retrieving average runtimes for job: {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);
        return groups;
    }

    /**
     * Get build breaks. Returns groups that have either passed or failed.
     *
     * e.g.: database search returns 4 builds: first 2 passes and 2 last fails.
     * Then two groups will be returned with valid start and end dates set.
     * first group will contain first 2 passed builds and second group will
     * contain last 2 failed builds.
     *
     * Start time of first group will be set to given startTime. End time of
     * last group will be set to given endTime.
     *
     * @param id
     * @param startTime
     * @param endTime
     * @return
     * @throws NotFoundException
     */
    public List<MetricsVerificationGroup> getVerificationBreaks(Long id, Date startTime, 
            Date endTime) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("Starting to retrieve build breaks for job {}", id);        
        
        // Fetch builds for whole timespan.
        List<MetricsVerification> metricsVerifications = metricsQueryEJB.getCompletedVerifications(
                id, startTime, endTime, Order.ASC);

        // Create list of metrics build groups.
        List<MetricsVerificationGroup> mvgs = new ArrayList<MetricsVerificationGroup>();

        // Create first metrics build group.
        MetricsVerificationGroup mvg = new MetricsVerificationGroup();
        mvg.setStartTime(startTime);
        mvgs.add(mvg);

        // Loop thru builds. Create new group when result changes.
        MetricsVerification previousMetricsVerification = null;
        for (MetricsVerification metricsVerification : metricsVerifications) {
            if (previousMetricsVerification != null) {
                if (metricsVerification.getResult() != previousMetricsVerification.getResult()) {
                    // Set end time one millisecond back from next group.
                    Calendar cal = getCalendar(null);
                    cal.setTime(metricsVerification.getStartTime());
                    cal.add(Calendar.SECOND, -1);
                    mvg.setEndTime(cal.getTime());

                    // Create new group for new result.
                    mvg = new MetricsVerificationGroup();
                    mvg.setStartTime(metricsVerification.getStartTime());
                    mvgs.add(mvg);
                }
            }

            // Add build to current group.
            mvg.add(metricsVerification);

            // Store previous build.
            previousMetricsVerification = metricsVerification;
        }

        // Set the last group to point to requested end date.
        if (!mvgs.isEmpty()) {
            MetricsVerificationGroup lastMetricsVerificationGroup = mvgs.get(mvgs.size() - 1);
            lastMetricsVerificationGroup.setEndTime(endTime);
        }

        // All done.
        log.info("Completed to retrieve build breaks for job {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);
        return mvgs;
    }
}