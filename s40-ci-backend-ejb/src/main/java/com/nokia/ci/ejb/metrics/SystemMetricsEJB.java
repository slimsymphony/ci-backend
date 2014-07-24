/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.metrics;

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Singleton
public class SystemMetricsEJB {

    private static Logger log = LoggerFactory.getLogger(SystemMetricsEJB.class);
    private OperatingSystemMXBean os;
    private MemoryMXBean mbean;
    private RuntimeMXBean rt;
    private ThreadMXBean tbean;

    @PostConstruct
    public void init() {
        os = ManagementFactory.getOperatingSystemMXBean();
        mbean = ManagementFactory.getMemoryMXBean();
        rt = ManagementFactory.getRuntimeMXBean();
        tbean = ManagementFactory.getThreadMXBean();
    }

    public String getOSArchitecture() {
        return os.getArch();
    }

    public String getOSName() {
        return os.getName();
    }

    public String getOSVersion() {
        return os.getVersion();
    }

    public int getAvailableProcessors() {
        return os.getAvailableProcessors();
    }

    public long getUptime() {
        return rt.getUptime();
    }

    public double getSystemLoad() {
        double systemLoad = os.getSystemLoadAverage();
        systemLoad = (systemLoad / getAvailableProcessors());
        return systemLoad;
    }

    public double getUsedHeapBytesAsPercent() {
        return (double)mbean.getHeapMemoryUsage().getUsed()/mbean.getHeapMemoryUsage().getMax()*100;
    }

    public long getUsedHeapBytes() {
        return mbean.getHeapMemoryUsage().getUsed();
    }

    public long getUsedNonHeapBytes() {
        return mbean.getNonHeapMemoryUsage().getUsed();
    }
    
    public long getUsedHeapMegaBytes() {
        return (long)(getUsedHeapBytes() / 1048576);
    }
    
    public long getUsedNonHeapMegaBytes() {
        return (long)(getUsedNonHeapBytes() / 1048576);
    }
    
    public long getLiveThreadCount() {
        return tbean.getThreadCount();
    }
    
    public long getDaemonThreadCount() {
        return tbean.getDaemonThreadCount();
    }
}
