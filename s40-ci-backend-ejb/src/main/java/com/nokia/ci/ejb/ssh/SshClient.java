/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.ssh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hhellgre
 */
public class SshClient {

    private static Logger log = LoggerFactory.getLogger(SshClient.class);
    private static int DEFAULT_SESSION_TIMEOUT = 15000;
    private String sshUrl;
    private int sshPort;
    private String sshUsername;
    private String sshPassword;
    private String httpProxyUrl;
    private int httpProxyPort;
    private File privateKey;
    private JSch jsch = null;
    private Session session = null;
    private int sessionTimeout = DEFAULT_SESSION_TIMEOUT;

    public SshClient() {
    }

    public SshClient(String url, int port, String username, File privateKey) {
        this.sshUrl = url;
        this.sshPort = port;
        this.sshUsername = username;
        this.privateKey = privateKey;
    }

    public SshClient(String url, int port, String username, String password) {
        this.sshUrl = url;
        this.sshPort = port;
        this.sshUsername = username;
        this.sshPassword = password;
    }

    public void setHttpProxy(String proxyUrl, int proxyPort) {
        this.httpProxyUrl = proxyUrl;
        this.httpProxyPort = proxyPort;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    private void connect() throws JSchException {
        jsch = new JSch();
        session = getSession();
        session.connect();
    }

    private Boolean disconnect() {
        if (session != null) {
            session.disconnect();
            return true;
        }
        return false;
    }

    public String execute(String command) {
        StringBuilder sb = new StringBuilder();
        log.info("Executing SSH command {} to {}", command, sshUrl);

        Channel channel = null;
        try {
            if (jsch == null || session == null || !session.isConnected()) {
                connect();
            }
            channel = session.openChannel("exec");

            ((ChannelExec) channel).setCommand(command);
            channel.connect();

            InputStream in = channel.getInputStream();
            byte[] tmp = new byte[1024];

            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) {
                        break;
                    }
                    sb.append(new String(tmp, 0, i));
                }

                if (channel.isClosed()) {
                    break;
                }
            }
        } catch (IOException e) {
            log.error("IOException while executing SSH command {}, {}", command, e.getMessage());
            log.error("IO Exception: {}", e);
        } catch (JSchException ex) {
            log.error("Error in JSch SSH connection while executing command {}, {}", command, ex.getMessage());
            log.error("JSch Exception: {}", ex);
        } finally {
            if (channel != null) {
                channel.disconnect();

            }
            disconnect();
        }

        return sb.toString();
    }

    private Session getSession() throws JSchException {
        Session session = null;

        jsch.setKnownHosts(sshUrl);
        if (privateKey != null) {
            // Use private key authentication
            jsch.addIdentity(privateKey.getAbsolutePath());
            session = jsch.getSession(sshUsername, sshUrl, sshPort);
        } else {
            // Use password authentication
            UserInfo userInfo = new UserInfo() {
                public String getPassphrase() {
                    return (null);
                }

                public String getPassword() {
                    return (sshPassword);
                }

                public boolean promptPassword(String prompt) {
                    return (true);
                }

                public boolean promptPassphrase(String prompt) {
                    return (false);
                }

                public boolean promptYesNo(String prompt) {
                    return (false);
                }

                public void showMessage(String string) {
                    return;
                }
            };

            session = jsch.getSession(sshUsername, sshUrl, sshPort);
            session.setUserInfo(userInfo);
        }

        session.setConfig("StrictHostKeyChecking", "no");
        session.setTimeout(sessionTimeout);

        if (httpProxyUrl != null) {
            session.setProxy(new ProxyHTTP(httpProxyUrl, httpProxyPort));
        }

        return (session);
    }
}
