/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildFailureEJB;
import com.nokia.ci.ejb.BuildFailureReasonEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.VerificationEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildFailure;
import com.nokia.ci.ejb.model.BuildFailureReason;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationFailureReason;
import com.nokia.ci.ejb.testresults.NJUnitTestCase;
import com.nokia.ci.ejb.testresults.NJUnitTestFailure;
import com.nokia.ci.ejb.testresults.NJUnitTestSuite;
import com.nokia.ci.ejb.testresults.TestResultParser;
import com.nokia.ci.ejb.util.RelationUtil;
import com.nokia.ci.ui.exception.QueryParamException;
import com.nokia.ci.ui.jenkins.Artifact;
import com.nokia.ci.ui.model.BuildFailureModel;
import com.nokia.ci.ui.testresults.BuildTestResultResolver;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@ViewScoped
public class BuildClassificationBean extends AbstractUIBaseBean {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(BuildClassificationBean.class);
    @Inject
    private BuildEJB buildEJB;
    @Inject
    private BuildGroupEJB buildGroupEJB;
    @Inject
    private VerificationEJB verificationEJB;
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    private BuildFailureEJB buildFailureEJB;
    @Inject
    private BuildFailureReasonEJB buildFailureReasonEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private JobEJB jobEJB;
    @Inject
    private ProjectEJB projectEJB;
    private BuildTestResultResolver btr;
    private List<Artifact> testResults = new ArrayList<Artifact>();
    private List<NJUnitTestSuite> testSuites;
    private List<NJUnitTestCase> filteredCases;
    private List<BuildFailureModel> buildFailures = new ArrayList<BuildFailureModel>();
    private List<BuildFailure> failures = new ArrayList<BuildFailure>();
    private List<VerificationFailureReason> failureReasons = new ArrayList<VerificationFailureReason>();
    private BuildGroup bg;
    private Build build;
    private Build nextClassifableBuild;
    private Build prevClassifableBuild;
    private String buildGroupId;
    private Project project;
    private Job job;
    static final int TIMEOUT = 7 * 1000;
    private int socketTimeout = TIMEOUT;
    private int connectionTimeout = TIMEOUT;

    @Override
    public void init() throws QueryParamException, BackendAppException {
        String buildId = getMandatoryQueryParam("buildId");

        if (buildId != null) {
            build = buildEJB.read(Long.parseLong(buildId));
            bg = buildGroupEJB.readSecure(build.getBuildGroup().getId());
            job = jobEJB.read(bg.getJob().getId());
            project = projectEJB.read(job.getProjectId());
            buildGroupId = bg.getId().toString();

            initTestSuites();
            failures = buildEJB.getBuildFailures(build.getId());
            if (failures.isEmpty()) {
                initBuildFailures();
            }

            initFailureReasons();

            for (BuildFailure f : failures) {
                BuildFailureModel m = new BuildFailureModel();
                m.setFailure(f);
                if (f.getFailureReason() != null) {
                    for (VerificationFailureReason r : failureReasons) {
                        if (r.getName().equals(f.getFailureReason().getName())) {
                            m.setReason(r);
                            break;
                        }
                    }
                    m.setComment(f.getFailureReason().getFailComment());
                }

                if (!StringUtils.isEmpty(f.getRelativePath())) {
                    String url = build.getUrl();
                    url += "/artifact/test_results/" + f.getRelativePath();
                    m.setUrl(url);
                }

                buildFailures.add(m);
            }

            initNextAndPrevBuilds();
        }
    }

