package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.CIServerEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.CIServer;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for servers.
 *
 * @author jajuutin
 */
@Named
public class ServersBean extends DataFilterBean<CIServer> {

    private static Logger log = LoggerFactory.getLogger(ServersBean.class);
    private List<CIServer> servers;
    @Inject
    private CIServerEJB serverEJB;

    @Override
    protected void init() {
        initServers();
    }

    public List<CIServer> getServers() {
        return servers;
    }

    public void delete(CIServer server) {
        log.info("Deleting server {}", server);

        List<Branch> branches = serverEJB.getBranches(server.getId());
        if (branches != null && !branches.isEmpty()) {
            log.warn("Deleting server {} failed! Cause: server is still included in some branch.", server);
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete server failed!", "Selected server could not be deleted! Please check that the server is not included in any branch before deleting.");
            return;
        }

        try {
            String url = server.getUrl();
            serverEJB.delete(server);
            servers.remove(server);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Server " + url + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting server {} failed! Cause: {}", server, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete server failed!", "Selected server could not be deleted!");
        }
    }

    private void initServers() {
        servers = serverEJB.readAll();
    }
}
