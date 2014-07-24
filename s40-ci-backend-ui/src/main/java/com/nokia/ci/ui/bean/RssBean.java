package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.SysUserEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.RoleType;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.SysUser;
import com.nokia.ci.ejb.util.Order;
import com.nokia.ci.ui.exception.QueryParamException;
import com.nokia.ci.ui.model.ExtendedBuildGroup;
import com.nokia.ci.ui.util.ExtendedBuildUtil;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@RequestScoped
public class RssBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(RssBean.class);
    private List<ExtendedBuildGroup> buildGroups;
    private Job verification;
    private SysUser user;
    @Inject
    JobEJB jobEJB;
    @Inject
    BuildGroupEJB buildGroupEJB;
    @Inject
    SysConfigEJB sysConfigEJB;
    @Inject
    SysUserEJB sysUserEJB;
    private String baseURL;
    static final int TIMEOUT = 7 * 1000;
    private int socketTimeout = TIMEOUT;
    private int connectionTimeout = TIMEOUT;
    private String title = "404 - Could not find feed";
    private String description = "There is a problem with this RSS feed";
    private String link = "";
    private int ttl = 15;
    private boolean needLogout = false;

    @Override
    protected void init() throws BackendAppException, QueryParamException {
        baseURL = sysConfigEJB.getSysConfig(SysConfigKey.BASE_URL).getConfigValue();
        String verificationId = getQueryParam("verificationId");
        String userId = getQueryParam("userId");
        String secretKey = getMandatoryQueryParam("secretKey");

        SysUser authUser = sysUserEJB.getSysUserWithSecretKey(secretKey);
        try {
            if (verificationId != null) {

                verification = jobEJB.read(Long.parseLong(verificationId));

                if (authUser.getUserRole() != RoleType.SYSTEM_ADMIN
                        && sysUserEJB.hasAccessToProject(authUser.getId(), verification.getBranch().getProject().getId()) == false) {
                    log.warn("Unauthenticated use of verification {} RSS feed", verification);
                    return;
                }

                login(authUser);
                initBuildGroups();
                title = verification.getDisplayName() + " builds";
                description = "Last builds for " + verification.getDisplayName();
                link = baseURL + "/verification/" + verificationId;

            } else if (userId != null) {
                Long uId = Long.parseLong(userId);
                if (authUser.getId().equals(uId) == false) {
                    log.warn("Unauthenticated use of user {} RSS feed", uId);
                    return;
                }

                login(authUser);
                user = sysUserEJB.read(uId);
                initBuildGroups();
                title = user.getRealName() + " builds";
                description = "Last builds for " + user.getRealName();
                link = baseURL + "/my/toolbox/";
            }
        } catch (ServletException ex) {
            log.error("Could not login user for RSS: {}", ex);
        }
    }

    private void login(SysUser authUser) throws ServletException {
        if (request.getUserPrincipal() == null) {
            needLogout = true;
            request.login(authUser.getLoginName(), authUser.getLoginName());
        }
    }

    @PreDestroy
    public void destroy() {
        if (needLogout) {
            try {
                request.logout();
            } catch (ServletException ex) {
                log.error("Could not log out user for RSS: {}", ex);
            }
        }
    }

    private void initBuildGroups() {
        if (verification != null) {
            List<BuildGroup> bgs = jobEJB.getBuildGroups(verification.getId(), 0, 20, "startTime", Order.DESC);
            buildGroups = createExtendedBuilds(bgs);
        } else if (user != null) {
            List<BuildGroup> bgs = sysUserEJB.getBuildGroups(user.getId(), 0, 20, "startTime", Order.DESC);
            buildGroups = createExtendedBuilds(bgs);
        }
    }

    private List<ExtendedBuildGroup> createExtendedBuilds(List<BuildGroup> bgs) {
        List<ExtendedBuildGroup> ret = new ArrayList<ExtendedBuildGroup>();
        if (bgs == null) {
            return ret;
        }
        for (BuildGroup buildGroup : bgs) {
            List<Change> changes = loadChangesIfAny(buildGroup);
            ret.add(ExtendedBuildUtil.createExtendedBuild(buildGroup, changes, socketTimeout, connectionTimeout));
        }
        return ret;
    }

    private List<Change> loadChangesIfAny(BuildGroup buildGroup) {
        List<Change> changes = null;
        if (buildGroup != null) {
            changes = buildGroupEJB.getChanges(buildGroup.getId());
        }
        if (changes == null || changes.isEmpty()) {
            log.debug("Could not find 'Changes' for buildgroup {}", buildGroup);
        }
        return changes;
    }

    public String getBuildDescription(ExtendedBuildGroup exBuildGroup) {
        StringBuilder sb = new StringBuilder();
        BuildGroup buildGroup = exBuildGroup.getBuildGroup();
        sb.append("<p>Phase: ").append(buildGroup.getPhase()).append("</p>");
        sb.append("<p>Status: ").append(buildGroup.getStatus()).append("</p>");
        sb.append("<p>Authors: ").append(exBuildGroup.getAuthorsString()).append("</p>");
        sb.append("<p>Ref spec: ").append(buildGroup.getGerritRefSpec()).append("</p>");
        sb.append("<p>Start time: ").append(buildGroup.getStartTime()).append("</p>");
        if (buildGroup.getEndTime() != null) {
            sb.append("<p>End time: ").append(buildGroup.getStartTime()).append("</p>");
        }
        return sb.toString();
    }

    public String buildURL(ExtendedBuildGroup exBuildGroup) {
        return baseURL + "/page/build/" + exBuildGroup.getBuildGroup().getId();
    }

    public Job getVerification() {
        return verification;
    }

    public void setVerification(Job verification) {
        this.verification = verification;
    }

    public List<ExtendedBuildGroup> getBuildGroups() {
        return buildGroups;
    }

    public void setBuildGroups(List<ExtendedBuildGroup> buildGroups) {
        this.buildGroups = buildGroups;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }
}
