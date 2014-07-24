/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.gerrit;

import com.google.gson.Gson;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.gerrit.model.GerritDetail;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.SysConfigKey;
import com.nokia.ci.ejb.util.GerritUtils;
import com.nokia.ci.ejb.util.ReportingUtil;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.GerritQueryHandler;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.ssh.Authentication;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.ssh.SshConnection;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.ssh.SshConnectionFactory;
import com.sonyericsson.hudson.plugins.gerrit.gerritevents.ssh.SshException;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
public class GerritClient {

    // Characters (usually one space) to put before the gerrit message
    // to make gerrit treat it as pre-formatted message
    private static final String PRE_MESSAGE_CHARS = " ";
    private static final boolean GERRIT_GET_PATCHSETS = true;
    private static final boolean GERRIT_GET_CURRENT_PATCHSET = true;
    private static final boolean GERRIT_GET_FILES = true;
    private static Logger log = LoggerFactory.getLogger(GerritClient.class);
    private String gerritUrl;
    private int gerritPort;
    private String privateKeyPath;
    private String sshUserName;

    public GerritClient() {
    }

    public GerritClient(String gerritUrl, int gerritPort, String privateKeyPath,
            String sshUserName) {
        this.gerritUrl = gerritUrl;
        this.gerritPort = gerritPort;
        this.privateKeyPath = privateKeyPath;
        this.sshUserName = sshUserName;
    }

    private String sendCommandStr(String command) throws SshException, IOException {
        log.info("Sending command: {}", command);
        Authentication auth = new Authentication(new File(privateKeyPath),
                sshUserName);
        SshConnection ssh = SshConnectionFactory.getConnection(gerritUrl, gerritPort, auth);
        String str = new String(ssh.executeCommand(command).getBytes(), "UTF-8");
        log.info("Reply received: {}", str);
        ssh.disconnect();
        return str;
    }

    public boolean verify(String patchSetRevision, Long buildId, String status, String baseURL,
            String message, List<String> buildResults, List<String> buildResultDetails, List<String> buildClassificationPages, boolean abandon, Integer reviewScore, Integer verifiedScore, String gerritNewline) {

        StringBuilder command = new StringBuilder("gerrit review ");
        // Message to gerrit
        command.append("-m \"").append(PRE_MESSAGE_CHARS)
                .append("Verifier: CI 2.0 Build Robot").append(gerritNewline).append("Build: ").append(status)
                .append(gerritNewline).append("Build URL: ").append(baseURL).append("/page/build/").append(buildId);
        // Custom message part
        if (!StringUtils.isEmpty(message)) {
            command.append(gerritNewline).append(gerritNewline).append(message);
        }

        if (buildResults != null && !buildResults.isEmpty()) {
            command.append(gerritNewline).append(gerritNewline).append("Verification Results:");
            for (String result : buildResults) {
                command.append(gerritNewline).append(result);
            }
        }

        if (buildResultDetails != null && !buildResultDetails.isEmpty()) {
            command.append(gerritNewline).append(gerritNewline).append("Build Result Details:");
            for (String detail : buildResultDetails) {
                command.append(gerritNewline).append(detail);
            }
        } else {
            log.warn("No build result details found for build {}", buildId);
        }

        if (buildClassificationPages != null && !buildClassificationPages.isEmpty()) {
            command.append(gerritNewline).append(gerritNewline).append("Optional Verification Failure Classifications:");
            for (String page : buildClassificationPages) {
                command.append(gerritNewline).append(page);
            }
        }

        command.append("\"").append(" ");

        // Verified score
        if (verifiedScore != null) {
            command.append("--verified ").append(verifiedScore).append(" ");
        }
        // Code review score
        if (reviewScore != null) {
            command.append("--code-review ").append(reviewScore).append(" ");
        }
        // Abandon code change
        if (abandon) {
            command.append("--abandon").append(" ");
        }
        // PatchSet revision
        command.append(patchSetRevision);

        try {
            sendCommandStr(command.toString());
            return true;
        } catch (SshException sshex) {
            log.error("Ssh exception in verify: ", sshex);
        } catch (IOException ioe) {
            log.error("Io exception in verify: ", ioe);
        }
        return false;
    }

    public List<GerritDetail> query(String query) {

        Authentication auth = new Authentication(new File(privateKeyPath),
                sshUserName);

        GerritQueryHandler gqh = new GerritQueryHandler(gerritUrl, gerritPort,
                auth);

        List<String> items = null;
        log.info("Executing gerrit query {}", query);
        try {
            items = gqh.queryJson(query, GERRIT_GET_PATCHSETS, GERRIT_GET_CURRENT_PATCHSET, GERRIT_GET_FILES);
        } catch (SshException ex) {
            log.error("Connection ssh error: ", ex);
        } catch (IOException ex) {
            log.error("Connection io error: ", ex);
        }

        List<GerritDetail> gerritData = new ArrayList<GerritDetail>();

        if (items != null) {
            for (String item : items) {
                if (item.contains("id") && item.contains("project")) {
                    gerritData.add(GerritUtils.parseGerritDetail(item));
                }
            }
        }

        return gerritData;
    }

    public String getGerritUrl() {
        return gerritUrl;
    }

    public void setGerritUrl(String gerritUrl) {
        this.gerritUrl = gerritUrl;
    }

    public int getGerritPort() {
        return gerritPort;
    }

    public void setGerritPort(int gerritPort) {
        this.gerritPort = gerritPort;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getSshUserName() {
        return sshUserName;
    }

    public void setSshUserName(String sshUserName) {
        this.sshUserName = sshUserName;
    }
}
