package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.model.TestResultType;
import com.nokia.ci.ejb.testresults.MemUsageObject;
import com.nokia.ci.ejb.testresults.NJUnitTestCase;
import com.nokia.ci.ejb.testresults.NJUnitTestSuite;
import com.nokia.ci.ejb.testresults.TestResultParser;
import com.nokia.ci.ejb.testresults.WarningObject;
import com.nokia.ci.ui.exception.QueryParamException;
import com.nokia.ci.ui.jenkins.Artifact;
import com.nokia.ci.ui.testresults.BuildTestResultResolver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@ViewScoped
public class BuildTestResultsBean extends AbstractUIBaseBean {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(BuildTestResultsBean.class);
    private Build build;
    private List<Artifact> testResults = new ArrayList<Artifact>();
    private List<Artifact> diffTestResults = new ArrayList<Artifact>();
    @Inject
    private BuildEJB buildEJB;
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    private BuildGroupEJB buildGroupEJB;
    @Inject
    private ProjectEJB projectEJB;
    @Inject
    private JobEJB jobEJB;
    private String verificationId;
    private String buildGroupId;
    private BuildTestResultResolver btr;
    static final int TIMEOUT = 7 * 1000;
    private int socketTimeout = TIMEOUT;
    private int connectionTimeout = TIMEOUT;
    private Set<TestResultType> types;
    private List<NJUnitTestSuite> testSuites;
    private List<NJUnitTestSuite> diffTestsuites;
    private List<NJUnitTestCase> filteredCases;
    private TreeNode memUsageTree;
    private TreeNode warningsTree;
    private BuildGroup bg;
    private BuildGroup diffBg;
    private List<BuildGroup> diffableBuildGroups;
    private Build diffBuild;
    private MemUsageObject memUsageModel;
    private WarningObject warningModel;
    private String linkUrl;
    private Project project;
    private Job job;

