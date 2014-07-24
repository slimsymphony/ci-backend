/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.access;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author hhellgre
 */
public class ProjectAccessCommand implements Command {

    private static Logger log = LoggerFactory.getLogger(ProjectAccessCommand.class);
    protected static final int OK = 0;
    protected static final int WARNING = 1;
    protected static final int ERROR = 2;
    private InputStream in;
    private String command;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private List<String> response;
    private int exitValue;
    private ProjectAccessCommandFactory factory;

    public ProjectAccessCommand(String command, ProjectAccessCommandFactory factory) {
        this.factory = factory;
        this.command = command;
        response = new ArrayList<String>();
        exitValue = OK;
    }

    @Override
    public void setInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    @Override
    public void setExitCallback(ExitCallback ec) {
        this.callback = ec;
    }

    @Override
    public void start(Environment e) throws IOException {

        log.info("Starting to populate response for command: " + command);

        populateResponse();
        long projectAccessResponseDelay = factory.getProjectAccessEventResponseDelay();

        log.info("Sleeping for " + projectAccessResponseDelay + "ms...");
        long startTime = System.currentTimeMillis();
        try {
            while (startTime + projectAccessResponseDelay > System.currentTimeMillis()) {
                Thread.sleep(500);
            }
        } catch (InterruptedException e1) {
            log.info("Thread interrupted while sleep: {}", e1);
        }

        if (exitValue == OK) {
            log.info("Writing response...");
            for (String w : response) {
                w += "\n";
                out.write(w.getBytes());
                out.flush();
            }
            callback.onExit(OK);
            log.info("Done writing response");
            return;
        }

        callback.onExit(exitValue);
    }

    @Override
    public void destroy() {
    }

    private void populateResponse() {
        if (!StringUtils.isEmpty(command)) {
            listProjects();
        } else {
            exitValue = ERROR;
        }
    }

    private void listProjects() {
        if (command.equals("user")) {
            response.add("x_project");
            response.add("y_project");
        } else {
            response.add("x_project");
        }
    }

}
