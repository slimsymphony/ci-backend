/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.gerrit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.json.JSONObject;
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
public class GerritCommand implements Command {

    private static Logger log = LoggerFactory.getLogger(GerritCommand.class);
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

    private class GerritStreamCommand extends Thread {

        @Override
        public void run() {
            Boolean streaming = true;
            try {
                while (streaming.booleanValue()) {

                    List<JSONObject> changes = gerritQuery();
                    for (JSONObject s : changes) {
                        if (s == null) {
                            continue;
                        }

                        String resp;
                        Map data = new HashMap();
                        data.put("type", "patchset-created");
                        data.put("change", s);

                        JSONObject obj = JSONObject.fromObject(data);
                        log.info("Streaming gerrit event: " + obj.toString());

                        resp = obj.toString();
                        resp += "\n";
                        out.write(resp.getBytes());
                        out.flush();
                        sleep(factory.getGerritStreamResponseDelay());
                    }
                }
            } catch (InterruptedException ex) {
                log.error("InterruptedException while streaming" + ex.getMessage());
            } catch (IOException e) {
                log.error("IOException while streaming" + e.getMessage());
            } finally {
                log.info("Done streaming");
                callback.onExit(OK);
            }
        }
    }

    public GerritCommand(String command, GerritCommandFactory factory) {
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
        if (command.contains("gerrit stream-events")) {
            GerritStreamCommand cmd = new GerritStreamCommand();
            cmd.start();
            return;
        }

        populateResponse();
        long gerritResponseDelay = factory.getGerritEventResponseDelay();

        log.info("Sleeping for " + gerritResponseDelay + "ms...");
        long startTime = System.currentTimeMillis();
        try {
            while (startTime + gerritResponseDelay > System.currentTimeMillis()) {
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
        if (command.contains("gerrit query")) {
            List<JSONObject> results = gerritQuery();
            for (JSONObject o : results) {
                response.add(o.toString());
            }
        } else if (command.contains("gerrit ls-projects")) {
            listProjects();
        } else if (command.contains("gerrit review")) {
            response.add("Reviewed! " + command);
        } else if (command.contains("gerrit version")) {
            response.add("gerrit version 2.4.2");
        } else {
            exitValue = ERROR;
        }
    }

    private void listProjects() {
        response.add("x_project");
        response.add("y_project");
    }

    private List<JSONObject> gerritQuery() {
        Map<String, String> query = getQueryValues();
        List<JSONObject> results = new ArrayList<JSONObject>();

        try {
            File responseFile = new File("s40-ci-backend-mock/data/gerrit_changes.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(responseFile);
            doc.getDocumentElement().normalize();

            NodeList changeList = doc.getElementsByTagName("change");

            for (int i = 0; i < changeList.getLength(); i++) {
                Node change = changeList.item(i);

                if (change.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) change;
                    if (matchesQuery(query, e) == false) {
                        continue;
                    }

                    results.add(generateJson(e));
                }
            }
        } catch (Exception e) {
            log.error("Could not parse gerrit changes xml");
        }

        return results;
    }

    private Boolean matchesQuery(Map<String, String> query, Element e) {
        String owner = "";
        String status = "";
        String branch = "";
        String project = "";

        if (query.containsKey("owner")) {
            NodeList own = e.getElementsByTagName("owner");
            Element o = (Element) own.item(0);
            owner = query.get("owner");
            if (!owner.equals(getTagValue("email", o))) {
                return false;
            }
        }

        if (query.containsKey("branch")) {
            branch = query.get("branch");
            if (!branch.equals(getTagValue("branch", e))) {
                return false;
            }
        }

        if (query.containsKey("status")) {
            status = query.get("status");
            String responseStatus = convertQueryStatusToResponseStatus(status);

            if (!responseStatus.equals(getTagValue("status", e))) {
                return false;
            }
        }

        if (query.containsKey(("project"))) {
            project = query.get("project");
            if (!project.equals(getTagValue("project", e))) {
                return false;
            }
        }

        return true;
    }

    private String convertQueryStatusToResponseStatus(String queryStatus) {
        if (queryStatus.equals("open")) {
            return "NEW";
        } else if (queryStatus.equals("merged")) {
            return "MERGED";
        } else if (queryStatus.equals("abandoned")) {
            return "ABANDONED";
        }

        return "SUBMITTED";
    }

    private JSONObject generateJson(Element e) {
        SecureRandom random = new SecureRandom();
        Map data = new HashMap();
        data.put("project", getTagValue("project", e));
        data.put("branch", getTagValue("branch", e));
        data.put("topic", getTagValue("topic", e));
        data.put("id", new BigInteger(130, random).toString(32));
        data.put("subject", getTagValue("subject", e));
        data.put("status", getTagValue("status", e));
        data.put("url", getTagValue("url", e));
        data.put("lastUpdated", System.currentTimeMillis() / 1000);

        NodeList own = e.getElementsByTagName("owner");
        Element o = (Element) own.item(0);

        Map owner = new HashMap();
        owner.put("name", getTagValue("name", o));
        owner.put("email", getTagValue("email", o));
        data.put("owner", owner);

        NodeList ps = e.getElementsByTagName("currentpatchset");
        Element p = (Element) ps.item(0);

        Map patchset = new HashMap();
        patchset.put("number", new BigInteger(3, random).toString(2));
        patchset.put("revision", new BigInteger(130, random).toString(32));
        patchset.put("ref", getTagValue("ref", p) + new BigInteger(3, random).toString(2));

        NodeList upl = p.getElementsByTagName("uploader");
        Element u = (Element) upl.item(0);

        Map uploader = new HashMap();
        uploader.put("name", getTagValue("name", u));
        uploader.put("email", getTagValue("email", u));
        patchset.put("uploader", uploader);

        NodeList fs = p.getElementsByTagName("files");
        List<GerritFile> files = new ArrayList<GerritFile>();
        for (int i = 0; i < fs.getLength(); i++) {
            Element f = (Element) fs.item(i);
            GerritFile file = new GerritFile();
            file.setFile(getTagValue("file", f));
            file.setType(getTagValue("type", f));
            files.add(file);
        }
        patchset.put("files", files);

        NodeList parents = p.getElementsByTagName("parents");
        if (parents != null && parents.getLength() > 0) {
            NodeList values = ((Element) parents.item(0)).getElementsByTagName("value");
            List<String> parentsArray = new ArrayList<String>();
            if (values != null) {
                for (int i = 0; i < values.getLength(); i++) {
                    NodeList v = (NodeList) values.item(i).getChildNodes();
                    parentsArray.add(((Node) v.item(0)).getNodeValue());
                }
                patchset.put("parents", parentsArray);
            }
        }

        data.put("currentPatchSet", patchset);

        JSONObject obj = JSONObject.fromObject(data);
        return obj;
    }

    private Map<String, String> getQueryValues() {
        Map queryValues = new HashMap();
        command = command.replace("\"", "");
        String[] val = command.split(" ");
        for (int i = 0; i < val.length; i++) {
            String items[] = val[i].split(":");
            if (items.length == 2) {
                queryValues.put(items[0], items[1]);
            }

        }
        return queryValues;
    }

    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = (Node) nlList.item(0);
        return nValue.getNodeValue();
    }
}
