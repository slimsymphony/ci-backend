/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.gerrit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class ServerCommand implements Command {

    private static Logger log = LoggerFactory.getLogger(ServerCommand.class);
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
    private GerritCommandFactory factory;

    public ServerCommand(String command, GerritCommandFactory factory) {
        this.command = command;
        this.factory = factory;
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

        populateResponse();

        if (exitValue == OK) {
            for (String w : response) {
                w += "\n";
                out.write(w.getBytes());
                out.flush();
            }
            callback.onExit(OK);
            return;
        } else if (exitValue == ERROR) {
            for (String w : response) {
                w += "\n";
                err.write(w.getBytes());
                err.flush();
            }
            callback.onExit(ERROR);
            return;
        }

        callback.onExit(exitValue);
    }

    @Override
    public void destroy() {
    }

    private void populateResponse() {
        if (command.contains("server event delay")) {
            try {
                factory.setGerritEventResponseDelay(Long.parseLong(command.replace("server event delay", "").trim()));
                response.add("Server event delay set to " + factory.getGerritEventResponseDelay());
            } catch (NumberFormatException e) {
                exitValue = ERROR;
                response.add("Error parsing server event delay value " + command.replace("server event delay", "").trim());
                log.error("Error parsing server event delay value: {}", e);
            }
        } else if (command.contains("server stream delay")) {
            try {
                factory.setGerritStreamResponseDelay(Long.parseLong(command.replace("server stream delay", "").trim()));
                response.add("Server stream delay set to " + factory.getGerritStreamResponseDelay());
            } catch (NumberFormatException e) {
                exitValue = ERROR;
                response.add("Error parsing server stream delay value " + command.replace("server stream delay", "").trim());
                log.error("Error parsing server stream delay value: {}", e);
            }
        } else {
            exitValue = ERROR;
            response.add("Could not parse server command: " + command);
            log.error("Could not parse server command: " + command);
        }
    }
}