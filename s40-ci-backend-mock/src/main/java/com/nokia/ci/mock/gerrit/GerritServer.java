/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.mock.gerrit;

import java.io.IOException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.cipher.AES128CBC;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class GerritServer extends Thread {

    private static Logger log = LoggerFactory.getLogger(GerritServer.class);
    private AtomicBoolean serverOn;
    private SshServer sshd;

    public GerritServer() {
        init(1338);
    }

    public GerritServer(int port) {
        init(port);
    }

    private void init(int port) {
        log.info("Starting Gerrit Mock server to port " + port);
        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);

        GerritCommandFactory factory = new GerritCommandFactory();
        sshd.setCommandFactory(factory);

        PublickeyAuthenticator auth = new PublickeyAuthenticator() {
            @Override
            public boolean authenticate(String username, PublicKey pk, ServerSession ss) {
                log.info("Authenticating user " + username);
                if (username.equals("account")) {
                    return true;
                }

                return false;
            }
        };

        PasswordAuthenticator pwAuth = new PasswordAuthenticator() {
            @Override
            public boolean authenticate(String username, String password, ServerSession ss) {
                log.info("Authenticating user " + username);
                if (username.equals(password)) {
                    return true;
                }
                return false;
            }
        };

        sshd.setPublickeyAuthenticator(auth);
        sshd.setPasswordAuthenticator(pwAuth);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("s40-ci-backend-mock/keys/hostkey.ser"));

        sshd.setNioWorkers(1);
        NamedFactory<org.apache.sshd.common.Cipher> cipher = new AES128CBC.Factory();
        sshd.setCipherFactories(Arrays.<NamedFactory<org.apache.sshd.common.Cipher>>asList(cipher));

        serverOn = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        try {
            sshd.start();

        } catch (IOException e) {
            log.error("IOException when starting gerrit mock server: {}", e.getMessage());
            serverOn.set(false);
        }

        while (serverOn.get()) {
            try {
                sleep(500);
            } catch (Exception e) {
            }
        }
    }
}
