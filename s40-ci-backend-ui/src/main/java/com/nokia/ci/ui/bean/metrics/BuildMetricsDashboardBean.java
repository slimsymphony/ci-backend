package com.nokia.ci.ui.bean.metrics;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
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
public class BuildMetricsDashboardBean extends MetricsDashboardBeanBase {

    private static final Logger log = LoggerFactory.getLogger(BuildMetricsDashboardBean.class);
    @Inject
    BuildEJB buildEJB;
    @Inject
    BuildGroupEJB buildGroupEJB;
    @Inject
    ProjectEJB projectEJB;
    @Inject
    JobEJB jobEJB;
    private Long buildId;
    private Long buildGroupId;
    private Project project;
    private Job job;
    private BuildGroup bg;
    private Build build;

    @Override
    public void init() throws BackendAppException {
        super.init();
        addWidget("memConsumTrendPanel");
        addWidget("memConsumStatusPanel");
        addWidget("buildPassrateLineChartPanel");
        addWidget("buildPassratePieChartPanel");
        addWidget("caseCountTrendPanel");
        addWidget("caseCountStatusPanel");
        addWidget("coverageTrendPanel");
        addWidget("coverageStatusPanel");
        addWidget("failureReasonCountPanel");
        addWidget("failureReasonRatioPanel");
        addWidget("testTriggerrateLineChartPanel");

        buildId = getbuildIdFromRequest();
        build = buildEJB.read(buildId);
        buildGroupId = build.getBuildGroup().getId();

        bg = buildGroupEJB.readSecure(buildGroupId);
        job = jobEJB.read(bg.getJob().getId());
        project = projectEJB.read(job.getProjectId());
    }

    private Long getbuildIdFromRequest() {
        Long id = null;
        FacesContext fc = FacesContext.getCurrentInstance();
        String buildIdString = fc.getExternalContext().getRequestParameterMap().get("buildId");
        if (buildIdString != null) {
            id = Long.parseLong(buildIdString);
        }
        return id;
    }

    public Long getBuildId() {
        return buildId;
    }

    public void setBuildId(Long buildId) {
        this.buildId = buildId;
    }

    public Long getBuildGroupId() {
        return buildGroupId;
    }

    public void setBuildGroupId(Long buildGroupId) {
        this.buildGroupId = buildGroupId;
    }

    public Project getProject() {
        return project;
    }

    public Job getJob() {
        return job;
    }

    public BuildGroup getBg() {
        return bg;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }
}
