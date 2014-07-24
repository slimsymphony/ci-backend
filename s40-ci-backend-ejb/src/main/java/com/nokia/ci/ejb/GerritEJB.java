package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.InvalidArgumentException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.gerrit.GerritClient;
import com.nokia.ci.ejb.gerrit.GerritQueryParameters;
import com.nokia.ci.ejb.gerrit.model.GerritDetail;
import com.nokia.ci.ejb.model.BuildStatus;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Gerrit_;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfigKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class GerritEJB extends CrudFunctionality<Gerrit> implements Serializable {

    private static final String GERRIT_MESSAGE_NEWLINE_CHAR_DEFAULT = "\n ";
    private static Logger log = LoggerFactory.getLogger(GerritEJB.class);
    @Inject
    SysConfigEJB sysConfigEJB;
    @Inject
    SysUserEJB sysUserEJB;
    private static String DEFAULT_QUERY = "status:open";

    public GerritEJB() {
        super(Gerrit.class);
    }

    public List<GerritDetail> getOpenCommits(String userEmail,
            Long gerritId, String branch, String project) throws NotFoundException {
        // If branch is not given, then default to "toolbox"
        if (StringUtils.isEmpty(branch)) {
            branch = "toolbox";
        }

        StringBuilder sb = new StringBuilder("status:open branch:");
        sb.append(branch);
        if (!StringUtils.isEmpty(project)) {
            sb.append(" project:");
            sb.append(project);
        }
        if (!StringUtils.isEmpty(userEmail)) {
            sb.append(" owner:");
            sb.append(userEmail);
        }

        return gerritQuery(sb.toString(), gerritId);
    }

    public List<GerritDetail> gerritQuery(String query, Long gerritId) throws NotFoundException {
        GerritClient client = getGerritClient(gerritId);
        return client.query(query);
    }

    public void verify(Long gerritId, String patchSetRevision, Long buildId, BuildStatus status,
            String message, List<String> buildResults, List<String> buildResultDetails, List<String> buildClassificationPages, boolean abandon, Integer reviewScore, Integer verifiedScore) throws NotFoundException {
        GerritClient gerritClient = getGerritClient(gerritId);
        String baseURL = sysConfigEJB.getSysConfig(SysConfigKey.BASE_URL).getConfigValue();
        String gerritNewline = sysConfigEJB.getValue(SysConfigKey.GERRIT_MESSAGE_NEWLINE_CHAR, GERRIT_MESSAGE_NEWLINE_CHAR_DEFAULT);
        gerritClient.verify(patchSetRevision, buildId, status.toString(), baseURL, message, buildResults, buildResultDetails, buildClassificationPages, abandon, reviewScore, verifiedScore, gerritNewline);
    }

    private GerritClient getGerritClient(Long gerritId) throws NotFoundException {
        Gerrit gerrit = read(gerritId);
        return new GerritClient(gerrit.getUrl(), gerrit.getPort(),
                gerrit.getPrivateKeyPath(), gerrit.getSshUserName());
    }

    @Asynchronous
    public Future<List<GerritDetail>> fetchChanges(GerritQueryParameters detail) throws InvalidArgumentException {
        List<GerritDetail> details = new ArrayList<GerritDetail>();
        try {

            if (StringUtils.isEmpty(detail.getBranchName())) {
                throw new InvalidArgumentException("Branch name is required!");
            }

            if (StringUtils.isEmpty(detail.getProjectName())) {
                throw new InvalidArgumentException("Project name is required!");
            }

            if (detail.getGerritId() == null) {
                throw new InvalidArgumentException("Gerrit id is required!");
            }

            StringBuilder query = new StringBuilder();
            query.append(DEFAULT_QUERY);
            query.append(" branch:").append(detail.getBranchName());
            query.append(" project:").append(detail.getProjectName());
            query.append(" -Verified=0 -Verified=+1 -Verified=-1");

            log.info("querying gerrit({}): {}", detail.getGerritId(), query.toString());
            details = gerritQuery(query.toString(), detail.getGerritId());
        } catch (NotFoundException nfe) {
            log.error("Query failed! Gerrit entity removed?", nfe);
        }

        return new AsyncResult<List<GerritDetail>>(details);
    }

    public List<Project> getProjects(Long id) {
        return getJoinList(id, Gerrit_.projects);
    }

    public Gerrit getGerritWithHostName(String hostName) throws NotFoundException {
        log.debug("Finding gerrit with host name {}", hostName);
        CriteriaQuery<Gerrit> query = cb.createQuery(Gerrit.class);
        Root<Gerrit> gerrit = query.from(Gerrit.class);

        Predicate gerritPredicate = cb.equal(gerrit.get(Gerrit_.url), hostName);
        query.where(gerritPredicate);
        try {
            Gerrit g = em.createQuery(query).getSingleResult();
            log.debug("Found gerrit with id {}", g.getId());
            return g;
        } catch (NoResultException e) {
            log.warn("Gerrit with host name {} not found", hostName);
            throw new NotFoundException("Gerrit with host name " + hostName + " not found!");
        }
    }
}
