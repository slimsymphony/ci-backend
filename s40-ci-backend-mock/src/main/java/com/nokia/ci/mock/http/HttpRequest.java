/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.parse.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class HttpRequest implements Runnable {

    private static Logger log = LoggerFactory.getLogger(HttpRequest.class);
    private final Response response;
    private final Request request;
    private final HttpServer server;
    private Status curStatus;
    private String responseContent;

    public HttpRequest(HttpServer server, Request request, Response response) {
        this.response = response;
        this.request = request;
        this.server = server;
        responseContent = "";
        curStatus = Status.OK;
    }

    private void populateResponse(Response response) {
        long time = System.currentTimeMillis();
        response.set("Content-Type", "text/plain");
        response.set("Server", "Http Mock server");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
        response.setCode(curStatus.getCode());

        PrintStream body = null;
        try {
            body = response.getPrintStream();
            body.println(responseContent);
        } catch (IOException ex) {
        } finally {
            if (body != null) {
                body.close();
            }
        }
    }

    private void handleRequest(List<String> pathParams, Map<String, String> params) {

        if(pathParams.isEmpty()) {
            log.info("No path set, returning");
            return;
        }
        
        log.info("Reading file from s40-ci-backend-mock/data/" + pathParams.get(pathParams.size() - 1));
        
        File responseFile = new File("s40-ci-backend-mock/data/" + pathParams.get(pathParams.size() - 1));
        
        if(!responseFile.exists()) {
            log.info("-- 404 - File not found.");
            responseContent = "404 - File not found";
            populateResponse(response);
            return;
        }
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(responseFile.getAbsoluteFile()));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = br.readLine();
                }
                responseContent = sb.toString();
            } finally {
                br.close();
            }
        } catch (FileNotFoundException e) {
            log.error("Could not find file " + responseFile.getAbsoluteFile());
        } catch(IOException ex) {
            
        } finally {
            populateResponse(response);
        }
    }

    @Override
    public void run() {
        synchronized (server) {
            Query query = request.getQuery();
            QueryParser parser = new QueryParser(query.toString());
            String path = request.getPath().getPath();
            if (path.equals("/favicon.ico")) {
                return;
            }

            log.info("Handling request to path: " + path + "with query: "
                    + query.toString());

            List<String> pathParams = Arrays.asList(path.split("/"));
            handleRequest(pathParams, parser);
        }
    }
}