    @Override
    protected void init() throws QueryParamException, BackendAppException {
        log.debug("Initializing...");

        String buildId = getMandatoryQueryParam("buildId");
        buildGroupId = getQueryParam("buildGroupId");
        verificationId = getQueryParam("verificationId");
        diffBg = null;
        diffBuild = null;

        connectionTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_CONNECTION_TIMEOUT, TIMEOUT);
        socketTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_SOCKET_TIMEOUT, TIMEOUT);

        build = buildEJB.read(Long.parseLong(buildId));
        bg = buildGroupEJB.readSecure(build.getBuildGroup().getId());

        job = jobEJB.read(bg.getJob().getId());
        project = projectEJB.read(job.getProjectId());

        if (build.getBuildVerificationConf() == null) {
            log.warn("Trying to get test results for build {} without verification conf", build);
            return;
        }

        types = buildEJB.getTestResultTypes(build);

        if (!types.isEmpty()) {
            btr = new BuildTestResultResolver(socketTimeout, connectionTimeout);
            if (build != null && bg != null) {
                testResults = btr.fetchTestResults(bg, build);
            }
            initLink();
            if (types.size() == 1 && types.contains(TestResultType.LINK)) {
                forwardLink();
                return;
            }

            initDiffBuilds();
            parseTestResults();
        }
    }

    private void initDiffBuilds() {
        try {
            log.info("Finding diff build for build {}", build);
            diffableBuildGroups = new ArrayList<BuildGroup>();
            List<BuildGroup> previousBuildGroups = buildGroupEJB.getPreviousBuildGroupsWithSameJob(bg.getId());
            if (previousBuildGroups.isEmpty()) {
                return;
            }

            BuildVerificationConf oldConf = build.getBuildVerificationConf();
            if (oldConf == null) {
                return;
            }
            for (BuildGroup buildGroup : previousBuildGroups) {
                List<Build> builds = buildGroupEJB.getBuilds(bg.getId());
                for (Build b : builds) {
                    BuildVerificationConf newConf = b.getBuildVerificationConf();

                    if (newConf == null) {
                        continue;
                    }

                    if (oldConf.sameType(newConf)) {
                        log.info("Found diff build {} for build {}", b, build);
                        diffableBuildGroups.add(buildGroup);
                        continue;
                    }
                }
            }

        } catch (NotFoundException ex) {
            log.error("Could not find previous build groups for build group {}", bg);
        }
    }

    public void diffSelected() {
        if (diffBg == null) {
            diffBuild = null;
            return;
        }

        log.info("Diffing {} to {}", bg, diffBg);
        BuildVerificationConf oldConf = build.getBuildVerificationConf();
        if (oldConf == null) {
            return;
        }

        List<Build> builds = buildGroupEJB.getBuilds(diffBg.getId());
        for (Build b : builds) {
            BuildVerificationConf newConf = b.getBuildVerificationConf();

            if (newConf == null) {
                continue;
            }

            if (oldConf.sameType(newConf)) {
                diffBuild = b;
                break;
            }
        }

        if (diffBuild == null) {
            diffableBuildGroups.remove(diffBg);
            addMessage(FacesMessage.SEVERITY_WARN,
                    "Could not load diff file!", "Diff file could not be found!");
            return;
        }

        if (diffBuild != null && diffBg != null) {
            diffTestResults = btr.fetchTestResults(diffBg, diffBuild);
        }

        if (diffTestResults != null && !diffTestResults.isEmpty()) {
            if (types.contains(TestResultType.NJUNIT)) {
                createNJUnitSuites();
            }

            if (types.contains(TestResultType.MEM_CONSUMPTION)) {
                createMemTree();
            }

            if (types.contains(TestResultType.WARNING)) {
                createWarningTree();
            }
        }
    }

    private void parseTestResults() {
        if (build.getBuildVerificationConf() == null) {
            log.debug("Old style build {}, not parsing test results", build);
            return;
        }

        if (testResults != null && !testResults.isEmpty()) {
            if (types.contains(TestResultType.NJUNIT)) {
                createNJUnitSuites();
            }

            if (types.contains(TestResultType.MEM_CONSUMPTION)) {
                createMemTree();
            }

            if (types.contains(TestResultType.WARNING)) {
                createWarningTree();
            }
        }
    }

    private void initLink() {
        String indexFile = build.getBuildVerificationConf().getTestResultIndexFile();
        if (StringUtils.isEmpty(indexFile)) {
            return;
        }

        for (Artifact a : testResults) {
            if (a.getUrl() != null && a.getUrl().toString().contains(indexFile)) {
                linkUrl = a.getUrl().toString();
                break;
            }
        }
    }

    private void forwardLink() {
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            externalContext.redirect(linkUrl);
        } catch (IOException ex) {
            log.warn("Could not redirect to url {}", linkUrl);
        }
    }

    private void createNJUnitSuites() {
        if (testSuites == null || testSuites.isEmpty()) {
            testSuites = new ArrayList<NJUnitTestSuite>();
            testSuites = parseNJUnitTestResults(testResults);
        }

        if (testSuites.isEmpty()) {
            log.info("Could not find test results file for build {}", build);
            return;
        }

        if (diffBuild != null) {
            diffTestsuites = new ArrayList<NJUnitTestSuite>();
            diffTestsuites = parseNJUnitTestResults(diffTestResults);

            if (diffTestsuites.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN,
                        "Could not load diff file!", "Diff file could not be found!");
                log.info("Could not find test results file for build {}", diffBuild);
                diffBuild = null;
            } else {
                List<NJUnitTestSuite> removedSuites = new ArrayList<NJUnitTestSuite>();
                for (NJUnitTestSuite diffSuite : diffTestsuites) {
                    boolean found = false;
                    for (NJUnitTestSuite suite : testSuites) {
                        if (diffSuite.equals(suite)) {
                            suite.diffTo(diffSuite);
                            found = true;
                            break;
                        }
                    }

                    if (found == false) {
                        diffSuite.setName(diffSuite.getName() + " (REMOVED)");

                        diffSuite.setDiffFailures(diffSuite.getNumFailures());
                        diffSuite.setDiffSuccess(diffSuite.getNumSuccess());
                        diffSuite.setDiffTestcases(diffSuite.getNumTestcases());
                        diffSuite.setDiffFailPercent(diffSuite.getFailPercent());
                        diffSuite.setDiffSuccessPercent(diffSuite.getSuccessPercent());

                        diffSuite.setNumFailures(0);
                        diffSuite.setNumSuccess(0);
                        diffSuite.setNumTestcases(0);
                        diffSuite.setSuccessPercent(0);
                        diffSuite.setFailPercent(0);

                        removedSuites.add(diffSuite);
                    }
                }

                for (NJUnitTestSuite suite : testSuites) {
                    boolean found = false;
                    for (NJUnitTestSuite diffSuite : diffTestsuites) {
                        if (suite.equals(diffSuite)) {
                            found = true;
                            break;
                        }
                    }

                    if (found == false) {
                        suite.setName(suite.getName() + " (NEW)");
                    }
                }

                testSuites.addAll(removedSuites);
            }
        }
    }

    private List<NJUnitTestSuite> parseNJUnitTestResults(List<Artifact> testResults) {
        List<NJUnitTestSuite> ret = new ArrayList<NJUnitTestSuite>();
        for (Artifact a : testResults) {
            TestResultParser parser = new TestResultParser(a.getArtifactPath(), a.getUrl(), socketTimeout, connectionTimeout);
            ret.addAll(parser.parseNJUnit());
        }
        return ret;
    }

    private void createMemTree() {
        for (Artifact a : testResults) {
            TestResultParser parser = new TestResultParser(a.getArtifactPath(), a.getUrl(), socketTimeout, connectionTimeout);
            memUsageModel = parser.parseMemUsageJSON();
            if (memUsageModel != null) {
                break;
            }
        }

        if (memUsageModel == null) {
            log.info("Could not find test results file for build {}", build);
            return;
        }

        if (diffBuild != null) {
            MemUsageObject diffModel = null;

            for (Artifact a : diffTestResults) {
                TestResultParser parser = new TestResultParser(a.getArtifactPath(), a.getUrl(), socketTimeout, connectionTimeout);
                diffModel = parser.parseMemUsageJSON();

                if (diffModel != null) {
                    break;
                }
            }

            if (diffModel == null) {
                addMessage(FacesMessage.SEVERITY_WARN,
                        "Could not load diff file!", "Diff file could not be found!");
                log.info("Could not find test results file for build {}", diffBuild);
            } else {
                memUsageModel.diffRecursiveTo(diffModel);
            }
        }

        memUsageTree = new DefaultTreeNode("root", null);
        TreeNode memoryRoot = new DefaultTreeNode(memUsageModel, memUsageTree);
        memoryRoot.setExpanded(true);

        parseAll(memUsageModel, memoryRoot);
    }

    private void parseAll(MemUsageObject m, TreeNode root) {
        parseComponents(m, root);
        parseLibraries(m, root);
        parseObjects(m, root);
    }

    private void parseComponents(MemUsageObject m, TreeNode root) {
        List<MemUsageObject> components = m.getComponents();
        if (components == null || components.isEmpty()) {
            return;
        }

        for (MemUsageObject c : components) {
            TreeNode t = new DefaultTreeNode(c, root);
            parseAll(c, t);
        }
    }

    private void parseLibraries(MemUsageObject m, TreeNode root) {
        List<MemUsageObject> libraries = m.getLibraries();
        if (libraries == null || libraries.isEmpty()) {
            return;
        }

        for (MemUsageObject l : libraries) {
            l.setComponent(l.getFilename());
            TreeNode t = new DefaultTreeNode(l, root);
            parseAll(l, t);
        }
    }

    private void parseObjects(MemUsageObject m, TreeNode root) {
        List<MemUsageObject> objects = m.getObjects();
        if (objects == null || objects.isEmpty()) {
            return;
        }

        for (MemUsageObject o : objects) {
            o.setComponent(o.getFilename());
            TreeNode t = new DefaultTreeNode(o, root);
            parseAll(o, t);
        }
    }

    private void createWarningTree() {
        for (Artifact a : testResults) {
            TestResultParser parser = new TestResultParser(a.getArtifactPath(), a.getUrl(), socketTimeout, connectionTimeout);
            warningModel = parser.parseWarnings();
            if (warningModel != null) {
                break;
            }
        }

        if (warningModel == null) {
            log.info("Could not find test results file for build {}", build);
            return;
        }

        if (diffBuild != null) {
            WarningObject diffModel = null;

            for (Artifact a : diffTestResults) {
                TestResultParser parser = new TestResultParser(a.getArtifactPath(), a.getUrl(), socketTimeout, connectionTimeout);
                diffModel = parser.parseWarnings();
                if (diffModel != null) {
                    break;
                }
            }

            if (diffModel == null) {
                addMessage(FacesMessage.SEVERITY_WARN,
                        "Could not load diff file!", "Diff file could not be found!");
                log.info("Could not find test results file for build {}", diffBuild);
            } else {
                warningModel.diffRecursiveTo(diffModel, warningModel);
            }
        }

        warningsTree = new DefaultTreeNode("root", null);
        warningsTree.setExpanded(true);
        parseWarnings(warningModel, warningsTree);
    }

    private void parseWarnings(WarningObject w, TreeNode root) {
        List<WarningObject> warnings = w.getWarnings();
        if (warnings == null || warnings.isEmpty()) {
            return;
        }

        for (WarningObject obj : warnings) {
            if (obj.getFile() == null || obj.getFile().isEmpty()) {
                // This is path object
                obj.setFile(obj.getPath());
            }

            TreeNode t = new DefaultTreeNode(obj, root);
            parseWarnings(obj, t);
        }
    }

    public WarningObject getWarningModel() {
        return warningModel;
    }

    public void setWarningModel(WarningObject warningModel) {
        this.warningModel = warningModel;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(String verificationId) {
        this.verificationId = verificationId;
    }

    public String getBuildGroupId() {
        return buildGroupId;
    }

    public void setBuildGroupId(String buildGroupId) {
        this.buildGroupId = buildGroupId;
    }

    public BuildGroup getDiffBg() {
        return diffBg;
    }

    public void setDiffBg(BuildGroup diffBg) {
        this.diffBg = diffBg;
    }

    public List<BuildGroup> getDiffableBuildGroups() {
        return diffableBuildGroups;
    }

    public void setDiffableBuildGroups(List<BuildGroup> diffableBuildGroups) {
        this.diffableBuildGroups = diffableBuildGroups;
    }

    public Build getDiffBuild() {
        return diffBuild;
    }

    public void setDiffBuild(Build diffBuild) {
        this.diffBuild = diffBuild;
    }

    public Set<TestResultType> getTypes() {
        return types;
    }

    public TreeNode getMemUsageTree() {
        return memUsageTree;
    }

    public List<NJUnitTestSuite> getTestSuites() {
        return testSuites;
    }

    public TreeNode getWarningsTree() {
        return warningsTree;
    }

    public void setWarningsTree(TreeNode warningsTree) {
        this.warningsTree = warningsTree;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public List<NJUnitTestCase> getFilteredCases() {
        return filteredCases;
    }

    public void setFilteredCases(List<NJUnitTestCase> filteredCases) {
        this.filteredCases = filteredCases;
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
