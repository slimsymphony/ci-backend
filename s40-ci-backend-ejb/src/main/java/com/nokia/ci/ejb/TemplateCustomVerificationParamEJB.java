package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.TemplateCustomVerificationParam;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link TemplateCustomVerificationParam} object
 * operations.
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class TemplateCustomVerificationParamEJB extends CrudFunctionality<TemplateCustomVerificationParam> {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(TemplateCustomVerificationParamEJB.class);

    public TemplateCustomVerificationParamEJB() {
        super(TemplateCustomVerificationParam.class);
    }
}
