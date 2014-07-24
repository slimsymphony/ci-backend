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
 * Bean class for system subverification trend metrics.
 *
 * @author larryang
 */
@Named
public class SysMetricsSubveriTrendBean extends MetricsLineChartBeanBase {

    private Logger log = LoggerFactory.getLogger(SysMetricsSubveriTrendBean.class);
    @Inject
    private JobMetricsEJB jobMetricsEJB;
    private List<String> selectedProjects;
    private TreeMap<String, String> projects;    
    private List<String> selectedVeriTypes;
    private TreeMap<String, String> veriTypes;
    
    private boolean showSum = false;
    
    @Override
    public void init() {
        
        super.init();
        
        setMetricsLevel(MetricsLevel.SYSTEM);
        setJsfComponent("sysSubveriTrendChart.xhtml");
        setHeader("Subverification trend");
        
        veriTypes = new TreeMap<String, String>();
        getVeriTypes().put(BranchType.SINGLE_COMMIT.toString(), BranchType.SINGLE_COMMIT.toString());
        getVeriTypes().put(BranchType.DEVELOPMENT.toString(), BranchType.DEVELOPMENT.toString());
        getVeriTypes().put(BranchType.MASTER.toString(), BranchType.MASTER.toString());
        getVeriTypes().put(BranchType.TOOLBOX.toString(), BranchType.TOOLBOX.toString());
        getVeriTypes().put(BranchType.DRAFT.toString(), BranchType.DRAFT.toString());
        
        getScaleOptions().add(MetricsTimespan.DAILY.toString());
        getScaleOptions().add(MetricsTimespan.WEEKLY.toString());
        getScaleOptions().add(MetricsTimespan.MONTHLY.toString());
        
        setSelectedScale(MetricsTimespan.DAILY.toString());
        
        initProjects();
        
        setRenderDiv("subveriTrendPanel");
        if(isRendered() && getChart() != null) {
            
            String projectsStr = getQueryParam("projects");
            selectedProjects = new ArrayList<String>();
            for (String curProjectStr : projectsStr.split(",")){
                if (!curProjectStr.equals("null")) {
                    selectedProjects.add(curProjectStr);
                }
            }
            
            String veriTypesStr = getQueryParam("veritypes");
            selectedVeriTypes = new ArrayList<String>();
            for (String curTypeStr : veriTypesStr.split(",")){
                if (!curTypeStr.equals("null")) {
                    selectedVeriTypes.add(curTypeStr);
                }
            }

            showSum = getQueryParam("showsum").equals("true");
            
            MetricsTimespan t = getTimespan();
            if(t != null) {
                setSelectedScale(t.toString());
            }
            
            updateDataModel();
        }
    }
    
