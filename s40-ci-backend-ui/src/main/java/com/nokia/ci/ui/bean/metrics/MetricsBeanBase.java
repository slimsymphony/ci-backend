package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.metrics.MetricsLevel;
import com.nokia.ci.ejb.metrics.MetricsTimespan;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ui.bean.HttpSessionBean;
import com.nokia.ci.ui.widget.MetricsChartWidget;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.context.RequestContext;
import com.ocpsoft.pretty.PrettyContext;
import javax.faces.application.FacesMessage.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for metrics basic functions.
 *
 * @author larryang
 */
@ViewScoped
public abstract class MetricsBeanBase implements Serializable {

    private Logger log = LoggerFactory.getLogger(MetricsBeanBase.class);
    @Inject
    private JobEJB jobEJB;
    @Inject
    private BuildEJB buildEJB;
    @Inject
    private ProjectEJB projectEJB;
    @Inject
    private BranchEJB branchEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private SysUserEJB sysUserEJB;
    
    private Long verificationId;
    private Long buildId;
    private Date startDate;
    private Date endDate;
    private List<Long> idList;
    private String renderDiv = "chart";
    private String chart = null;
    private MetricsTimespan timespan = null;
    private MetricsLevel metricsLevel = MetricsLevel.VERIFICATION;
    private String jsfComponent;
    private String header = "";

    @PostConstruct
    public void init() {
        //Set common initial values.
        chart = getQueryParam("chart");
        if (chart != null) {

            String scale = getQueryParam("scale");
            if (scale != null && !scale.equalsIgnoreCase("null")) {
                timespan = MetricsTimespan.valueOf(scale);
            }

            String start = getQueryParam("startDate");
            String end = getQueryParam("endDate");

            DateFormat df = new SimpleDateFormat("M-d-y");
            try {
                endDate = df.parse(end);
                startDate = df.parse(start);
            } catch (ParseException ex) {
                log.warn("Could not parse end and start date from URL");
                endDate = new Date();
                startDate = goBackInTime(endDate, 15, getTimezone());
            }
        } else {
            endDate = new Date();
            startDate = goBackInTime(endDate, 15, getTimezone());
        }

        idList = new ArrayList<Long>();
    }

    protected String getQueryParam(String key) {
        FacesContext fc = FacesContext.getCurrentInstance();
        return fc.getExternalContext().getRequestParameterMap().get(key);
    }

    public void updateDataModel() {
        getVerificationIdFromRequest();
        getBuildIdFromRequest();
        idList.clear();
    }

    protected void openBuild(Long id) {
        if (id != null) {
            RequestContext.getCurrentInstance().execute("window.open('/page/build/" + id + "')");
        }
    }

    protected float msToMin(long ms) {
        float mstmp = ms;
        return mstmp / 1000F / 60F;
    }

