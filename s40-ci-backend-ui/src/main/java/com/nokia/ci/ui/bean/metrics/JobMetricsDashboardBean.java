package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Job;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author larryang
 */
@Named
public class JobMetricsDashboardBean extends MetricsDashboardBeanBase {

    private static final Logger log = LoggerFactory.getLogger(JobMetricsDashboardBean.class);
    @Inject
    JobEJB jobEJB;

    @Override
    public void init() throws BackendAppException {
        super.init();
        addWidget("durationPanel");
        addWidget("passrateLineChartPanel");
        addWidget("passratePieChartPanel");

        Long jobId = getVerificationIdFromRequest();
        Job job = jobEJB.read(jobId);
        if (job.getBranch() != null
                && (job.getBranch().getType() == BranchType.DEVELOPMENT || job.getBranch().getType() == BranchType.MASTER)) {
            addWidget("breakTimeLineChartPanel");
            addWidget("breakTimeTlPanel");
        }

        addWidget("historyTlPanel");
    }

    private Long getVerificationIdFromRequest() {
        Long id = null;
        FacesContext fc = FacesContext.getCurrentInstance();
        String verificationIdString = fc.getExternalContext().getRequestParameterMap().get("verificationId");
        if (verificationIdString != null) {
            id = Long.parseLong(verificationIdString);
        }
        return id;
    }
}
