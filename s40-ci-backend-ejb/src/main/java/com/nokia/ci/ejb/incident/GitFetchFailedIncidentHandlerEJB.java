package com.nokia.ci.ejb.incident;

import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.event.GitFetchFailedEvent;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.git.GitUtils;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfigKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 4/15/13
 * Time: 9:17 AM
 * To change this template use File | Settings | File Templates.
 */
@Stateless
@LocalBean
public class GitFetchFailedIncidentHandlerEJB extends BaseIncidentHandler {

    private static Logger log = LoggerFactory.getLogger(GitFetchFailedIncidentHandlerEJB.class);
    @EJB
    private BranchEJB branchEJB;
    @EJB
    SysConfigEJB sysConfigEJB;

    private final String DESC = "Git failed: Cannot retrieve head of branch '%s' owned by project '%s' (repoURL : '%s')";
    private final int GIT_FETCH_FAILURE_THRESHOLD = 2;

    @Asynchronous
    public void fetchFailed(@Observes(during = TransactionPhase.AFTER_SUCCESS) @GitFetchFailedEvent Long id) {
        log.info("Handling git fetch failure for branch id: {}", id);
        Branch branch = null;
        try {
            branch = branchEJB.read(id);
        } catch (NotFoundException e) {
            log.error("Branch with id : {} not found", id);
            return;
        }
        if (branch.getGitFailureCounter().intValue() == getGitFetchFailureThreshold()) {
            Project project = branch.getProject();
            String repoURL = generateRepositoryURL(project);
            createIncident(IncidentType.SYSTEM, String.format(DESC, branch.getDisplayName(), project.getDisplayName(), repoURL));
        }

    }

    private String generateRepositoryURL(Project project) {
        Gerrit gerrit = project.getGerrit();
        return GitUtils.buildGitFullURL(gerrit.getSshUserName(), gerrit.getUrl(), gerrit.getPort(), project.getGerritProject());
    }

    private int getGitFetchFailureThreshold() {
        return sysConfigEJB.getValue(SysConfigKey.GIT_FETCH_FAILURE_THRESHOLD, GIT_FETCH_FAILURE_THRESHOLD);
    }

}