    protected Long getVerificationIdFromRequest() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String verificationIdString = fc.getExternalContext().getRequestParameterMap().get("verificationId");
        if (verificationIdString != null) {
            verificationId = Long.parseLong(verificationIdString);
        }
        return verificationId;
    }
    
    protected Long getBuildIdFromRequest() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String buildIdString = fc.getExternalContext().getRequestParameterMap().get("buildId");
        if (buildIdString != null) {
            buildId = Long.parseLong(buildIdString);
        }
        return buildId;
    }

    protected void displayEmptyDatasetMessage() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Info", "No data obtained within selected timespan, please consider changing query criterias."));
    }

    protected void displayEmptyDatasetMessage(String axisName) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Info", "No data obtained for axis " + axisName + " within selected timespan, please consider changing query criterias."));
    }

    protected void displayGeneralMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Info", msg));
    }
    
    protected boolean checkEmptyInputSelection(List selection, String dataLabel){
        
        if (selection.isEmpty()){
            displayGeneralMessage("No " + dataLabel + " is selected from selection list.");
            return true;
        }else{
            return false;
        }
    }
    
    protected void checkMaxInputSelection(List selection, String dataLabel, int maxLimit){
        if (selection.size() > maxLimit){
            displayGeneralMessage("Too many " + dataLabel + " are selected from selction list, should no more than " + maxLimit + " selected.");                
        }        
    }

    protected TimeZone getTimezone() {
        if (httpSessionBean != null && httpSessionBean.getSysUser() != null
                && httpSessionBean.getSysUser().getTimezone() != null) {
            return TimeZone.getTimeZone(httpSessionBean.getSysUser().getTimezone());
        }

        return TimeZone.getTimeZone("GMT");
    }

    protected MetricsTimespan getMetricsTimespan(String timeSpan) {
        MetricsTimespan selectedTimespan = null;

        if (timeSpan.equalsIgnoreCase(MetricsTimespan.INDIVIDUAL.toString())) {
            selectedTimespan = MetricsTimespan.INDIVIDUAL;
        } else if (timeSpan.equalsIgnoreCase(MetricsTimespan.DAILY.toString())) {
            selectedTimespan = MetricsTimespan.DAILY;
        } else if (timeSpan.equalsIgnoreCase(MetricsTimespan.WEEKLY.toString())) {
            selectedTimespan = MetricsTimespan.WEEKLY;
        } else if (timeSpan.equalsIgnoreCase(MetricsTimespan.MONTHLY.toString())) {
            selectedTimespan = MetricsTimespan.MONTHLY;
        }

        return selectedTimespan;
    }

    private Date goBackInTime(Date d, int days, TimeZone timezone) {
        Calendar now = Calendar.getInstance(timezone);
        now.setTime(d);
        now.add(Calendar.DAY_OF_YEAR, -days);
        return now.getTime();
    }

    public void openPermalink() {
        String command = createPermalinkURL();
        RequestContext.getCurrentInstance().execute("window.open('" + command + "')");
    }

    protected String createPermalinkURL() {
        String ret = "";
        String uri = PrettyContext.getCurrentInstance().getRequestURL().toURL();
        if (!uri.endsWith("/")) {
            uri += "/";
        }
        
        //Tweak to make permalink generation works for pin-to-toolbox UI feature.
        if (uri.contains("/my/toolbox/")){
            
            String repStr = "";
            
            if (getMetricsLevel() == MetricsLevel.BUILD){
                repStr = "/" + getMetricsLevel().toString() + "/metrics/" + getBuildId() + "/";
            }else if (getMetricsLevel() == MetricsLevel.SYSTEM){
                repStr = "/metrics/";
            }else{ //for MetricsLevel.VERIFICATION
                repStr = "/" + getMetricsLevel().toString() + "/metrics/" + getVerificationId() + "/";
            }
            uri = uri.replace("/my/toolbox/", repStr);
        }
                    
        ret = uri;

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        String start = (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);

        cal.setTime(endDate);
        String end = (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.DATE) + "-" + cal.get(Calendar.YEAR);

        ret += renderDiv + "/";
        ret += start + "/";
        ret += end + "/";

        return ret;
    }

    public HttpSessionBean getHttpSessionBean() {
        return httpSessionBean;
    }

    public void setHttpSessionBean(HttpSessionBean httpSessionBean) {
        this.httpSessionBean = httpSessionBean;
    }

    public Long getVerificationId() {
        if (verificationId == null){
            getVerificationIdFromRequest();
        }
        return verificationId;
    }

    public void setVerificationId(Long verificationId) {
        this.verificationId = verificationId;
    }

    public Long getBuildId() {
        if (buildId == null){
            getBuildIdFromRequest();
        }
        return buildId;
    }

    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> buildIdList) {
        this.idList = buildIdList;
    }

    public String getRenderDiv() {
        return renderDiv;
    }

    public void setRenderDiv(String renderDiv) {
        this.renderDiv = renderDiv;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public MetricsTimespan getTimespan() {
        return timespan;
    }

    public void setTimespan(MetricsTimespan timespan) {
        this.timespan = timespan;
    }

    public Boolean isRendered() {
        if (chart == null) {
            return true;
        } else if (chart != null && renderDiv.equals(chart)) {
            return true;
        }

        return false;
    }

    protected String constructDescriptor(Project project) {
        StringBuilder sb = new StringBuilder();
        if (project.getProjectGroup() != null) {
            sb.append(project.getProjectGroup().getName());
            sb.append("-");
        }
        sb.append(project.getDisplayName());

        return sb.toString();
    }

    /**
     * @return the projectEJB
     */
    public ProjectEJB getProjectEJB() {
        return projectEJB;
    }

    /**
     * @param projectEJB the projectEJB to set
     */
    public void setProjectEJB(ProjectEJB projectEJB) {
        this.projectEJB = projectEJB;
    }

    /**
     * @return the branchEJB
     */
    public BranchEJB getBranchEJB() {
        return branchEJB;
    }

    /**
     * @param branchEJB the branchEJB to set
     */
    public void setBranchEJB(BranchEJB branchEJB) {
        this.branchEJB = branchEJB;
    }

    public List<Project> getAssignedProjects() {
        List<Project> allProjects = projectEJB.readAll();
        List<Project> allowedProjects = new ArrayList<Project>();
        for (Project p : allProjects) {
            if (!httpSessionBean.hasAccessToProject(p.getId())) {
                continue;
            }
            allowedProjects.add(p);
        }
        return allowedProjects;
    }
    
    /**
     * Handle selection for item.
     * 
     * Should be overwritten in implementing class, but is not mandatory.
     * 
     * @param id 
     */
    void open(Long id) {        
    }

    public void pinToMyToolbox() {
        try {
            Long id = -1l;

            if (getMetricsLevel() == MetricsLevel.VERIFICATION){
                id = getVerificationId();
            }else if (getMetricsLevel() == MetricsLevel.BUILD){
                id = getBuildId();
            }

            MetricsChartWidget wgt = new MetricsChartWidget(id, this, getPinChartHeader());
            Widget metricsWidget = wgt.getPersistentWidget();
            sysUserEJB.addWidget(httpSessionBean.getSysUserId(), metricsWidget);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Chart pinned!", getRenderDiv() + " metrics chart is now pinned to My toolbox"));
        } catch (NotFoundException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Chart pin failed!", "Could not pin this metrics chart to My toolbox"));
        }
    }
    
    public String getPinChartHeader(){
        String headerPrefix = "";
        try {
            Long id = -1l;
            
            if (getMetricsLevel() == MetricsLevel.VERIFICATION){
                id = getVerificationId();
                Job job = jobEJB.read(id);
                headerPrefix = job.getDisplayName() + " - ";
            }else if (getMetricsLevel() == MetricsLevel.BUILD){
                id = getBuildId();
                Build build = buildEJB.read(id);
                if (build.getBuildVerificationConf() == null || build.getBuildVerificationConf().getVerificationDisplayName() == null
                        || build.getBuildVerificationConf().getVerificationDisplayName().equals("")){
                    headerPrefix = (build.getJobDisplayName() == null ? "Build" : build.getJobDisplayName()) + " - ";
                }else{
                    headerPrefix = build.getBuildVerificationConf().getVerificationDisplayName() + " - ";
                }
            }else{
                headerPrefix = "System metrics - ";
            }
        } catch (NotFoundException e) {
            log.warn("Could not construct pin-unit header." + e.getMessage());
	}
        String headerStr = headerPrefix + this.getHeader();
        return headerStr;
    }
        
    public MetricsLevel getMetricsLevel() {
        return metricsLevel;
    }

    public void setMetricsLevel(MetricsLevel metricsLevel) {
        this.metricsLevel = metricsLevel;
    }

    public String getJsfComponent() {
        return jsfComponent;
    }

    public void setJsfComponent(String jsfComponent) {
        this.jsfComponent = jsfComponent;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    protected void addMessage(String clientId, Severity severity, String summary, String detail) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(severity, summary, detail);
        fc.addMessage(clientId, facesMessage);
    }

}
