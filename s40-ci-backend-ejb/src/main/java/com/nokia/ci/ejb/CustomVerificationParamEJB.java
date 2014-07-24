package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.CustomVerificationParam;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link CustomVerificationParam} object
 * operations.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class CustomVerificationParamEJB extends CrudFunctionality<CustomVerificationParam> {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CustomVerificationParamEJB.class);

    public CustomVerificationParamEJB() {
        super(CustomVerificationParam.class);
    }
}
