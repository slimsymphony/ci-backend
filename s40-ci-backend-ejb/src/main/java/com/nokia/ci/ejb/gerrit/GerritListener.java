/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.gerrit;

import com.jcraft.jsch.*;
import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.SysConfigKey;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
@TransactionAttribute(value = TransactionAttributeType.NOT_SUPPORTED)
public class GerritListener {

    private static Logger log = LoggerFactory.getLogger(GerritListener.class);
    @Inject
    private GerritEventProducer producer;
    @EJB
    private GerritEJB gerritEJB;
    @EJB
    private SysConfigEJB sysConfigEJB;
    // Timeout for checking if we still need to listen this gerrit (in seconds)
    private static final int DEFAULT_GERRIT_LISTENER_CHECK_TIMEOUT = 10;
    // Interval when checking events (ms)
    private static final int DEFAULT_GERRIT_LISTENER_LOOP_INTERVAL = 1000;
    // Timeout to disconnect if nothing is received (minutes)
    private static final int DEFAULT_GERRIT_NORESPONSE_TIMEOUT = 240;
    // Max number of server alive messages to be sent before disconnect
    private static final int DEFAULT_GERRIT_SERVER_ALIVE_COUNT_MAX = 0;
    // Socket session timeout (ms)
    private static final int DEFAULT_GERRIT_SOCKET_TIMEOUT = 120 * 60 * 1000;

    private int gerritListenerCheckTimeout;
    private int gerritListenerLoopInterval;
    private int gerritNoResponceTimeout;
    private int gerritServerAliveCountMax;
    private int gerritSocketTimeout;
    
    /**
     *
     * @param gerritId
     * @return Returns future boolean value when done false on error while
     * starting the listener true on intended stop of the listener
     */
    @Asynchronous
    public Future<Boolean> listen(Long gerritId) {

        Gerrit gerrit;
        try {
            gerrit = gerritEJB.read(gerritId);
        } catch (NotFoundException ex) {
            log.error("Could not find gerrit with id {}, stopping stream listener", gerritId);
            return new AsyncResult<Boolean>(false);
        }

        Boolean returnValue = true;
        String gerritHostName = gerrit.getUrl();
        int gerritSshPort = gerrit.getPort();
        String username = gerrit.getSshUserName();
        String privateKey = gerrit.getPrivateKeyPath();

        Channel channel = null;
        Session session = null;
        String line = null;
        Long gerritListenCheckTimer = 0L;
        Long gerritNoResponseTimer = 0L;

        JSch client = new JSch();
        JSch.setConfig("StrictHostKeyChecking", "no");

        try {
            // Startup connection
            client.setKnownHosts(gerritHostName);
            if (privateKey != null) {
                client.addIdentity(privateKey);
            }

            // Configure gerrit listener session
            gerritListenerCheckTimeout = sysConfigEJB.getValue(SysConfigKey.GERRIT_LISTENER_CHECK_TIMEOUT, DEFAULT_GERRIT_LISTENER_CHECK_TIMEOUT);
            gerritListenerLoopInterval = sysConfigEJB.getValue(SysConfigKey.GERRIT_LISTENER_LOOP_INTERVAL, DEFAULT_GERRIT_LISTENER_LOOP_INTERVAL);
            gerritNoResponceTimeout = sysConfigEJB.getValue(SysConfigKey.GERRIT_NORESPONSE_TIMEOUT, DEFAULT_GERRIT_NORESPONSE_TIMEOUT);
            gerritServerAliveCountMax = sysConfigEJB.getValue(SysConfigKey.GERRIT_SERVER_ALIVE_COUNT_MAX, DEFAULT_GERRIT_SERVER_ALIVE_COUNT_MAX);
            gerritSocketTimeout = sysConfigEJB.getValue(SysConfigKey.GERRIT_SOCKET_TIMEOUT, DEFAULT_GERRIT_SOCKET_TIMEOUT);
            
            session = client.getSession(username, gerritHostName, gerritSshPort);
            session.setTimeout(gerritSocketTimeout);
            session.setServerAliveCountMax(gerritServerAliveCountMax);
            session.connect();

            channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand("gerrit stream-events");

            InputStream in = channel.getInputStream();
            channel.connect();

            double second_multiplier = gerritListenerLoopInterval / 1000;

            while(true) {
                // Check for connection aliveness
                if (!session.isConnected()) {
                    log.error("Gerrit {} listener is no more connected, stopping stream listener", gerrit);
                    returnValue = false;
                    break;
                }

                // Check for no response timeout, fallback mechanism
                if ((gerritNoResponseTimer * second_multiplier / 60) >= gerritNoResponceTimeout) {
                    log.info("No responses from gerrit {} in {} minutes, stopping stream listener",
                            gerrit, gerritNoResponceTimeout);
                    returnValue = false;
                    break;
                }

                // Check for gerrit data in database
                if ((gerritListenCheckTimer * second_multiplier) >= gerritListenerCheckTimeout) {
                    try {
                        gerrit = gerritEJB.read(gerritId);
                    } catch (NotFoundException ex) {
                        log.error("Could not find gerrit with id {}, stopping stream listener", gerritId);
                        returnValue = false;
                        break;
                    }

                    if (gerrit.getListenStream() == null || gerrit.getListenStream() == false) {
                        log.info("Gerrit listener for gerrit {} is disabled, stopping stream listener", gerrit);
                        break;
                    }
                    gerritListenCheckTimer = 0L;
                }

                // Read all new lines that are in stream
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                if (br.ready()) {
                    do {
                        // TODO: Do we need to read this line-by-line in order to prevent
                        //       data with no EOL not to break our great system?
                        line = br.readLine();
                        
                        // Queue new data
                        if (line != null && line.length() > 0) {
                            line = new String(line.getBytes(), "UTF-8");
                            log.info("Got data from gerrit {} " + line, gerrit);
                            producer.queueEvent(line);
                        }
                    } while (line != null);

                    gerritNoResponseTimer = 0L;
                }

                Thread.sleep(gerritListenerLoopInterval);
                gerritListenCheckTimer++;
                gerritNoResponseTimer++;

            }
        } catch (JSchException e) {
            log.error("Error opening stream channel for gerrit {}", gerrit);
            returnValue = false;
        } catch (IOException e) {
            log.error("Could not open input stream for gerrit {} stream channel", gerrit);
            returnValue = false;
        } catch (InterruptedException e) {
            log.error("Error while running gerrit {} stream listener, shutting down", gerrit);
            returnValue = false;
        } finally {
            // Clean up
            log.info("Gerrit {} listener done, disconnecting", gerrit);

            if (session != null) {
                session.disconnect();
            }

            if (channel != null) {
                channel.disconnect();
            }
        }

        return new AsyncResult<Boolean>(returnValue);
    }
}