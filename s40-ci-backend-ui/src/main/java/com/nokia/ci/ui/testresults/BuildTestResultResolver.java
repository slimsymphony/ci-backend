/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.testresults;

import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ui.jenkins.Artifact;
import com.nokia.ci.ui.jenkins.BuildDetailResolver;
import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class BuildTestResultResolver implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(BuildTestResultResolver.class);
    private static final String TEST_RESULT_FOLDER = "test_results/";
    static final int TIMEOUT = 7 * 1000;
    private int socketTimeout = TIMEOUT;
    private int connectionTimeout = TIMEOUT;
    private AbstractBuildArtifactResolver resolver;

    public BuildTestResultResolver(int socketTimeout, int connectionTimeout) {
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
    }

    public List<Artifact> fetchTestResults(BuildGroup bg, Build build) {
        List<Artifact> list = new ArrayList<Artifact>();

        if (bg == null || bg.getBuildGroupCIServer() == null || build == null) {
            return list;
        }

        String testResultStorage = bg.getBuildGroupCIServer().getTestResultStorage();

        if (testResultStorage == null) {
            return list;
        }

        if (!testResultStorage.endsWith("/")) {
            testResultStorage += "/";
        }

        // If testing this on localhost, remove comments from the next line:
        // String storage = testResultStorage + "test_results/";
        String storage = testResultStorage + bg.getId() + "/" + build.getId() + "/" + TEST_RESULT_FOLDER;

        // First try to go through the filesystem for test results
        boolean found = false;
        File stor = new File(storage);
        if (stor.exists() && stor.isDirectory()) {
            Collection<File> files = FileUtils.listFiles(stor, null, true);
            found = !files.isEmpty();

            for (File f : files) {
                String filePath = f.getAbsolutePath().toString();
                String name = filePath;
                URL url = null;
                int last = filePath.lastIndexOf("/") + 1;
                int test_results = filePath.lastIndexOf(TEST_RESULT_FOLDER);

                if (last >= 0) {
                    name = filePath.substring(last);
                }

                if (test_results >= 0 && StringUtils.isNotEmpty(build.getUrl())) {
                    try {
                        url = new URL(build.getUrl() + "/artifact/" + filePath.substring(test_results));
                    } catch (MalformedURLException ex) {
                        log.debug("Could not create url from " + build.getUrl() + filePath.substring(test_results));
                    }
                }

                Artifact a = new Artifact(name, url, filePath);
                list.add(a);
            }
        }

        // Jenkins/proxy fallback
        if (!found && StringUtils.isNotEmpty(build.getUrl())) {
            if (StringUtils.isEmpty(bg.getBuildGroupCIServer().getProxyServerUrl())) {
                resolver = new BuildDetailResolver(build.getUrl(), socketTimeout, connectionTimeout);
            } else {
                StringBuilder artifactUrl = new StringBuilder(bg.getBuildGroupCIServer().getProxyServerUrl());
                artifactUrl.append("/").append(bg.getId()).append("/").append(build.getId());
                resolver = new ProxyArtifactResolver(artifactUrl.toString(), socketTimeout, connectionTimeout);
            }
            list = resolver.fetchTestResults();
            if (list == null) {
                log.warn("Failed to fetch test results from URL: {}", resolver.getUrl());
                return new ArrayList<Artifact>();
            }
            for (Artifact a : list) {
                String path = a.getArtifactPath();
                if (StringUtils.isNotEmpty(path) && path.startsWith(TEST_RESULT_FOLDER)) {
                    path = path.replaceFirst(TEST_RESULT_FOLDER, "");
                }
                a.setArtifactPath(storage + path);
            }
        }

        return list;
    }
}
