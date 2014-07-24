/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.access;

import org.apache.commons.lang.StringUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class ProjectAccessCommandFactory extends ScpCommandFactory {

    private static Logger log = LoggerFactory.getLogger(ProjectAccessCommandFactory.class);
    private static final long PROJECT_ACCESS_EVENT_RESPONSE_DELAY = 100;
    private long projectAccessEventResponseDelay = PROJECT_ACCESS_EVENT_RESPONSE_DELAY;

    @Override
    public Command createCommand(String string) {
        if (string.contains("server")) {
            log.info("Project access server command: " + string);
            return new ServerCommand(string, this);
        } else if (!StringUtils.isEmpty(string)) {
            log.info("Project access command: " + string);
            return new ProjectAccessCommand(string, this);
        }

        return new ProcessShellFactory().create();
    }

    public long getProjectAccessEventResponseDelay() {
        return projectAccessEventResponseDelay;
    }

    public void setProjectAccessEventResponseDelay(long projectAccessEventResponseDelay) {
        this.projectAccessEventResponseDelay = projectAccessEventResponseDelay;
    }

}
