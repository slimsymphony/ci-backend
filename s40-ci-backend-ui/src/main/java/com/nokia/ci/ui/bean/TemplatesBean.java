package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.TemplateEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Template;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean class for templates.
 *
 * @author jajuutin
 */
@Named
public class TemplatesBean extends DataFilterBean<Template> {

    private static Logger log = LoggerFactory.getLogger(TemplatesBean.class);
    private List<Template> templates;
    @Inject
    private TemplateEJB templateEJB;

    @Override
    protected void init() {
        templates = templateEJB.readAll();
    }

    public List<Template> getTemplates() {
        return templates;
    }

    public void delete(Template template) {
        log.info("Deleting template {}", template);
        try {
            String name = template.getName();
            templateEJB.delete(template);
            templates.remove(template);
            addMessage(FacesMessage.SEVERITY_INFO, "Operation successful.", "Template " + name + " was deleted.");
        } catch (NotFoundException ex) {
            log.warn("Deleting template {} failed! Cause: {}", template, ex.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Delete template failed!", "Selected template could not be deleted!");
        }
    }
}
