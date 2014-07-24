/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.gerrit;

import org.apache.sshd.server.Command;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class GerritCommandFactory extends ScpCommandFactory {

    private static Logger log = LoggerFactory.getLogger(GerritCommandFactory.class);
    private static final long GERRIT_EVENT_RESPONSE_DELAY = 100;
    private static final long GERRIT_STREAM_RESPONSE_DELAY = 30 * 1000;
    private long gerritEventResponseDelay = GERRIT_EVENT_RESPONSE_DELAY;
    private long gerritStreamResponseDelay = GERRIT_STREAM_RESPONSE_DELAY;
    private Boolean respond = true;

    @Override
    public Command createCommand(String string) {
        if (string.contains("gerrit")) {
            log.info("Gerrit command: " + string);
            return new GerritCommand(string, this);
        } else if (string.contains("server")) {
            log.info("Gerrit command: " + string);
            return new ServerCommand(string, this);
        }

        return new ProcessShellFactory().create();
    }

    public long getGerritEventResponseDelay() {
        return gerritEventResponseDelay;
    }

    public void setGerritEventResponseDelay(long gerritEventResponseDelay) {
        this.gerritEventResponseDelay = gerritEventResponseDelay;
    }

    public long getGerritStreamResponseDelay() {
        return gerritStreamResponseDelay;
    }

    public void setGerritStreamResponseDelay(long gerritStreamResponseDelay) {
        this.gerritStreamResponseDelay = gerritStreamResponseDelay;
    }
}
