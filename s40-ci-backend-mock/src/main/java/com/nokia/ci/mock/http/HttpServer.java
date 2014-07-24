/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class HttpServer extends Thread implements Container {

    private static Logger log = LoggerFactory.getLogger(HttpServer.class);
    
    private Stack<HttpRequest> serverQueue;
    private AtomicBoolean serverOn;
    private Connection connection;
    private SocketAddress address;
    private int port;

    public HttpServer() {
        init(1339);
    }
    
    public HttpServer(int port) {
        init(port);
    }
    
    private void init(int port) {
        log.info("Starting HTTP Mock server to port " + port);
        serverOn = new AtomicBoolean(true);
        serverQueue = new Stack<HttpRequest>();
        this.port = port;
    }

    @Override
    public void handle(Request request, Response response) {
        serverQueue.push(new HttpRequest(this, request, response));
    }

    @Override
    public void run() {
        try {
            connection = new SocketConnection(this);
            address = new InetSocketAddress(port);
            connection.connect(address);

            while (serverOn.get()) {
                if (!serverQueue.isEmpty()) {
                    serverQueue.pop().run();
                }
                sleep(500);
            }
        } catch(IOException e) {
            log.error("JenkinsServer IOException: " + e.getMessage());
        } catch (InterruptedException e) {
            log.error("JenkinsServer InterruptedException: " + e.getMessage());
        }
    }

    public void shutDown() {
        serverOn.set(false);
    }
}
