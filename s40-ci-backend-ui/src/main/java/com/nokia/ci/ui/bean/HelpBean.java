package com.nokia.ci.ui.bean;

import com.nokia.ci.ejb.CIHelpTopicEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CIHelpTopic;
import javax.enterprise.context.RequestScoped;
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
@RequestScoped
public class HelpBean extends AbstractUIBaseBean {

    private static final Logger log = LoggerFactory.getLogger(HelpBean.class);
    private CIHelpTopic helpTopic;
    @Inject
    private CIHelpTopicEJB helpTopicEJB;

    @Override
    protected void init() throws NotFoundException {
        String page = getQueryParam("page");
        try {
            Long id = Long.parseLong(page);
            log.debug("Finding help for id {}", page);
            helpTopic = helpTopicEJB.read(id);
        } catch (NumberFormatException e) {
            log.debug("Finding help for page {}!", page);
            helpTopic = helpTopicEJB.getByName(page);
        }
    }

    public CIHelpTopic getHelpTopic() {
        return helpTopic;
    }

    public void setHelpTopic(CIHelpTopic helpTopic) {
        this.helpTopic = helpTopic;
    }
}
