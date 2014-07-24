package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.metrics.MetricsHangtimeGroup;
import com.nokia.ci.ejb.metrics.MetricsHangtimeType;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ejb.metrics.ProjectMetricsEJB;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author larryang
 */
@Named
public class SysMetricsHangtimeBean extends MetricsLineChartBeanBase {

    @Inject
    private ProjectMetricsEJB projectMetricsEJB;
    private List<String> selectedProjects;
    private TreeMap<String, String> projects;
    private List<String> hangtimeTypes;
    private String selectedHangtimeType;
    private Logger log = LoggerFactory.getLogger(SysMetricsHangtimeBean.class);

    @Override
    public void init() {

        super.init();
        
        setMetricsLevel(MetricsLevel.SYSTEM);
        setJsfComponent("sysHangtimeChart.xhtml");
        setHeader("Hang time");

        hangtimeTypes = new ArrayList<String>();

        getHangtimeTypes().add(MetricsHangtimeType.DEVELOPMENT.toString());
        getHangtimeTypes().add(MetricsHangtimeType.INTEGRATION.toString());
        getHangtimeTypes().add(MetricsHangtimeType.DELIVERY_CHAIN.toString());

        setSelectedHangtimeType(MetricsHangtimeType.DEVELOPMENT.toString());

        getScaleOptions().add(MetricsTimespan.INDIVIDUAL.toString());
        getScaleOptions().add(MetricsTimespan.DAILY.toString());
        getScaleOptions().add(MetricsTimespan.WEEKLY.toString());
        getScaleOptions().add(MetricsTimespan.MONTHLY.toString());

        setSelectedScale(MetricsTimespan.DAILY.toString());

        initProjects();

        setRenderDiv("hangtimeTrendPanel");
        if (isRendered() && getChart() != null) {

            String projectsStr = getQueryParam("projects");
            selectedProjects = new ArrayList<String>();
            for (String curProjectStr : projectsStr.split(",")) {
                if (!curProjectStr.equals("null")) {
                    selectedProjects.add(curProjectStr);
                }
            }

            selectedHangtimeType = getQueryParam("hangtimetype");

            MetricsTimespan t = getTimespan();
            if (t != null) {
                setSelectedScale(t.toString());
            }

            updateDataModel();
        }
    }

    @Override
    public void updateDataModel() {

        super.updateDataModel();
        setLabelDivider(0);

        if (checkEmptyInputSelection(selectedProjects, "project")){
            return;
        }
        
        checkMaxInputSelection(selectedProjects, "projects", 10);

        MetricsTimespan selectedTimespan = getMetricsTimespan(getSelectedScale());

        int projectLimitCounter = 0;
        for (String projectId : selectedProjects) {
            try {
                Project project = getProjectEJB().read(Long.parseLong(projectId));

                ChartSeries hangtimeTrend = new ChartSeries();
                hangtimeTrend.setLabel(constructDescriptor(project));

                if (this.getSelectedScale().equals(MetricsTimespan.INDIVIDUAL.toString())) {
                    MetricsHangtimeGroup mhg = projectMetricsEJB.getHangtimes(
                            Long.parseLong(projectId), getStartDate(), getEndDate(),
                            getTimezone(), MetricsHangtimeType.valueOf(selectedHangtimeType));

                    if (getLabelDivider() == 0) {
                        calculateXAxisLabelDivider(mhg.getItems().size());
                    }
                    for (int i = 0; i < mhg.getItems().size(); i++) {
                        MetricsXAxisLabel label = getXAxisLabel(mhg.getItems().get(i).getTimestamp(), i, selectedTimespan);
                        hangtimeTrend.set(label, Math.round(msToMin(mhg.getItems().get(i).getHangtime())));
                    }
                } else {
                    List<MetricsHangtimeGroup> mhgs = projectMetricsEJB.getHangtimes(
                            Long.parseLong(projectId), getStartDate(), getEndDate(), 
                            selectedTimespan, getTimezone(), MetricsHangtimeType.valueOf(selectedHangtimeType));
                    
                    if (getLabelDivider() == 0) {
                        calculateXAxisLabelDivider(mhgs.size());
                    }
                    for (int i = 0; i < mhgs.size(); i++) {
                        MetricsXAxisLabel label = getXAxisLabel(mhgs.get(i).getStartTime(), i, selectedTimespan);
                        hangtimeTrend.set(label, Math.round(msToMin(mhgs.get(i).getHangtimeAverage())));
                    }
                }
                
                if (projectLimitCounter < 10){

                    if (hangtimeTrend.getData().isEmpty()) {
                        hangtimeTrend.set(0, 0);
                        displayEmptyDatasetMessage(hangtimeTrend.getLabel());
                    }

                    getDataModel().addSeries(hangtimeTrend);
                }

            } catch (Exception e) {
                log.debug("Exception encountered when fetching hangtime data: {}, {}.", e.getMessage(), e.getStackTrace());
                continue;
            }
            
            projectLimitCounter ++;
        }

    }

    private void initProjects() {
        projects = new TreeMap<String, String>();

        List<Project> assignedProjects = getAssignedProjects();
        for (Project p : assignedProjects) {
            projects.put(constructDescriptor(p), p.getId().toString());
        }
    }

    @Override
    protected String createPermalinkURL() {
        String permalinkURL = super.createPermalinkURL();
        String projectsStr = "";

        for (int i = 0; i < selectedProjects.size(); i++) {
            projectsStr += selectedProjects.get(i);
            if (i < selectedProjects.size() - 1) {
                projectsStr += ",";
            }
        }

        if (projectsStr.equals("")) {
            projectsStr = "null";
        }

        permalinkURL += projectsStr + "/0/0/" + selectedHangtimeType + "/";

        return permalinkURL;
    }

    public List<String> completeHangtimeOptions(String query) {
        return hangtimeTypes;
    }

    public List<String> getHangtimeTypes() {
        return hangtimeTypes;
    }

    public void setHangtimeTypes(List<String> hangtimeTypes) {
        this.hangtimeTypes = hangtimeTypes;
    }

    public TreeMap<String, String> getProjects() {
        return projects;
    }

    public void setProjects(TreeMap<String, String> projects) {
        this.projects = projects;
    }

    public List<String> getSelectedProjects() {
        return selectedProjects;
    }

    public void setSelectedProjects(List<String> selectedProjects) {
        this.selectedProjects = selectedProjects;
    }

    public String getSelectedHangtimeType() {
        return selectedHangtimeType;
    }

    public void setSelectedHangtimeType(String selectedHangtimeType) {
        this.selectedHangtimeType = selectedHangtimeType;
    }

    public ProjectMetricsEJB getProjectMetricsEJB() {
        return projectMetricsEJB;
    }

    public void setProjectMetricsEJB(ProjectMetricsEJB projectMetricsEJB) {
        this.projectMetricsEJB = projectMetricsEJB;
    }
}
