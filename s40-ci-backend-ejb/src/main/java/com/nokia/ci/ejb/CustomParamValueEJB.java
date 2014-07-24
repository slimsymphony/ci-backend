package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.CustomParamValue;
import java.io.Serializable;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link CustomParamValue} object operations.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class CustomParamValueEJB extends CrudFunctionality<CustomParamValue> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CustomParamValueEJB.class);

    public CustomParamValueEJB() {
        super(CustomParamValue.class);
    }
}