    private void initTestSuites() {
        connectionTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_CONNECTION_TIMEOUT, TIMEOUT);
        socketTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_SOCKET_TIMEOUT, TIMEOUT);

        btr = new BuildTestResultResolver(socketTimeout, connectionTimeout);
        testResults = btr.fetchTestResults(bg, build);

        if (testResults == null) {
            return;
        }

        testSuites = new ArrayList<NJUnitTestSuite>();

        for (Artifact a : testResults) {
            TestResultParser parser = new TestResultParser(a.getArtifactPath(), a.getUrl(), socketTimeout, connectionTimeout);
            testSuites.addAll(parser.parseNJUnit());
        }
    }

    private void initFailureReasons() throws NotFoundException {
        BuildVerificationConf bvc = build.getBuildVerificationConf();
        if (bvc == null) {
            throw new NotFoundException("No build verification conf found for build " + build.getId());
        }

        Verification v = verificationEJB.getVerificationByUuid(bvc.getVerificationUuid());

        if (v == null) {
            throw new NotFoundException("No verification found for build " + build.getId());
        }

        failureReasons = verificationEJB.getFailureReasons(v.getId());
    }

    private void initBuildFailures() throws NotFoundException {
        for (NJUnitTestSuite suite : testSuites) {
            List<NJUnitTestCase> testCases = suite.getTestCases();
            for (NJUnitTestCase c : testCases) {
                List<NJUnitTestFailure> fails = c.getFailures();
                if (fails == null || fails.isEmpty()) {
                    continue;
                }

                for (NJUnitTestFailure f : fails) {
                    BuildFailure bf = new BuildFailure();
                    bf.setTestcaseName(c.getName());
                    bf.setMessage(f.getMessage());
                    bf.setType(f.getType());
                    bf.setRelativePath(c.getRelativePath());
                    // For some reason same failures are added twice
                    addIfNotExists(bf);
                }
            }
        }
    }

    private void addIfNotExists(BuildFailure failure) {
        boolean found = false;
        for (BuildFailure f : failures) {
            if (failure.getMessage().equals(f.getMessage()) && failure.getTestcaseName().equals(f.getTestcaseName())
                    && failure.getType().equals(f.getType())) {
                found = true;
                break;
            }
        }
        if (!found) {
            failures.add(failure);
        }
    }

    private void initNextAndPrevBuilds() throws NotFoundException {
        List<Build> builds = buildGroupEJB.getBuilds(bg.getId());
        for (Build b : builds) {
            if (nextClassifableBuild != null && prevClassifableBuild != null) {
                break;
            }

            if (b.getId() < build.getId() && buildEJB.isClassifiable(b.getId())) {
                prevClassifableBuild = b;
                continue;
            }

            if (b.getId() > build.getId() && buildEJB.isClassifiable(b.getId())) {
                nextClassifableBuild = b;
                continue;
            }
        }
    }

    public String save() throws NotFoundException {
        for (BuildFailureModel m : buildFailures) {
            BuildFailure bf = m.getFailure();

            if (bf == null) {
                continue;
            }

            VerificationFailureReason vfr = m.getReason();
            if (vfr != null) {
                BuildFailureReason bfr = bf.getFailureReason();

                if (bfr == null || bfr.getId() == null) {
                    bfr = new BuildFailureReason();
                    RelationUtil.relate(bf, bfr);
                }

                bfr.setFailComment(m.getComment());
                bfr.setName(vfr.getName());
                bfr.setDescription(vfr.getDescription());
                bfr.setSeverity(vfr.getSeverity());
                bfr.setCheckUser(httpSessionBean.getSysUserLoginName());
            } else {
                BuildFailureReason bfr = bf.getFailureReason();

                if (bfr != null && bfr.getId() != null) {
                    RelationUtil.unrelate(bf, bfr);
                    buildFailureReasonEJB.delete(bfr);
                }
            }

            if (bf.getId() == null) {
                bf.setBuild(build);
                buildFailureEJB.create(bf);
            } else {
                buildFailureEJB.update(bf);
            }
        }

        return "buildDetails?faces-redirect=true&buildId=" + bg.getId();
    }

    public String cancelEdit() {
        return "buildDetails?faces-redirect=true&buildId=" + bg.getId();
    }

    public String getReasonDescription(BuildFailureModel m) {
        if (m == null || m.getReason() == null) {
            return "Waiting for analyzation";
        }
        return m.getReason().getDescription();
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public String getBuildGroupId() {
        return buildGroupId;
    }

    public void setBuildGroupId(String buildGroupId) {
        this.buildGroupId = buildGroupId;
    }

    public List<BuildFailureModel> getBuildFailures() {
        return buildFailures;
    }

    public void setBuildFailures(List<BuildFailureModel> buildFailures) {
        this.buildFailures = buildFailures;
    }

    public List<VerificationFailureReason> getFailureReasons() {
        return failureReasons;
    }

    public void setFailureReasons(List<VerificationFailureReason> failureReasons) {
        this.failureReasons = failureReasons;
    }

    public List<NJUnitTestSuite> getTestSuites() {
        return testSuites;
    }

    public void setTestSuites(List<NJUnitTestSuite> testSuites) {
        this.testSuites = testSuites;
    }

    public List<NJUnitTestCase> getFilteredCases() {
        return filteredCases;
    }

    public void setFilteredCases(List<NJUnitTestCase> filteredCases) {
        this.filteredCases = filteredCases;
    }

    public Build getNextClassifableBuild() {
        return nextClassifableBuild;
    }

    public void setNextClassifableBuild(Build nextClassifableBuild) {
        this.nextClassifableBuild = nextClassifableBuild;
    }

    public Build getPrevClassifableBuild() {
        return prevClassifableBuild;
    }

    public void setPrevClassifableBuild(Build prevClassifableBuild) {
        this.prevClassifableBuild = prevClassifableBuild;
    }

    public BuildGroup getBg() {
        return bg;
    }

    public Project getProject() {
        return project;
    }

    public Job getJob() {
        return job;
    }
}