    @Override
    public void updateDataModel() {
        super.updateDataModel();
        setLabelDivider(0);
        
        if (checkEmptyInputSelection(selectedProjects, "project") || checkEmptyInputSelection(selectedVeriTypes, "verification type")){
            return;
        }
        
        checkMaxInputSelection(selectedProjects, "projects", 10);

        MetricsTimespan selectedTimespan = getMetricsTimespan(getSelectedScale());
        
        ChartSeries subveriSumCountTrend = new ChartSeries();
        subveriSumCountTrend.setLabel("SUM");
        
        int projectLimitCounter = 0;
        for (String projectId : selectedProjects){
                    
            try{
                Project project = getProjectEJB().read(Long.parseLong(projectId));

                ChartSeries subveriCountTrend = new ChartSeries();
                subveriCountTrend.setLabel((project.getProjectGroup() == null ? "" : (project.getProjectGroup().getName() + "-")) + project.getDisplayName());
                
                for (Branch branch : getProjectEJB().getBranches(Long.parseLong(projectId))){
                    
                    Long jobId = null;
                                
                    if (withinSelectedTypes(branch.getType())){
                        List<Job> jobs = getBranchEJB().getJobs(branch.getId());
                        if (jobs.size() > 0){
                            jobId = jobs.get(0).getId();
                        }else{
                            continue;
                        }
                    }

                
                    if (jobId != null){
                        List<MetricsVerificationGroup> metricsVerificationGroupList = getJobMetricsEJB().getVerifications(jobId, getStartDate(), getEndDate(), selectedTimespan, getTimezone(), false);

                        if (getLabelDivider() == 0){
                            calculateXAxisLabelDivider(metricsVerificationGroupList.size());
                        }

                        for (int i = 0; i < metricsVerificationGroupList.size(); i++) {
                            MetricsVerificationGroup metricsVerificationGroup = metricsVerificationGroupList.get(i);

                            MetricsXAxisLabel label = getXAxisLabel(metricsVerificationGroup.getStartTime(), i, selectedTimespan);

                            if (subveriCountTrend.getData().get(label) == null){
                                subveriCountTrend.set(label, metricsVerificationGroup.getItemCount());
                            }else{
                                subveriCountTrend.set(label, subveriCountTrend.getData().get(label).longValue() + metricsVerificationGroup.getItemCount());
                            }

                            if (showSum){
                                if (subveriSumCountTrend.getData().get(label) == null){
                                    subveriSumCountTrend.set(label, metricsVerificationGroup.getItemCount());
                                }else{
                                    subveriSumCountTrend.set(label, subveriSumCountTrend.getData().get(label).longValue() + metricsVerificationGroup.getItemCount());
                                }
                            }

                            if (metricsVerificationGroup.getItems().size() > 0) {
                                getIdList().add(metricsVerificationGroup.getItems().get(0).getId());
                            } else {
                                getIdList().add(null);
                            }
                        }
                    }
                }

                if (projectLimitCounter < 10){
                    //Set it empty so that the chart can be shown normally.
                    if (subveriCountTrend.getData().isEmpty()) {
                        subveriCountTrend.set(0, 0);
                        displayEmptyDatasetMessage(subveriCountTrend.getLabel());
                    }

                    getDataModel().addSeries(subveriCountTrend);
                }

            } catch (NotFoundException ex) {
                log.debug("Exception encountered when updating data module: {}, {}.", ex.getMessage(), ex.getStackTrace());
            }
            
            projectLimitCounter ++;
        }
        
        if (showSum){
            if (subveriSumCountTrend.getData().isEmpty()){
                subveriSumCountTrend.set(0, 0);
            }
            getDataModel().addSeries(subveriSumCountTrend);
        }
    }
    
    private boolean withinSelectedTypes(BranchType type){
        
        for (String curType : this.selectedVeriTypes){
            if (curType.equals(type.toString())){
                return true;
            }
        }
        
        return false;
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
        
        for (int i=0; i<selectedProjects.size(); i++){
            projectsStr += selectedProjects.get(i);
            if (i < selectedProjects.size()-1){
                projectsStr += ",";
            }
        }
        
        if (projectsStr.equals("")){
            projectsStr = "null";
        }
        
        String typesStr = "";
        
        for (int i=0; i < selectedVeriTypes.size(); i++){
            typesStr += selectedVeriTypes.get(i);
            if (i < selectedVeriTypes.size()-1){
                typesStr += ",";
            }
        }
        
        if (typesStr.equals("")){
            typesStr = "null";
        }
        
        permalinkURL += projectsStr + "/" + showSum + "/" + typesStr + "/";
        
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

    public List<String> getSelectedVeriTypes() {
        return selectedVeriTypes;
    }

    public void setSelectedVeriTypes(List<String> selectedVeriTypes) {
        this.selectedVeriTypes = selectedVeriTypes;
    }

    public TreeMap<String, String> getVeriTypes() {
        return veriTypes;
    }

    public void setVeriTypes(TreeMap<String, String> veriTypes) {
        this.veriTypes = veriTypes;
    }

    public boolean isShowSum() {
        return showSum;
    }

    public void setShowSum(boolean showSum) {
        this.showSum = showSum;
    }

    /**
     * @return the jobMetricsEJB
     */
    public JobMetricsEJB getJobMetricsEJB() {
        return jobMetricsEJB;
    }

    /**
     * @param jobMetricsEJB the jobMetricsEJB to set
     */
    public void setJobMetricsEJB(JobMetricsEJB jobMetricsEJB) {
        this.jobMetricsEJB = jobMetricsEJB;
    }
    
    @Override
    protected void open(Long id) {
        openBuild(id);
    }    
}
