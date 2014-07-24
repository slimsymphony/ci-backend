package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.BuildGroupEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ui.exception.QueryParamException;
import com.nokia.ci.ui.jenkins.Artifact;
import com.nokia.ci.ui.jenkins.BuildDetailResolver;
import com.nokia.ci.ui.testresults.AbstractBuildArtifactResolver;
import com.nokia.ci.ui.testresults.ProxyArtifactResolver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
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
public class BuildArtifactBean extends AbstractUIBaseBean {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(BuildArtifactBean.class);
    private Build build;
    private List<Artifact> artifacts = new ArrayList<Artifact>();
    private List<Artifact> testResults = new ArrayList<Artifact>();
    @Inject
    private SysConfigEJB sysConfigEJB;
    @Inject
    private BuildEJB buildEJB;
    @Inject
    private HttpSessionBean httpSessionBean;
    @Inject
    private BuildGroupEJB buildGroupEJB;
    @Inject
    private JobEJB jobEJB;
    @Inject
    private ProjectEJB projectEJB;
    static final int TIMEOUT = 7 * 1000;
    private int socketTimeout = TIMEOUT;
    private int connectionTimeout = TIMEOUT;
    private Artifact selectedArtifact;
    private TreeNode root;
    private AbstractBuildArtifactResolver resolver;
    private String verificationId;
    private String buildGroupId;
    private Job job;
    private Project project;
    private BuildGroup bg;

    @Override
    protected void init() throws BackendAppException, QueryParamException {
        log.debug("Initializing BuildArtifactBean");

        connectionTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_CONNECTION_TIMEOUT, TIMEOUT);
        socketTimeout = sysConfigEJB.getValue(SysConfigKey.HTTP_CLIENT_SOCKET_TIMEOUT, TIMEOUT);

        String buildId = getMandatoryQueryParam("buildId");
        buildGroupId = getQueryParam("buildGroupId");
        verificationId = getQueryParam("verificationId");

        build = buildEJB.read(Long.parseLong(buildId));

        artifacts = new ArrayList<Artifact>();
        testResults = new ArrayList<Artifact>();
        root = new DefaultTreeNode("root", null);

        bg = buildGroupEJB.readSecure(build.getBuildGroup().getId());
        job = jobEJB.read(bg.getJob().getId());
        project = projectEJB.read(job.getProjectId());

        if (!httpSessionBean.hasAccessToArtifactsAndReports(job.getBranch().getProject().getId())) {
            throw new UnauthorizedException();
        }

        if (StringUtils.isEmpty(bg.getBuildGroupCIServer().getProxyServerUrl())) {
            resolver = new BuildDetailResolver(build.getUrl(), socketTimeout, connectionTimeout);
        } else {
            StringBuilder artifactUrl = new StringBuilder(bg.getBuildGroupCIServer().getProxyServerUrl());
            artifactUrl.append("/").append(bg.getId()).append("/").append(build.getId());
            resolver = new ProxyArtifactResolver(artifactUrl.toString(), socketTimeout, connectionTimeout);
        }

        initArtifacts();
        initTestResults();
        initTree();
    }

    private void initArtifacts() {
        if (build != null) {
            long startTime = System.currentTimeMillis();
            log.debug("Getting artifacts for {}", build);

            artifacts = resolver.fetchArtifacts();
            if (artifacts != null && !artifacts.isEmpty()) {
                try {
                    artifacts.add(new Artifact("(all files in zip)", new URL(build.getUrl() + "/artifact/*zip*/archive.zip")));
                } catch (MalformedURLException e) {
                    log.warn("URL parsing failed", e);
                }
            }

            log.debug("Got artifacts in {}ms", System.currentTimeMillis() - startTime);
        }

        if (artifacts == null) {
            artifacts = new ArrayList<Artifact>();
        }
    }

    private void initTestResults() {
        if (build != null) {
            long startTime = System.currentTimeMillis();
            log.debug("Getting test results for {}", build);

            testResults = resolver.fetchTestResults();
            if (testResults != null && !testResults.isEmpty()) {
                try {
                    testResults.add(new Artifact("(all files in zip)", new URL(build.getUrl() + "/artifact/test_results/*zip*/archive.zip")));
                } catch (MalformedURLException e) {
                    log.warn("URL parsing failed", e);
                }
            }

            log.debug("Got test results in {}ms", System.currentTimeMillis() - startTime);
        }

        if (testResults == null) {
            testResults = new ArrayList<Artifact>();
        }
    }

    private void initTree() {
        root = new DefaultTreeNode("root", null);

        if (!artifacts.isEmpty()) {
            TreeNode buildArtifacts = new DefaultTreeNode(new Artifact("Build artifacts"), root);
            buildArtifacts.setExpanded(true);
            for (Artifact a : artifacts) {
                TreeNode t = new DefaultTreeNode(a, buildArtifacts);
            }
        }

        if (!testResults.isEmpty()) {
            TreeNode testResultArtifacts = new DefaultTreeNode(new Artifact("Test results"), root);
            testResultArtifacts.setExpanded(true);
            for (Artifact a : testResults) {
                String url = a.getUrl().getPath();
                List<String> paths = new LinkedList<String>(Arrays.asList(url.split("/")));
                paths.removeAll(Arrays.asList("", null));
                paths.remove(paths.size() - 1);

                Iterator<String> it = paths.iterator();

                while (it.hasNext()) {
                    String s = it.next();
                    it.remove();
                    if (s.equals("test_results")) {
                        break;
                    }
                }

                TreeNode folder = findFolder(paths, testResultArtifacts);
                TreeNode t = new DefaultTreeNode(a, folder);
            }
        }
    }

    public TreeNode findFolder(List<String> paths, TreeNode base) {
        if (paths.isEmpty()) {
            return base;
        }

        TreeNode ret = base;
        String s = paths.get(0);
        Boolean found = false;

        for (TreeNode t : ret.getChildren()) {
            Artifact a = (Artifact) t.getData();

            if (a.getName().equals(s)) {
                paths.remove(s);
                ret = findFolder(paths, t);
                found = true;
                break;
            }
        }

        if (found == false) {
            ret = new DefaultTreeNode(new Artifact(s), base);
        }

        return ret;
    }

    public void download() {
        if (selectedArtifact != null) {
            try {
                FacesContext context = FacesContext.getCurrentInstance();
                HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
                response.sendRedirect(selectedArtifact.getUrl().toString());
            } catch (IOException ex) {
                log.warn("Could not download file {}", selectedArtifact);
            }
        }
    }

    public Boolean hasArtifacts() {
        if (artifacts.isEmpty() && testResults.isEmpty()) {
            return false;
        }

        return true;
    }

    public Build getBuild() {
        return build;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public List<Artifact> getTestResults() {
        return testResults;
    }

    public void setTestResults(List<Artifact> testResults) {
        this.testResults = testResults;
    }

    public Artifact getSelectedArtifact() {
        return selectedArtifact;
    }

    public void setSelectedArtifact(Artifact selectedArtifact) {
        this.selectedArtifact = selectedArtifact;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
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

    public Job getJob() {
        return job;
    }

    public Project getProject() {
        return project;
    }

    public BuildGroup getBg() {
        return bg;
    }
}
