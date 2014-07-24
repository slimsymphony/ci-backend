package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Gerrit;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for gerrit.
 *
 * @author jajuutin
 */
@Named
@ViewScoped
public class GerritBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(GerritBean.class);
    private Gerrit gerrit;
    @Inject
    private GerritEJB gerritEJB;
    private String gerritId;

    @Override
    protected void init() throws NotFoundException {
        gerritId = getQueryParam("gerritId");
        if (gerritId == null) {
            gerrit = new Gerrit();
            gerrit.setListenStream(true);
            gerrit.setPort(29418);
            return;
        }
        log.debug("Finding gerrit {} for editing!", gerritId);
        gerrit = gerritEJB.read(Long.parseLong(gerritId));

        if (gerrit.getListenStream() == null) {
            gerrit.setListenStream(false);
        }
    }

    public Gerrit getGerrit() {
        return gerrit;
    }

    public void setGerrit(Gerrit gerrit) {
        this.gerrit = gerrit;
    }

    public String getGerritId() {
        return gerritId;
    }

    public String save() {
        log.debug("Save triggered!");

        String action = null;

        try {
            if (gerrit.getId() != null) {
                log.debug("Updating existing gerrit {}", gerrit);
                gerritEJB.update(gerrit);
            } else {
                log.debug("Saving new gerrit!");
                gerritEJB.create(gerrit);
            }
            action = "gerrits?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save gerrit {}! Cause: {}", gerrit, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Gerrit could not be saved!", "");
        }

        return action;
    }

    public String cancelEdit() {
        return "gerrits?faces-redirect=true";
    }
}
