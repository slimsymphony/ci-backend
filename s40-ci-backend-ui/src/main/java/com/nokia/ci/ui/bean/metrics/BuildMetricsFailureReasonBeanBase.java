package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.VerificationEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.BuildMetricsEJB;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ejb.metrics.MetricsBuildFailureCategory;
import com.nokia.ci.ejb.metrics.MetricsBuildFailureCategoryGroup;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationFailureReason;
import com.nokia.ci.ui.model.MetricsXAxisLabel;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.model.chart.ChartSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for build failure reason count metrics.
 *
 * @author larryang
 */
public abstract class BuildMetricsFailureReasonBeanBase extends MetricsLineChartBeanBase {

    private Logger log = LoggerFactory.getLogger(BuildMetricsFailureReasonBeanBase.class);
    @Inject
    private BuildMetricsEJB buildMetricsEJB;
    @Inject
    private BuildEJB buildEJB;
    @Inject
    private VerificationEJB verificationEJB;
    private List<String> selectedReasons;
    private TreeMap<String, String> reasons;
    public static final String KW_NOT_ANALYZED = "Not analyzed";
    
    protected void initProperties(){
    }

    @Override
    public void init() {
        super.init();
        
        getBuildIdFromRequest();
        
        initProperties();

        selectedReasons = new ArrayList<String>();

        getScaleOptions().add(MetricsTimespan.INDIVIDUAL.toString());
        getScaleOptions().add(MetricsTimespan.DAILY.toString());
        getScaleOptions().add(MetricsTimespan.WEEKLY.toString());
        getScaleOptions().add(MetricsTimespan.MONTHLY.toString());

        setSelectedScale(MetricsTimespan.DAILY.toString());

        initReasons();

        
        if (isRendered() && getChart() != null) {

            String reasonsStr = getQueryParam("reasons");

            for (String curReasonStr : reasonsStr.split(",")) {
                if (!curReasonStr.equals("null")) {
                    selectedReasons.add(curReasonStr);
                }
            }

            MetricsTimespan t = getTimespan();
            if (t != null) {
                setSelectedScale(t.toString());
            }

            updateDataModel();
        }
    }
    
    protected void setFailureBuildData(ChartSeries failureReasonTrend, MetricsXAxisLabel label, MetricsBuildFailureCategory failureBuild, String failureReason){

    }
    
    protected void setFailureBuildGroupData(ChartSeries failureReasonTrend, MetricsXAxisLabel label, MetricsBuildFailureCategoryGroup failureBuildGroup, String failureReason){

    }

    @Override
    public void updateDataModel() {
        super.updateDataModel();
        setLabelDivider(0);

        if (checkEmptyInputSelection(selectedReasons, "reasons")){
            return;
        }
        
        checkMaxInputSelection(selectedReasons, "reasons", 10);

        MetricsTimespan selectedTimespan = getMetricsTimespan(getSelectedScale());
        
        try{
            if (selectedTimespan == MetricsTimespan.INDIVIDUAL){

                List<MetricsBuildFailureCategory> failureBuilds = buildMetricsEJB.getFailureCategoryOfBuilds(getBuildId(), getStartDate(), 
                                getEndDate(), getTimezone());

                if (getLabelDivider() == 0) {
                    calculateXAxisLabelDivider(failureBuilds.size());
                }

                int reasonLimitCounter = 0;
                for (String failureReason : selectedReasons){

                    if (reasonLimitCounter < 10){
                        ChartSeries failureReasonTrend = new ChartSeries();
                        failureReasonTrend.setLabel(failureReason);

                        for (int i = 0; i < failureBuilds.size(); i++){
                            MetricsBuildFailureCategory failureBuild = failureBuilds.get(i);
                            MetricsXAxisLabel label = getXAxisLabel(failureBuild.getStartTime(), i, selectedTimespan);
                            setFailureBuildData(failureReasonTrend, label, failureBuild, failureReason);
                        }

                        if (failureReasonTrend.getData().isEmpty()) {
                            failureReasonTrend.set(0, 0);
                        }

                        getDataModel().addSeries(failureReasonTrend);
                    }  
                    reasonLimitCounter++;
                }
            }else{
                List<MetricsBuildFailureCategoryGroup> failureBuildGroups = buildMetricsEJB.getFailureCategoryGroups(getBuildId(), getStartDate(), 
                                getEndDate(), selectedTimespan, getTimezone());

                if (getLabelDivider() == 0) {
                    calculateXAxisLabelDivider(failureBuildGroups.size());
                }

                int reasonLimitCounter = 0;
                for (String failureReason : selectedReasons){

                    if (reasonLimitCounter < 10){
                        ChartSeries failureReasonTrend = new ChartSeries();
                        failureReasonTrend.setLabel(failureReason);

                        for (int i = 0; i < failureBuildGroups.size(); i++){
                            MetricsBuildFailureCategoryGroup failureBuildGroup = failureBuildGroups.get(i);
                            MetricsXAxisLabel label = getXAxisLabel(failureBuildGroup.getStartTime(), i, selectedTimespan);
                            setFailureBuildGroupData(failureReasonTrend, label, failureBuildGroup, failureReason);
                        }

                        if (failureReasonTrend.getData().isEmpty()) {
                            failureReasonTrend.set(0, 0);
                        }

                        getDataModel().addSeries(failureReasonTrend);
                    }

                    reasonLimitCounter++;
                 }
            }
        }catch(Exception e){
            log.error("Update data model exception. {}", e.getMessage() + e.getStackTrace());
        }
    }

    private void initReasons() {
        
        reasons = new TreeMap<String,String>();
        
        if (getBuildId() == null){
            return;
        }
        
        Build build = null;
        
        try{
            build = buildEJB.read(getBuildId());
        }catch (NotFoundException e){
            log.error("Build not found {}", getBuildId());
            return;
        }
        
        reasons.put(KW_NOT_ANALYZED, KW_NOT_ANALYZED);
        
        try{
            Verification verification = verificationEJB.getVerificationByUuid(build.getBuildVerificationConf().getVerificationUuid());

            for (VerificationFailureReason failureReason: verificationEJB.getFailureReasons(verification.getId())){
                reasons.put(failureReason.getName(), failureReason.getName());
            }
        }catch(Exception e){
            log.error("Exception when getting verification failure reasons for build id {}. Details: {}.", getBuildId(), e.getMessage() + e.getStackTrace());
        }
        
    }

    @Override
    protected String createPermalinkURL() {
        String permalinkURL = super.createPermalinkURL();
        String reasonsStr = "";

        for (int i = 0; i < selectedReasons.size(); i++) {
            reasonsStr += selectedReasons.get(i);
            if (i < selectedReasons.size() - 1) {
                reasonsStr += ",";
            }
        }

        if (reasonsStr.equals("")) {
            reasonsStr = "null";
        }
        
        permalinkURL += "null/" + reasonsStr + "/";

        return permalinkURL;
    }

    public TreeMap<String, String> getReasons() {
        
        if (reasons == null || reasons.isEmpty()){
            FacesContext context = FacesContext.getCurrentInstance();
            setBuildId(Long.parseLong(UIComponent.getCurrentCompositeComponent(context).getAttributes().get("buildId").toString()));
            initReasons();
        }
                
        return reasons;
    }

    public void setReasons(TreeMap<String, String> reasons) {
        this.reasons = reasons;
    }

    public List<String> getSelectedReasons() {
        return selectedReasons;
    }

    public void setSelectedReasons(List<String> selectedReasons) {
        this.selectedReasons = selectedReasons;
    }

}
