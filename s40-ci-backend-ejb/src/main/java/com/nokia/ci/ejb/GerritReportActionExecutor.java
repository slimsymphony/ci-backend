package com.nokia.ci.ejb;

import com.nokia.ci.ejb.annotation.ReportActionExecutorType;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.GerritReportAction;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.util.ConsistencyValidator;
import com.nokia.ci.ejb.util.ReportingUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LocalBean
@Stateless
@ReportActionExecutorType(type = GerritReportAction.class)
public class GerritReportActionExecutor implements ReportActionExecutor<GerritReportAction>, Serializable {

    private static final Logger log = LoggerFactory.getLogger(GerritReportActionExecutor.class);
    @EJB
    private GerritEJB gerritEJB;
    @EJB
    private BuildGroupEJB buildGroupEJB;
    @EJB
    private SysConfigEJB sysConfigEJB;
    @EJB
    private BuildEJB buildEJB;

    @Override
    public void execute(GerritReportAction action, BuildGroup buildGroup) {
        log.info("Executing report action {} for build group {}", action, buildGroup);
        if (action == null || buildGroup == null) {
            return;
        }

        List<String> buildResults = ReportingUtil.formatBuildResults(buildGroup.getBuilds());
        List<String> buildResultDetails = ReportingUtil.formatBuildResultDetailsParams(buildGroupEJB.getBuildResultDetailsParams((buildGroup.getId())));
        List<String> buildClassificationPages = createClassificationLinks(buildGroup.getBuilds());

        reportToGerrit(buildGroup, action, buildResults, buildResultDetails, buildClassificationPages);
    }

    @Override
    public void execute(GerritReportAction action, Build build) {
        log.info("Executing report action {} for build {}", action, build);
        if (action == null || build == null) {
            return;
        }

        List<String> buildResults = ReportingUtil.formatBuildResults(build);

        reportToGerrit(build.getBuildGroup(), action, buildResults, null, null);
    }
    
    @Override
    public void execute(GerritReportAction action, BuildGroup buildGroup, List<Change> changes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Gerrit getGerrit(BuildGroup buildGroup) throws NotFoundException {
        ConsistencyValidator.validate(buildGroup);
        return buildGroup.getJob().getBranch().getProject().getGerrit();
    }

    private void reportToGerrit(BuildGroup buildGroup, GerritReportAction action, List<String> buildResults, List<String> buildResultDetails, List<String> buildClassificationPages) {

        List<Change> changes = buildGroup.getChanges();
        try {
            Gerrit gerrit = getGerrit(buildGroup);

            for (Change c : changes) {
                gerritEJB.verify(gerrit.getId(), c.getCommitId(),
                        buildGroup.getId(), buildGroup.getStatus(), action.getMessage(), buildResults, buildResultDetails, buildClassificationPages,
                        action.getAbandon(), action.getReviewScore(), action.getVerifiedScore());
            }
        } catch (NotFoundException ex) {
            log.error("Can not perform gerrit action for buildGroup {}, Cause: {}", buildGroup, ex.getMessage());
        }
    }

    private List<String> createClassificationLinks(List<Build> builds) {

        List<String> classificationLinks = new ArrayList<String>();
        String baseURL = sysConfigEJB.getValue(SysConfigKey.BASE_URL, "");

        if (!StringUtils.isEmpty(baseURL)) {
            for (Build build : builds) {

                try {
                    if (buildEJB.isClassifiable(build.getId())) {
                        classificationLinks.add(baseURL + "/page/build/classification/" + build.getId());
                    }
                } catch (NotFoundException e) {
                    log.warn("Could not resolve classification of build id={}", build.getId());
                }
            }
        }
        return classificationLinks;
    }
}
