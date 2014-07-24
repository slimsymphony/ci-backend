package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.BuildVerificationConfEJB;
import com.nokia.ci.ejb.JobCustomVerificationEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.TemplateCustomVerificationEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.AbstractCustomParam;
import com.nokia.ci.ejb.model.AbstractCustomVerificationConf;
import com.nokia.ci.ejb.model.AbstractTemplateVerificationConf;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.CustomVerificationConf;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobAnnouncement;
import com.nokia.ci.ejb.model.JobTriggerType;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.TemplateCustomVerificationConf;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.Widget;
import com.nokia.ci.ui.exception.QueryParamException;
import com.nokia.ci.ui.jenkins.Artifact;
import com.nokia.ci.ui.jenkins.BuildDetailResolver;
import com.nokia.ci.ui.model.AnnouncementModel;
import com.nokia.ci.ui.model.BuildGroupsLazyDataModel;
import com.nokia.ci.ui.model.ExtendedBuildGroup;
import com.nokia.ci.ui.widget.JobBuildsWidget;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TimeZone;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.LazyDataModel;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bean class for single job.
 *
 * @author jajuutin
 */
@Named
@ViewScoped
public class JobBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(JobBean.class);
    private Job verification;
    private Project project;
    private AnnouncementModel announcementModel;
    private LazyDataModel<ExtendedBuildGroup> lazyDataModel;
    private List<Verification> preVerifications = new ArrayList<Verification>();
    private List<Verification> postVerifications = new ArrayList<Verification>();
    private List<? extends AbstractTemplateVerificationConf> enabledVerifications = new ArrayList<AbstractTemplateVerificationConf>();
    private ArrayList<AbstractCustomVerificationConf> enabledCustomVerifications = new ArrayList<AbstractCustomVerificationConf>();
    private Map<AbstractCustomVerificationConf, String> formattedCustomParameters = new HashMap<AbstractCustomVerificationConf, String>();
    @Inject
    JobEJB jobEJB;
    @Inject
    BuildEJB buildEJB;
    @Inject
    BuildGroupEJB buildGroupEJB;
    @Inject
    ProjectEJB projectEJB;
    @Inject
    BuildVerificationConfEJB buildVerificationConfEJB;
    @Inject
    private JobCustomVerificationEJB jobCustomVerificationEJB;
    @Inject
    private TemplateCustomVerificationEJB templateCustomVerificationEJB;
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private SysUserEJB sysUserEJB;
    static final int TIMEOUT = 7 * 1000;
    private int socketTimeout = TIMEOUT;
    private int connectionTimeout = TIMEOUT;

    @Override
    protected void init() throws BackendAppException, QueryParamException {
        String jobId = getMandatoryQueryParam("verificationId");
        log.debug("Finding job {} for editing!", jobId);
        verification = jobEJB.readSecure(Long.parseLong(jobId));
        project = projectEJB.read(verification.getProjectId());

        if (verification.getDisabled() != null && verification.getDisabled().booleanValue()) {
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Job is disabled.", "Job is currently disabled and no new builds will be started.");
        }

        connectionTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_CONNECTION_TIMEOUT, TIMEOUT);
        socketTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_SOCKET_TIMEOUT, TIMEOUT);

        initAnnouncementModel();
        initLazyDataModel();
        initVerifications();
        initVerificationCustomParameters();
    }

    public LazyDataModel<ExtendedBuildGroup> getLazyDataModel() {
        return lazyDataModel;
    }

    public AnnouncementModel getAnnouncementModel() {
        return announcementModel;
    }

    /**
     * @return the verification
     */
    public Job getVerification() {
        return verification;
    }

    public Project getProject() {
        return project;
    }

    /**
     * @param verification the verification to set
     */
    public void setVerification(Job verification) {
        this.verification = verification;
    }

    public boolean isToolboxOrDraftVerification() {
        if (verification == null || verification.getBranch() == null) {
            return false;
        }

        if (verification.getBranch().getType() == BranchType.TOOLBOX || verification.getBranch().getType() == BranchType.DRAFT) {
            return true;
        }

        return false;
    }

    public boolean hasVerifications() {
        if (preVerifications.isEmpty() && postVerifications.isEmpty()
                && enabledVerifications.isEmpty() && enabledCustomVerifications.isEmpty()) {
            return false;
        }
        return true;
    }

    public List<Verification> getPreVerifications() {
        return preVerifications;
    }

    public List<Verification> getPostVerifications() {
        return postVerifications;
    }

    public List<? extends AbstractTemplateVerificationConf> getEnabledVerifications() {
        return enabledVerifications;
    }

    public List<? extends AbstractCustomVerificationConf> getEnabledCustomVerifications() {
        return enabledCustomVerifications;
    }

    public void onTabViewChange(TabChangeEvent event) {
        if (event.getTab().getId().equalsIgnoreCase("verificationDetails")) {
            RequestContext.getCurrentInstance().execute("buildsPollerVar.start()");
        } else {
            RequestContext.getCurrentInstance().execute("buildsPollerVar.stop()");
        }
    }

    public void pinToMyToolbox() {
        try {
            if (verification == null) {
                return;
            }

            JobBuildsWidget wgt = new JobBuildsWidget(verification.getId(), verification.getDisplayName() + " builds");
            Widget jobWidget = wgt.getPersistentWidget();
            sysUserEJB.addWidget(httpSessionBean.getSysUserId(), jobWidget);

            addMessage(FacesMessage.SEVERITY_INFO,
                    "Job builds added", "Job builds table is now on your toolbox");
        } catch (NotFoundException ex) {
            log.error("Could not bind verification {} builds to toolbox", verification);
        }
    }

    private void initAnnouncementModel() {
        List<JobAnnouncement> appropriateAnnouncement = jobEJB.getJobAnnouncements(verification.getId());
        announcementModel = new AnnouncementModel(appropriateAnnouncement);
    }

    private void initLazyDataModel() {
        lazyDataModel = new BuildGroupsLazyDataModel(jobEJB, buildGroupEJB, sysConfigEJB, verification.getId());
    }

    private void initVerifications() {
        preVerifications = new ArrayList<Verification>();
        postVerifications = new ArrayList<Verification>();
        enabledVerifications = new ArrayList<AbstractTemplateVerificationConf>();
        enabledCustomVerifications = new ArrayList<AbstractCustomVerificationConf>();

        if (verification != null) {
            try {
                preVerifications = jobEJB.getPreVerifications(verification.getId());
                postVerifications = jobEJB.getPostVerifications(verification.getId());
                enabledVerifications = jobEJB.getValidJobAndTemplateVerificaitonConfs(verification.getId());
                enabledCustomVerifications.addAll(jobEJB.getEnabledCustomVerificationConfs(verification.getId()));
                enabledCustomVerifications.addAll(jobEJB.getEnabledTemplateCustomVerificationConfs(verification.getId()));
            } catch (NotFoundException e) {
                log.warn("Cannot find job with id {}", verification.getId());
            }
        }
    }

    private void initVerificationCustomParameters() {
        formattedCustomParameters = new HashMap<AbstractCustomVerificationConf, String>();
        for (AbstractCustomVerificationConf v : enabledCustomVerifications) {
            formattedCustomParameters.put(v, formatCustomParameterString(v));
        }
    }

    private String formatCustomParameterString(AbstractCustomVerificationConf v) {
        if (v == null) {
            return "";
        }
        List<? extends AbstractCustomParam> customParameters = null;

        if (v instanceof CustomVerificationConf) {
            customParameters = jobCustomVerificationEJB.getCustomVerificationParams(v.getCustomVerification().getId());
        } else if (v instanceof TemplateCustomVerificationConf) {
            customParameters = templateCustomVerificationEJB.getCustomVerificationParams(v.getCustomVerification().getId());
        }
        if (customParameters == null || customParameters.isEmpty()) {
            return "";
        }

        long startTime = System.currentTimeMillis();
        log.debug("Formating custom parameters for {}", v);

        ListIterator<? extends AbstractCustomParam> listIterator = customParameters.listIterator();
        StringBuilder sb = new StringBuilder();
        while (listIterator.hasNext()) {
            AbstractCustomParam param = listIterator.next();
            if (listIterator.previousIndex() > 0 && sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(param.getCustomParam().getParamKey()).append("=").append(param.getParamValue());
        }
        String s = sb.toString();
        log.debug("Formating done in {}ms", System.currentTimeMillis() - startTime);
        return s;
    }

    public String getCustomParameterString(AbstractCustomVerificationConf v) {
        return formattedCustomParameters.get(v);
    }

    public String getNextRunCounter() {
        if (verification.getTriggerType() == JobTriggerType.SCHEDULE && verification.getCronExpression() != null) {
            try {
                Date lastRun = verification.getLastRun();
                if (lastRun == null) {
                    return "Next change";
                }
                CronExpression cron = new CronExpression(verification.getCronExpression());
                if (!StringUtils.isEmpty(verification.getCronTimezone())) {
                    cron.setTimeZone(TimeZone.getTimeZone(verification.getCronTimezone()));
                }
                Long nextRun;
                Date lastCronCheck = verification.getLastCronCheck();
                if (lastCronCheck != null && lastCronCheck.after(lastRun)) {
                    nextRun = cron.getNextValidTimeAfter(lastCronCheck).getTime() - System.currentTimeMillis();
                } else {
                    nextRun = cron.getNextValidTimeAfter(lastRun).getTime() - System.currentTimeMillis();
                }
                if (nextRun < 0) {
                    return "Next change";
                }
                return DurationFormatUtils.formatDurationWords(nextRun, true, true);
            } catch (ParseException e) {
                return null;
            }
        } else if (verification.getTriggerType() == JobTriggerType.POLL && verification.getPollInterval() != null) {
            Long sinceLastRun = 0L;
            Long pollInterval = new Long(verification.getPollInterval() * 60 * 1000);
            if (verification.getLastRun() != null) {
                sinceLastRun = System.currentTimeMillis() - verification.getLastRun().getTime();
            }
            pollInterval -= sinceLastRun;
            if (pollInterval < 0) {
                return "Next change";
            }
            return DurationFormatUtils.formatDurationWords(pollInterval, true, true);
        }
        return null;
    }
}
