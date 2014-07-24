package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.TemplateCustomVerification;
import com.nokia.ci.ejb.model.TemplateCustomVerificationConf;
import com.nokia.ci.ejb.model.TemplateCustomVerificationParam;
import com.nokia.ci.ejb.model.TemplateCustomVerification_;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link TemplateCustomVerification} object
 * operations.
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class TemplateCustomVerificationEJB extends CrudFunctionality<TemplateCustomVerification> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(TemplateCustomVerificationEJB.class);

    public TemplateCustomVerificationEJB() {
        super(TemplateCustomVerification.class);
    }

    public List<TemplateCustomVerificationParam> getCustomVerificationParams(Long id) {
        return getJoinList(id, TemplateCustomVerification_.customVerificationParams);
    }

    public List<TemplateCustomVerificationConf> getCustomVerificationConfs(Long id) {
        return getJoinList(id, TemplateCustomVerification_.customVerificationConfs);
    }
    
    /**
     * Saves given custom verification configurations for the
     * {@link TemplateCustomVerification}. Whole verification configurations
     * collection is replaced with given collection.
     *
     * @param id Template custom verification id
     * @param confs List of custom verification configurations.
     * @return Updated {@link TemplateCustomVerification}.
     * @throws NotFoundException If {@link TemplateCustomVerification} not found with
     * given id.
     */
    public TemplateCustomVerification saveCustomVerificationConfs(Long id, List<TemplateCustomVerificationConf> confs) throws NotFoundException {
        TemplateCustomVerification customVerification = read(id);
        for (TemplateCustomVerificationConf conf : customVerification.getCustomVerificationConfs()) {
            em.remove(conf);
        }
        customVerification.getCustomVerificationConfs().clear();

        log.info("Saving verification configurations for custom verification {}", customVerification);
        for (TemplateCustomVerificationConf customConf : confs) {
            RelationUtil.relate(customVerification, customConf);
        }
        return em.merge(customVerification);
    }    
}
