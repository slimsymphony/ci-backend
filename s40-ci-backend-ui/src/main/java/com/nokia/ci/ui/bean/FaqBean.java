/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.CIFaqEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CIFaq;
import javax.annotation.security.RolesAllowed;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Named
@ViewScoped
public class FaqBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(FaqBean.class);
    @Inject
    private CIFaqEJB ciFaqEJB;
    private CIFaq faq;
    private String id;

    @Override
    protected void init() throws NotFoundException {
        id = getQueryParam("id");
        if (StringUtils.isEmpty(id)) {
            log.debug("Creating new faq");
            faq = new CIFaq();
        } else {
            log.debug("Finding faq for id {}!", id);
            Long did = Long.parseLong(id);
            faq = ciFaqEJB.read(did);
        }
    }

    public CIFaq getFaq() {
        return faq;
    }

    public void setFaq(CIFaq faq) {
        this.faq = faq;
    }

    public String getId() {
        return id;
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public String save() {
        log.debug("Save triggered!");
        String action = null;

        try {
            if (faq.getId() != null) {
                log.debug("Updating existing faq {}", faq);
                ciFaqEJB.update(faq);
            } else {
                log.debug("Saving new faq!");
                ciFaqEJB.create(faq);
            }
            action = "info?faces-redirect=true";
        } catch (NotFoundException nfe) {
            log.warn("Can not save faq {}! Cause: {}", faq, nfe.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "FAQ could not be saved!", "");
        }

        return action;
    }

    @RolesAllowed("SYSTEM_ADMIN")
    public String cancelEdit() {
        return "info?faces-redirect=true";
    }
}
