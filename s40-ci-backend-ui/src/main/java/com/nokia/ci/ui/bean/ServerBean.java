package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.CIServerEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CIServer;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for verification.
 *
 * @author vrouvine
 */
@Named
@ViewScoped
public class ServerBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(VerificationBean.class);
    private CIServer server;
    @Inject
    private CIServerEJB serverEJB;

    @Override
    protected void init() throws NotFoundException {
        String serverId = getQueryParam("serverId");
        if (serverId == null) {
            server = new CIServer();
            server.setPort(8080);
            return;
        }
        log.debug("Finding server {} for editing!", serverId);
        server = serverEJB.read(Long.parseLong(serverId));
    }

    public CIServer getServer() {
        return server;
    }

    public void setServer(CIServer server) {
        this.server = server;
    }

    public String save() {
        log.debug("Save triggered!");

        try {
            if (server.getId() != null) {
                log.debug("Updating existing server {}", server);
                serverEJB.update(server);
            } else {
                log.debug("Saving new server!");
                serverEJB.create(server);
            }
            return "servers?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save server {}! Cause: {}", server, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Server could not be saved!", "");
        }

        return null;
    }

    public String cancelEdit() {
        return "servers?faces-redirect=true";
    }
}
