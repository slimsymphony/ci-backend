package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.CIFaqEJB;
import com.nokia.ci.ejb.CIHelpTopicEJB;
import com.nokia.ci.ejb.CIReleaseInfoEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CIFaq;
import com.nokia.ci.ejb.model.CIHelpTopic;
import com.nokia.ci.ejb.model.CIReleaseInfo;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@ViewScoped
public class InfoBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(InfoBean.class);
    @Inject
    CIHelpTopicEJB helpTopicEJB;
    @Inject
    CIReleaseInfoEJB releaseInfoEJB;
    @Inject
    CIFaqEJB ciFaqEJB;
    private List<CIHelpTopic> helpTopics;
    private List<CIReleaseInfo> releaseInfo;
    private List<CIFaq> faqs;

    @Override
    protected void init() {
        initReleaseInfo();
        initHelpTopics();
        initFaqs();
    }

    private void initHelpTopics() {
        helpTopics = helpTopicEJB.readAllSortByTopic();
    }

    private void initReleaseInfo() {
        releaseInfo = releaseInfoEJB.readAll();
    }

    private void initFaqs() {
        faqs = ciFaqEJB.readAll();
    }

    public List<CIHelpTopic> getHelpTopics() {
        return helpTopics;
    }

    public void setHelpTopics(List<CIHelpTopic> helpTopics) {
        this.helpTopics = helpTopics;
    }

    public List<CIReleaseInfo> getReleaseInfo() {
        return releaseInfo;
    }

    public void setReleaseInfo(List<CIReleaseInfo> releaseInfo) {
        this.releaseInfo = releaseInfo;
    }

    public List<CIFaq> getFaqs() {
        return faqs;
    }

    public void setFaqs(List<CIFaq> faqs) {
        this.faqs = faqs;
    }

    public Boolean hasHelpTopic(String page) {
        for (CIHelpTopic t : helpTopics) {
            if (StringUtils.isEmpty(t.getPage())) {
                continue;
            }

            if (t.getPage().equals(page)) {
                return true;
            }
        }

        return false;
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public void delete(CIFaq faq) {
        log.info("Deleting faq {}", faq);
        try {
            Long id = faq.getId();
            ciFaqEJB.delete(faq);
            faqs.remove(faq);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "FAQ was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting FAQ {} failed! Cause: {}", faq, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete FAQ failed!", "Selected FAQ could not be deleted!");
        }
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public void delete(CIHelpTopic helpTopic) {
        log.info("Deleting help topic {}", helpTopic);
        try {
            Long id = helpTopic.getId();
            helpTopicEJB.delete(helpTopic);
            helpTopics.remove(helpTopic);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Help topic was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting Help topic {} failed! Cause: {}", helpTopic, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete help topic failed!", "Selected help topic could not be deleted!");
        }
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public void delete(CIReleaseInfo ri) {
        log.info("Deleting releaseInfo {}", ri);
        try {
            releaseInfoEJB.delete(ri);
            releaseInfo.remove(ri);
            addMessage(FacesMessage.SEVERITY_INFO,
                    "Operation successful.", "Release info was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting release info {} failed! Cause: {}", ri, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Delete release info failed!", "Selected release info could not be deleted!");
        }
    }
}
