package com.nokia.ci.ejb.incident;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.metrics.SystemMetricsEJB;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.SysConfigKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 4/19/13 Time: 9:57 AM To change
 * this template use File | Settings | File Templates.
 */
@Startup
@Singleton
public class SystemUsageIncidentHandlerEJB extends BaseIncidentHandler {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    TimerService timerService;
    protected Timer timer;
    @EJB
    private SystemMetricsEJB systemMetricsEJB;
    @EJB
    private SysConfigEJB sysConfigEJB;
    private final int USAGE_SAMPLING_TIMEOUT = 60;
    private final int SYSTEM_LOAD_STACK_SAMPLING_SIZE = 15;
    private final double SYSTEM_LOAD_THRESHOLD = 0.7;
    private final String SYSTEM_LOAD_INCIDENT_DESCRIPTION = "Averaged system load on cluster node '%s' was '%s' in a past %d seconds";
    private final int MEM_USAGE_STACK_SAMPLING_SIZE = 5;
    private final int MEM_USAGE_THRESHOLD_IN_PERCENT = 90;
    private final String MEM_USAGE_HIGH_INCIDENT_DESCRIPTION = "Memory averaged usage on cluster node '%s' was '%s' in a past %d seconds";
    private UsageMeasurement systemUsageStat = new UsageMeasurement();
    private UsageMeasurement memUsageStat = new UsageMeasurement();
    private String host = "";

    @PostConstruct
    protected void initTimer() {
        log.info("Starting timer.");
        TimerConfig timerConfig = new TimerConfig("System usage Timer", false);
        int usageSamplingTimeout = getUsageSamplingTimeout();
        int timeout = usageSamplingTimeout * 1000;
        log.info("Creating single action timer with timeout {}", timeout);
        timer = timerService.createSingleActionTimer(timeout, timerConfig);

        try {
            //this will not working when two node will be settled on one hardware
            //TODO: try to get cluster node name
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("Cannot detect host name ", e);
        }
    }

    @PreDestroy
    protected void destroy() {
        log.info("Stopping timer.");
        if (timer != null) {
            timer.cancel();
        }
    }

    private int getSystemLoadStackSamplingSize() {
        return sysConfigEJB.getValue(SysConfigKey.SYSTEM_LOAD_STACK_SAMPLING_SIZE, SYSTEM_LOAD_STACK_SAMPLING_SIZE);
    }

    private int getMemUsageStackSamplingSize() {
        return sysConfigEJB.getValue(SysConfigKey.MEM_USAGE_STACK_SAMPLING_SIZE, MEM_USAGE_STACK_SAMPLING_SIZE);
    }

    private double getSystemLoadThresholdAsPercent() {
        return Double.valueOf(sysConfigEJB.getValue(SysConfigKey.SYSTEM_LOAD_THRESHOLD_IN_PERCENT, String.valueOf(SYSTEM_LOAD_THRESHOLD)));
    }

    private int getMemUsageThresholdAsPercent() {
        return sysConfigEJB.getValue(SysConfigKey.MEM_USAGE_THRESHOLD_IN_PERCENT, MEM_USAGE_THRESHOLD_IN_PERCENT);
    }

    private int getUsageSamplingTimeout() {
        return sysConfigEJB.getValue(SysConfigKey.USAGE_SAMPLING_TIMEOUT, USAGE_SAMPLING_TIMEOUT);
    }

    @Timeout
    protected void task() {
        log.info("**** System usage task triggered ****");
        systemUsageStat.setMaxSize(getSystemLoadStackSamplingSize());
        systemUsageStat.setThreshold(getSystemLoadThresholdAsPercent());
        memUsageStat.setMaxSize(getMemUsageStackSamplingSize());
        memUsageStat.setThreshold(getMemUsageThresholdAsPercent());

        long startTime = System.currentTimeMillis();
        try {
            systemUsageStat.addUsage(systemMetricsEJB.getSystemLoad());
            log.info("System load measurement {}", systemUsageStat.toString());
            if (systemUsageStat.isAvgUsageExceeded()) {
                String description = String.format(SYSTEM_LOAD_INCIDENT_DESCRIPTION, host, systemUsageStat.getAverageUsage(), getUsageSamplingTimeout() * getSystemLoadStackSamplingSize());
                log.info("System load measurement - creating incident desc: {}", description);
                createIncident(IncidentType.SYSTEM, description);
                systemUsageStat.clear();
            }

            memUsageStat.addUsage(systemMetricsEJB.getUsedHeapBytesAsPercent());
            log.info("Memory measurement {}", memUsageStat.toString());
            if (memUsageStat.isUsagePermanentlyExceeded()) {
                String description = String.format(MEM_USAGE_HIGH_INCIDENT_DESCRIPTION, host, memUsageStat.getAverageUsage(), getUsageSamplingTimeout() * getMemUsageStackSamplingSize());
                log.info("Mem measurement - creating incident desc: {}", description);
                createIncident(IncidentType.SYSTEM, description);
                memUsageStat.clear();
            }
        } catch (Exception ex) {
            log.error("Error in measuring cpu and mem usage!", ex);
        } finally {
            initTimer();
        }
        log.info("**** System usage task done in {}ms ****", System.currentTimeMillis() - startTime);
    }

    public void fire() {
        if (timer != null) {
            timer.cancel();
        }
        task();
    }

    class UsageMeasurement {

        FixedSizeStack stack;
        private double threshold;
        private int maxSize;

        public void setMaxSize(int maxSize) {
            if (this.maxSize != maxSize) {
                stack = new FixedSizeStack(maxSize);
                this.maxSize = maxSize;
            }
        }

        public void setThreshold(double threshold) {
            this.threshold = threshold;
        }

        public void addUsage(double usage) {
            stack.offer(usage);
        }

        public boolean isAvgUsageExceeded() {
            log.info("stack.size(): {}, maxSize : {}", stack.size(), maxSize);
            if (stack.size() < maxSize) {
                return false;
            }
            log.info("calc avg: {}, threshold : {}", getAverageUsage(), threshold);
            return getAverageUsage() > threshold;
        }

        public boolean isUsagePermanentlyExceeded() {
            log.info("stack.size(): {}, maxSize : {}", stack.size(), maxSize);
            if (stack.size() < maxSize) {
                return false;
            }
            boolean usagePermanentlyExceeded = true;
            for (Double usage : stack) {
                if (usage <= threshold) {
                    usagePermanentlyExceeded = false;
                }
            }
            log.info("isUsagePermanentlyExceeded returning {}", usagePermanentlyExceeded);
            return usagePermanentlyExceeded;
        }

        public void clear() {
            stack = new FixedSizeStack(maxSize);
        }

        public double getAverageUsage() {
            double totalUsage = 0;
            for (Double usage : stack) {
                totalUsage = totalUsage + usage;
            }
            return totalUsage / stack.size();
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            ret.append("UsageMeasurement with stack size ").append(maxSize).append(" values : ");
            for (Double usage : stack) {
                ret.append(usage);
                ret.append("; ");
            }
            return ret.toString();
        }
    }

    class FixedSizeStack extends LinkedList<Double> {

        private int size;

        FixedSizeStack(int size) {
            this.size = size;
        }

        @Override
        public boolean offer(Double item) {
            boolean ret = super.offer(item);
            while (size() > size) {
                poll();
            }
            return ret;
        }
    }
}
