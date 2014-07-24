package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.map.LinkedCacheMap;
import com.nokia.ci.ejb.metrics.SystemMetricsEJB;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

/**
 *
 * @author hhellgre
 */
@Named
@ViewScoped
public class SystemMetricsBean extends AbstractUIBaseBean {

    private CartesianChartModel memoryModel;
    private ChartSeries heapUsage;
    private ChartSeries nonHeapUsage;
    private CartesianChartModel systemLoadModel;
    private ChartSeries systemLoad;
    private CartesianChartModel threadModel;
    private ChartSeries threadUsage;
    private ChartSeries daemonThreadUsage;
    private String OSArchitecture;
    private int availableProcessors;
    private String OSName;
    private String OSVersion;
    private String uptime;
    @Inject
    private SystemMetricsEJB systemEJB;

    @Override
    protected void init() {
        initOSInfo();
        initMemoryModel();
        initSystemLoadModel();
        initThreadModel();
        updateUptime();
    }

    private void initOSInfo() {
        availableProcessors = systemEJB.getAvailableProcessors();
        OSName = systemEJB.getOSName();
        OSVersion = systemEJB.getOSVersion();
        OSArchitecture = systemEJB.getOSArchitecture();
    }

    private void updateUptime() {
        long up = systemEJB.getUptime();
        uptime = DurationFormatUtils.formatDurationWords(up, true, true);
    }

    private void initSystemLoadModel() {
        systemLoadModel = new CartesianChartModel();
        systemLoad = new ChartSeries();
        systemLoad.setLabel("System load");
        systemLoad.setData(new LinkedCacheMap<Object, Number>(40));
        systemLoadModel.addSeries(systemLoad);
        updateSystemLoadModel();
    }
    
    private void updateSystemLoadModel() {
        if(systemLoadModel == null || systemLoad == null) {
            initSystemLoadModel();
            return;
        }
        
        double cpu = systemEJB.getSystemLoad();
        systemLoad.getData().put(getTimeStr(), cpu);
    }
    
    private void initThreadModel() {
        threadModel = new CartesianChartModel();
        threadUsage = new ChartSeries();
        threadUsage.setLabel("Thread count");
        threadUsage.setData(new LinkedCacheMap<Object, Number>(40));
        daemonThreadUsage = new ChartSeries();
        daemonThreadUsage.setLabel("Daemon thread count");
        daemonThreadUsage.setData(new LinkedCacheMap<Object, Number>(40));
        threadModel.addSeries(threadUsage);
        threadModel.addSeries(daemonThreadUsage);
        updateThreadModel();
    }

    private void updateThreadModel() {
        if(threadModel == null || threadUsage == null) {
            initThreadModel();
            return;
        }
        
        long threads = systemEJB.getLiveThreadCount();
        threadUsage.getData().put(getTimeStr(), threads);
        
        threads = systemEJB.getDaemonThreadCount();
        daemonThreadUsage.getData().put(getTimeStr(), threads);
    }
    
    private void initMemoryModel() {
        memoryModel = new CartesianChartModel();
        heapUsage = new ChartSeries();
        heapUsage.setLabel("Heap (MB)");
        heapUsage.setData(new LinkedCacheMap<Object, Number>(40));
        nonHeapUsage = new ChartSeries();
        nonHeapUsage.setLabel("Non heap (MB)");
        nonHeapUsage.setData(new LinkedCacheMap<Object, Number>(40));
        memoryModel.addSeries(heapUsage);
        memoryModel.addSeries(nonHeapUsage);
        updateMemoryModel();
    }

    private void updateMemoryModel() {
        if(memoryModel == null || heapUsage == null || nonHeapUsage == null) {
            initMemoryModel();
            return;
        }
        
        long heap = systemEJB.getUsedHeapMegaBytes();
        long nonHeap = systemEJB.getUsedNonHeapMegaBytes();

        heapUsage.getData().put(getTimeStr(), heap);
        nonHeapUsage.getData().put(getTimeStr(), nonHeap);
    }
    
    private String getTimeStr() {
        Date d = new Date();
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(d);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        String time = (minute < 10) ? "0" + minute : "" + minute;
        time += ":";
        time += (second < 10) ? "0" + second : "" + second;
        return time;
    }

    public void updateData() {
        updateUptime();
        updateMemoryModel();
        updateSystemLoadModel();
        updateThreadModel();
    }

    public CartesianChartModel getMemoryModel() {
        return memoryModel;
    }

    public void setMemoryModel(CartesianChartModel memoryModel) {
        this.memoryModel = memoryModel;
    }

    public CartesianChartModel getThreadModel() {
        return threadModel;
    }

    public void setThreadModel(CartesianChartModel threadModel) {
        this.threadModel = threadModel;
    }

    public CartesianChartModel getSystemLoadModel() {
        return systemLoadModel;
    }

    public void setSystemLoadModel(CartesianChartModel systemLoadModel) {
        this.systemLoadModel = systemLoadModel;
    }
    
    public String getOSArchitecture() {
        return OSArchitecture;
    }

    public void setOSArchitecture(String OSArchitecture) {
        this.OSArchitecture = OSArchitecture;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    public String getOSName() {
        return OSName;
    }

    public void setOSName(String OSName) {
        this.OSName = OSName;
    }

    public String getOSVersion() {
        return OSVersion;
    }

    public void setOSVersion(String OSVersion) {
        this.OSVersion = OSVersion;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }
}
