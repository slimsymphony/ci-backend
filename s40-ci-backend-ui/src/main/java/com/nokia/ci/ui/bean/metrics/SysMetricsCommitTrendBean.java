package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.JobMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsVerificationGroup;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for system commit trend metrics.
 *
 * @author larryang
 */
@Named
public class SysMetricsCommitTrendBean extends MetricsLineChartBeanBase {

    private Logger log = LoggerFactory.getLogger(SysMetricsCommitTrendBean.class);
    @Inject
    private JobMetricsEJB jobMetricsEJB;
    private List<String> selectedProjects;
    private TreeMap<String, String> projects;
    private boolean showSum = false;

    @Override
    public void init() {
        super.init();
        
        setMetricsLevel(MetricsLevel.SYSTEM);
        setJsfComponent("sysCommitTrendChart.xhtml");
        setHeader("Commit trend");

        selectedProjects = new ArrayList<String>();

        getScaleOptions().add(MetricsTimespan.DAILY.toString());
        getScaleOptions().add(MetricsTimespan.WEEKLY.toString());
        getScaleOptions().add(MetricsTimespan.MONTHLY.toString());

        setSelectedScale(MetricsTimespan.DAILY.toString());

        initProjects();

        setRenderDiv("commitTrendPanel");
        if (isRendered() && getChart() != null) {

            String projectsStr = getQueryParam("projects");

            for (String curProjectStr : projectsStr.split(",")) {
                if (!curProjectStr.equals("null")) {
                    selectedProjects.add(curProjectStr);
                }
            }

            showSum = getQueryParam("showsum").equals("true");

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

        ChartSeries commitSumCountTrend = new ChartSeries();
        commitSumCountTrend.setLabel("SUM");

        MetricsTimespan selectedTimespan = getMetricsTimespan(getSelectedScale());

        int projectLimitCounter = 0;
        for (String projectId : selectedProjects) {
            Long jobId = null;

            try {
                Project project = getProjectEJB().read(Long.parseLong(projectId));

                ChartSeries commitCountTrend = new ChartSeries();
                
                commitCountTrend.setLabel(constructDescriptor(project));

                for (Branch branch : getProjectEJB().getBranches(Long.parseLong(projectId))) {
                    if (branch.getType() == BranchType.SINGLE_COMMIT) {
                        List<Job> jobs = getBranchEJB().getJobs(branch.getId());
                        if (jobs.size() > 0) {
                            jobId = jobs.get(0).getId();
                            break;
                        }
                    }
                }

                if (jobId != null) {
                    List<MetricsVerificationGroup> metricsVerificationGroupList = 
                            jobMetricsEJB.getVerifications(jobId, getStartDate(), 
                            getEndDate(), selectedTimespan, getTimezone());

                    if (getLabelDivider() == 0) {
                        calculateXAxisLabelDivider(metricsVerificationGroupList.size());
                    }

                    for (int i = 0; i < metricsVerificationGroupList.size(); i++) {
                        MetricsVerificationGroup metricsVerificationGroup = metricsVerificationGroupList.get(i);

                        MetricsXAxisLabel label = getXAxisLabel(metricsVerificationGroup.getStartTime(), i, selectedTimespan);

                        commitCountTrend.set(label, metricsVerificationGroup.getItemCount());

                        if (showSum) {
                            if (commitSumCountTrend.getData().get(label) == null) {
                                commitSumCountTrend.set(label, metricsVerificationGroup.getItemCount());
                            } else {
                                commitSumCountTrend.set(label, 
                                        commitSumCountTrend.getData().get(label).longValue() + metricsVerificationGroup.getItemCount());
                            }
                        }

                        if (metricsVerificationGroup.getItems().size() > 0) {
                            getIdList().add(metricsVerificationGroup.getItems().get(0).getId());
                        } else {
                            getIdList().add(null);
                        }
                    }
                }

                if (projectLimitCounter < 10){
                    //Set it empty so that the chart can be shown normally.
                    if (commitCountTrend.getData().isEmpty()) {
                        commitCountTrend.set(0, 0);
                        displayEmptyDatasetMessage(commitCountTrend.getLabel());
                    }

                    getDataModel().addSeries(commitCountTrend);
                }

            } catch (NotFoundException ex) {
                log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
            }
            
            projectLimitCounter ++;
        }

        if (showSum) {
            if (commitSumCountTrend.getData().isEmpty()) {
                commitSumCountTrend.set(0, 0);
            }
            getDataModel().addSeries(commitSumCountTrend);
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

        permalinkURL += projectsStr + "/" + showSum + "/";

        return permalinkURL;
    }

    public List<String> getSelectedProjects() {
        return selectedProjects;
    }

    public void setSelectedProjects(List<String> selectedProjects) {
        this.selectedProjects = selectedProjects;
    }

    public Map<String, String> getProjects() {
        return projects;
    }

    public boolean isShowSum() {
        return showSum;
    }

    public void setShowSum(boolean showSum) {
        this.showSum = showSum;
    }

    public JobMetricsEJB getJobMetricsEJB() {
        return jobMetricsEJB;
    }

    public void setJobMetricsEJB(JobMetricsEJB jobMetricsEJB) {
        this.jobMetricsEJB = jobMetricsEJB;
    }
    
    @Override
    protected void open(Long id) {
        openBuild(id);
    }    
}
