package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.CIHelpTopicEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CIHelpTopic;
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
public class HelpTopicBean extends AbstractUIBaseBean {

    private static Logger log = LoggerFactory.getLogger(HelpTopicBean.class);
    private CIHelpTopic helpTopic;
    @Inject
    private CIHelpTopicEJB helpTopicEJB;
    private String pageId;

    @Override
    protected void init() throws NotFoundException {
        pageId = getQueryParam("page");
        if (pageId == null) {
            helpTopic = new CIHelpTopic();
            String pageToAdd = getQueryParam("pageToAdd");
            if (pageToAdd != null) {
                helpTopic.setPage(pageToAdd);
            }
            return;
        }
        log.debug("Finding helpTopic {} for editing!", pageId);
        helpTopic = helpTopicEJB.read(Long.parseLong(pageId));
    }

    public CIHelpTopic getHelpTopic() {
        return helpTopic;
    }

    public void setHelpTopic(CIHelpTopic helpTopic) {
        this.helpTopic = helpTopic;
    }

    public String getPageId() {
        return pageId;
    }

    public String save() {
        log.debug("Save triggered!");
        String action = null;

        try {
            if (helpTopic.getId() != null) {
                log.debug("Updating existing helpTopic {}", helpTopic);
                helpTopicEJB.update(helpTopic);
            } else {
                log.debug("Saving new helpTopic!");
                helpTopicEJB.create(helpTopic);
            }
            action = "info?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save helpTopic {}! Cause: {}", helpTopic, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Help topic could not be saved!", "");
        }

        return action;
    }

    public String cancelEdit() {
        return "info?faces-redirect=true";
    }
}
