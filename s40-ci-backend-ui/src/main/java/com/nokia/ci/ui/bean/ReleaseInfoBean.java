package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.CIReleaseInfoEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CIReleaseInfo;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@ViewScoped
public class ReleaseInfoBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(ReleaseInfoBean.class);
    private CIReleaseInfo releaseInfo;
    @Inject
    private CIReleaseInfoEJB releaseInfoEJB;

    @Override
    protected void init() throws NotFoundException {
        String releaseId = getQueryParam("release");
        if (releaseId == null) {
            releaseInfo = new CIReleaseInfo();
            return;
        }
        log.debug("Finding releaseInfo {} for editing!", releaseId);
        releaseInfo = releaseInfoEJB.read(Long.parseLong(releaseId));
    }

    public CIReleaseInfo getReleaseInfo() {
        return releaseInfo;
    }

    public void setReleaseInfo(CIReleaseInfo releaseInfo) {
        this.releaseInfo = releaseInfo;
    }

    public String save() {
        log.debug("Save triggered!");

        try {
            if (releaseInfo.getId() != null) {
                log.debug("Updating existing hreleaseInfo {}", releaseInfo);
                releaseInfoEJB.update(releaseInfo);
            } else {
                log.debug("Saving new releaseInfo!");
                releaseInfoEJB.create(releaseInfo);
            }
            return "info?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save releaseInfo {}! Cause: {}", releaseInfo, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Release info could not be saved!", "");
        }

        return null;
    }

    public String cancelEdit() {
        return "info?faces-redirect=true";
    }
}
