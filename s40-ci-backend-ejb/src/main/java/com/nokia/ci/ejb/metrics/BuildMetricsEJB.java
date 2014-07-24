package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
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
 *
 * @author larryang
 */
@Stateless
@LocalBean
public class BuildMetricsEJB extends MetricsEJB {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BuildMetricsEJB.class);
    @EJB
    MetricsQueryEJB metricsQueryEJB;
    
    public List<MetricsBuildMemConsumption> getMemConsumptionOfBuilds(Long id, Date start, 
            Date end, TimeZone timeZone, String componentName) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch individual builds with mem consumptions like build {} for component {}", id, componentName);                

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();

        List<MetricsBuildMemConsumption> metricsBuilds = metricsQueryEJB.getMemConsumptionOfCompletedBuilds(
                id, searchStart, searchEnd, Order.ASC, componentName);

        log.info("completed fetching individual builds with mem consumptions for reference buld {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);

        return metricsBuilds;
    }
    
    public List<MetricsBuildMemConsumption> getMemConsumptionsOfLatestBuild(Long id, Date start, 
            Date end, TimeZone timeZone) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch latest build with mem consumptions like build {} for all components", id);                

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();

        List<MetricsBuildMemConsumption> metricsBuilds = metricsQueryEJB.getAllMemConsumptionsOfLatestBuild(
                id, searchStart, searchEnd);

        log.info("completed fetching individual builds with mem consumptions for reference buld {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);

        return metricsBuilds;
    }
    
    public List<MetricsVerificationGroup> getBuilds(Long id, Date startTime, 
            Date endTime, MetricsTimespan timespan, TimeZone timeZone, MetricsVerificationResult resultStatus) throws NotFoundException {        

        long methodStartTime = System.currentTimeMillis();        
        StringBuilder sb = new StringBuilder();
        sb.append("Starting to retrieve average runtimes for similar builds like build ").append(id);
        sb.append(". startTime: ").append(startTime);
        sb.append(". endTime: ").append(endTime);
        sb.append(". timespan: ").append(timespan);
        log.debug(sb.toString());

        Calendar startCal = getAbsoluteBeginningOfDay(startTime, timeZone);
        Calendar endCal = getAbsoluteEndOfDay(endTime, timeZone);

        // Fetch builds for whole timespan.
        List<MetricsBuild> metricsBuilds = metricsQueryEJB.getCompletedBuilds(id, startTime, endTime, Order.ASC, false);

        // Create groups. One for each day, week or month depending on given timespan.
        List<MetricsVerificationGroup> groups = MetricsGroup.createGroups(startCal, 
                endCal, timespan, MetricsVerificationGroup.class);

        // place builds into their proper groups.
        int buildIndex = 0;
        for (MetricsVerificationGroup group : groups) {
            while (buildIndex < metricsBuilds.size()) {
                MetricsVerification metricsVerification = metricsBuilds.get(buildIndex);
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
        log.info("Completed retrieving average runtimes for similar builds like build {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);
        return groups;
    }
    
    public MetricsVerificationGroup getBuilds(Long id, Date start, 
            Date end, TimeZone timeZone, MetricsVerificationResult resultStatus) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch individual builds for similar builds like build {}", id);                

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();
        
        // Fetch builds for whole timespan.
        List<MetricsBuild> metricsBuilds = metricsQueryEJB.getCompletedBuilds(id, searchStart, searchEnd, Order.ASC, false);
        
        MetricsVerificationGroup mvg = new MetricsVerificationGroup();

        for (MetricsVerification metricsVerification : metricsBuilds) {
            if (resultStatus == MetricsVerificationResult.ALL || metricsVerification.getResult() == resultStatus){
                mvg.add(metricsVerification);
            }
        }

        log.info("completed fetching individual builds for similar builds like build {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);

        return mvg;
    }

    
    public List<MetricsBuildTestCaseStat> getTestCaseStatOfBuilds(Long id, Date start, 
            Date end, TimeZone timeZone, String componentName) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch individual builds with test case stats like build {} for component {}", id, componentName);                

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();

        List<MetricsBuildTestCaseStat> caseStatMetricsBuilds = metricsQueryEJB.getTestCaseStatOfCompletedBuilds(
                id, searchStart, searchEnd, Order.ASC, componentName);

        log.info("completed fetching individual builds with test case stats for reference buld {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);

        return caseStatMetricsBuilds;
    }
    
    public List<MetricsBuildTestCaseStat> getTestCaseStatsOfLatestBuild(Long id, Date start, 
            Date end, TimeZone timeZone) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch latest build with test case stats like build {} for all components", id);                

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();

        List<MetricsBuildTestCaseStat> caseStatMetricsBuilds = metricsQueryEJB.getAllTestCaseStatsOfLatestBuild(
                id, searchStart, searchEnd);

        log.info("completed fetching individual builds with test case stats for reference buld {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);

        return caseStatMetricsBuilds;
    }
    
    public List<MetricsBuildTestCoverage> getTestCoverageOfBuilds(Long id, Date start, 
            Date end, TimeZone timeZone, String componentName) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch individual builds with test coverages like build {} for component {}", id, componentName);                

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();

        List<MetricsBuildTestCoverage> coverageMetricsBuilds = metricsQueryEJB.getTestCoverageOfCompletedBuilds(
                id, searchStart, searchEnd, Order.ASC, componentName);

        log.info("completed fetching individual builds with test coverages for reference buld {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);

        return coverageMetricsBuilds;
    }
    
    public List<MetricsBuildTestCoverage> getTestCoveragesOfLatestBuild(Long id, Date start, 
            Date end, TimeZone timeZone) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch latest build with test coverages like build {} for all components", id);                

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();

        List<MetricsBuildTestCoverage> coverageMetricsBuilds = metricsQueryEJB.getAllTestCoveragesOfLatestBuild(
                id, searchStart, searchEnd);

        log.info("completed fetching individual builds with test coverages for reference buld {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);

        return coverageMetricsBuilds;
    }
    
    public List<MetricsVerificationGroup> getTestTriggeringBuilds(Long id, Date startTime, 
            Date endTime, MetricsTimespan timespan, TimeZone timeZone, MetricsVerificationResult resultStatus) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();        
        StringBuilder sb = new StringBuilder();
        sb.append("Starting to retrieve test triggering builds for similar builds like build ").append(id);
        sb.append(". startTime: ").append(startTime);
        sb.append(". endTime: ").append(endTime);
        sb.append(". timespan: ").append(timespan);
        log.debug(sb.toString());

        Calendar startCal = getAbsoluteBeginningOfDay(startTime, timeZone);
        Calendar endCal = getAbsoluteEndOfDay(endTime, timeZone);

        // Fetch builds for whole timespan.
        List<MetricsBuild> testTriggerMetricsBuilds = metricsQueryEJB.getCompletedBuildsWhichTriggerTest(id, startTime, endTime, Order.ASC);

        // Create groups. One for each day, week or month depending on given timespan.
        List<MetricsVerificationGroup> groups = MetricsGroup.createGroups(startCal, 
                endCal, timespan, MetricsVerificationGroup.class);

        // place builds into their proper groups.
        int buildIndex = 0;
        for (MetricsVerificationGroup group : groups) {
            while (buildIndex < testTriggerMetricsBuilds.size()) {
                MetricsVerification metricsVerification = testTriggerMetricsBuilds.get(buildIndex);
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
        log.info("Completed retrieving average runtimes for similar builds like build {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);
        return groups;
    }
    
    public List<MetricsBuildFailureCategory> getFailureCategoryOfBuilds(Long id, Date start, 
            Date end, TimeZone timeZone) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch individual builds with failure category like build {}", id);                

        Date searchStart = getAbsoluteBeginningOfDay(start, timeZone).getTime();
        Date searchEnd = getAbsoluteEndOfDay(end, timeZone).getTime();

        List<MetricsBuildFailureCategory> failureCategoryMetricsBuilds = metricsQueryEJB.getFailureCategoryOfCompletedBuilds(
                id, searchStart, searchEnd, Order.ASC);

        log.info("completed fetching individual builds with failure category for reference buld {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);

        return failureCategoryMetricsBuilds;
    }
    
    public List<MetricsBuildFailureCategoryGroup> getFailureCategoryGroups(Long id, Date startTime, 
            Date endTime, MetricsTimespan timespan, TimeZone timeZone) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();        
        StringBuilder sb = new StringBuilder();
        sb.append("Starting to retrieve failure category builds for similar builds like build ").append(id);
        sb.append(". startTime: ").append(startTime);
        sb.append(". endTime: ").append(endTime);
        sb.append(". timespan: ").append(timespan);
        log.debug(sb.toString());
        
        Calendar startCal = getAbsoluteBeginningOfDay(startTime, timeZone);
        Calendar endCal = getAbsoluteEndOfDay(endTime, timeZone);
        
        List<MetricsBuildFailureCategory> metricsBuildFailureCategoryBuilds = getFailureCategoryOfBuilds(id, startTime, endTime, timeZone);
        
        List<MetricsBuildFailureCategoryGroup> groups = MetricsGroup.createGroups(startCal, 
                endCal, timespan, MetricsBuildFailureCategoryGroup.class);
        
        List<MetricsBuildFailureCategoryGroup> targetGroups = new ArrayList<MetricsBuildFailureCategoryGroup>();
        
        // place builds into their proper groups.
        int buildIndex = 0;
        for (MetricsBuildFailureCategoryGroup group : groups) {
            while (buildIndex < metricsBuildFailureCategoryBuilds.size()) {
                MetricsBuildFailureCategory metricsBuildFailureCategory = metricsBuildFailureCategoryBuilds.get(buildIndex);
                if (metricsBuildFailureCategory.getStartTime().after(group.getEndTime())) {
                    break;
                }
                group.add(metricsBuildFailureCategory);
                buildIndex++;
            }
            if ((!group.isEmpty())){
                targetGroups.add(group);
            }
        }

        // All done.
        log.info("Completed retrieving failure category builds for similar builds like build {}. Completion time:{}ms", 
                id, System.currentTimeMillis() - methodStartTime);
        return targetGroups;
    }
    
}
