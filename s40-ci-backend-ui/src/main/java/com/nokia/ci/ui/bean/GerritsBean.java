package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.GerritEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Project;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for gerrits.
 *
 * @author jajuutin
 */
@Named
public class GerritsBean extends DataFilterBean<Gerrit> {

    private static Logger log = LoggerFactory.getLogger(GerritsBean.class);
    private List<Gerrit> gerrits;
    @Inject
    private GerritEJB gerritEJB;

    @Override
    protected void init() {
        initGerrits();
    }

    private void initGerrits() {
        gerrits = gerritEJB.readAll();
    }

    public List<Gerrit> getGerrits() {
        return gerrits;
    }

    public void delete(Gerrit gerrit) {
        log.info("Deleting gerrit {}", gerrit);

        List<Project> projects = gerritEJB.getProjects(gerrit.getId());
        if (projects != null && !projects.isEmpty()) {
            log.warn("Deleting gerrit {} failed! Cause: gerrit is still included in some project.", gerrit);
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete gerrit failed!", "Selected gerrit could not be deleted! Please check that the gerrit is not included in any project before deleting.");
            return;
        }

        try {
            String url = gerrit.getUrl();
            gerritEJB.delete(gerrit);
            gerrits.remove(gerrit);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Gerrit " + url + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting gerrit {} failed! Cause: {}", gerrit, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete gerrit failed!", "Selected gerrit could not be deleted!");
        }
    }
}
