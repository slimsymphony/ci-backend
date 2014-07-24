package com.nokia.ci.ejb.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.util.Order;
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
 * Business logic implementation load balancer metrics.
 *
 * @author larryang
 */
@Stateless
@LocalBean
public class SlaveMetricsEJB extends MetricsEJB {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(SlaveMetricsEJB.class);
    @EJB
    MetricsQueryEJB metricsQueryEJB;
    
    public MetricsSlaveStatGroup getIndividualSlaveStatPerPool(Long poolId, Date start, Date end, TimeZone timeZone) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch individual slave stat for pool {}", poolId);                

        List<MetricsSlaveStat> metricsSlaveStats = metricsQueryEJB.getSlaveStatsPerPool(poolId, start, end, Order.ASC);
        
        MetricsSlaveStatGroup metricsSlaveStatGroup = getIndividualSlaveStat(metricsSlaveStats);
        
        log.info("completed fetching individual slave stat for pool {}. Completion time:{}ms", 
                poolId, System.currentTimeMillis() - methodStartTime);
        
        return metricsSlaveStatGroup;
    }
    
    public MetricsSlaveStatGroup getIndividualSlaveStatPerLabel(Long poolId, Long labelId, Date start, Date end, TimeZone timeZone) throws NotFoundException {

        long methodStartTime = System.currentTimeMillis();
        if (labelId != null){
            log.debug("starting to fetch individual slave stat for pool {} and label {}", poolId, labelId);                
        }else{
            log.debug("starting to fetch individual slave stat for pool {} and label {}", poolId, "default NULL");
        }

        List<MetricsSlaveStat> metricsSlaveStats = metricsQueryEJB.getSlaveStatsPerLabel(poolId, labelId, start, end, Order.ASC);
        
        MetricsSlaveStatGroup metricsSlaveStatGroup = getIndividualSlaveStat(metricsSlaveStats);
        
        if (labelId != null){
            log.info("completed fetching individual slave stat for label {}. Completion time:{}ms", 
                    labelId, System.currentTimeMillis() - methodStartTime);
        }else{
            log.info("completed fetching individual slave stat for label {}. Completion time:{}ms", 
                    "default NULL", System.currentTimeMillis() - methodStartTime); 
        }
        
        return metricsSlaveStatGroup;       
    }
    
    public MetricsSlaveStatGroup getIndividualSlaveStatPerMachine(Long poolId, Long machineId, Date start, Date end, TimeZone timeZone) throws NotFoundException {

        long methodStartTime = System.currentTimeMillis();
        log.debug("starting to fetch individual slave stat for pool {} and machine", poolId, machineId);                

        List<MetricsSlaveStat> metricsSlaveStats = metricsQueryEJB.getSlaveStatsPerMachine(poolId, machineId, start, end, Order.ASC);
        
        MetricsSlaveStatGroup metricsSlaveStatGroup = getIndividualSlaveStat(metricsSlaveStats);
        
        log.info("completed fetching individual slave stat for machine {}. Completion time:{}ms", 
                machineId, System.currentTimeMillis() - methodStartTime);
        
        return metricsSlaveStatGroup;  
    }
       
    public MetricsSlaveStatGroup getIndividualSlaveStat(List<MetricsSlaveStat> metricsSlaveStats){
        
        MetricsSlaveStatGroup mssg = new MetricsSlaveStatGroup();

        for (MetricsSlaveStat metricsSlaveStat : metricsSlaveStats) {
            mssg.add(metricsSlaveStat);
        }

        return mssg;        
    }
    
    public List<MetricsSlaveStatGroup> getGroupedSlaveStatPerPool(Long poolId, Date start, Date end, MetricsTimespan timespan, TimeZone timeZone) throws NotFoundException {

        long methodStartTime = System.currentTimeMillis();        
        StringBuilder sb = new StringBuilder();
        sb.append("Starting to retrieve average slave stat for pool: ").append(poolId);
        sb.append(". startTime: ").append(start);
        sb.append(". endTime: ").append(end);
        sb.append(". timespan: ").append(timespan);
        log.debug(sb.toString());
        
        Calendar startCal = getAbsoluteBeginningOfDay(start, timeZone);
        Calendar endCal = getAbsoluteEndOfDay(end, timeZone);

        List<MetricsSlaveStat> metricsVerifications = metricsQueryEJB.getSlaveStatsPerPool(poolId, startCal.getTime(), endCal.getTime(), Order.ASC);
        
        List<MetricsSlaveStatGroup> mssgs = getGroupedSlaveStat(metricsVerifications, startCal, endCal, timespan);
        
        log.info("Completed retrieving average slave stat for pool: {}. Completion time:{}ms", 
                poolId, System.currentTimeMillis() - methodStartTime);
        
        return mssgs;
    }
    
    public List<MetricsSlaveStatGroup> getGroupedSlaveStatPerLabel(Long poolId, Long labelId, Date start, Date end, MetricsTimespan timespan, TimeZone timeZone) throws NotFoundException {

        long methodStartTime = System.currentTimeMillis();        
        StringBuilder sb = new StringBuilder();
        sb.append("Starting to retrieve average slave stat for pool: ").append(poolId);
        if (labelId != null){
            sb.append(" and label: ").append(labelId);
        }else{
            sb.append(" and label: ").append("default NULL");
        }
        sb.append(". startTime: ").append(start);
        sb.append(". endTime: ").append(end);
        sb.append(". timespan: ").append(timespan);
        log.debug(sb.toString());
        
        Calendar startCal = getAbsoluteBeginningOfDay(start, timeZone);
        Calendar endCal = getAbsoluteEndOfDay(end, timeZone);

        List<MetricsSlaveStat> metricsVerifications = metricsQueryEJB.getSlaveStatsPerLabel(poolId, labelId, startCal.getTime(), endCal.getTime(), Order.ASC);
        
        List<MetricsSlaveStatGroup> mssgs = getGroupedSlaveStat(metricsVerifications, startCal, endCal, timespan);
        
        log.info("Completed retrieving average slave stat for label: {}. Completion time:{}ms", 
                labelId, System.currentTimeMillis() - methodStartTime);
        
        return mssgs;
    }

    public List<MetricsSlaveStatGroup> getGroupedSlaveStatPerMachine(Long poolId, Long machineId, Date start, Date end, MetricsTimespan timespan, TimeZone timeZone) throws NotFoundException {
        
        long methodStartTime = System.currentTimeMillis();        
        StringBuilder sb = new StringBuilder();
        sb.append("Starting to retrieve average slave stat for pool: ").append(poolId);
        sb.append(" and machine: ").append(machineId);
        sb.append(". startTime: ").append(start);
        sb.append(". endTime: ").append(end);
        sb.append(". timespan: ").append(timespan);
        log.debug(sb.toString());
        
        Calendar startCal = getAbsoluteBeginningOfDay(start, timeZone);
        Calendar endCal = getAbsoluteEndOfDay(end, timeZone);

        List<MetricsSlaveStat> metricsVerifications = metricsQueryEJB.getSlaveStatsPerMachine(poolId, machineId, startCal.getTime(), endCal.getTime(), Order.ASC);
        
        List<MetricsSlaveStatGroup> mssgs = getGroupedSlaveStat(metricsVerifications, startCal, endCal, timespan);
        
        log.info("Completed retrieving average slave stat for machine: {}. Completion time:{}ms", 
                machineId, System.currentTimeMillis() - methodStartTime);
        
        return mssgs;
    }
    
    public List<MetricsSlaveStatGroup> getGroupedSlaveStat(List<MetricsSlaveStat> metricsSlaveStats, Calendar startCal, Calendar endCal, MetricsTimespan timespan){

        // Create groups. One for each day, week or month depending on given timespan.
        List<MetricsSlaveStatGroup> groups = MetricsGroup.createGroups(startCal, 
                endCal, timespan, MetricsSlaveStatGroup.class);

        // place slave stats into their proper groups.
        int slaveStatIndex = 0;
        for (MetricsSlaveStatGroup group : groups) {
            while (slaveStatIndex < metricsSlaveStats.size()) {
                MetricsSlaveStat metricsSlaveStat = metricsSlaveStats.get(slaveStatIndex);
                if (metricsSlaveStat.getProvisionTime().after(group.getEndTime())) {
                    break;
                }
                group.add(metricsSlaveStat);
                slaveStatIndex++;
            }
        }
        
        return groups;
    }

}