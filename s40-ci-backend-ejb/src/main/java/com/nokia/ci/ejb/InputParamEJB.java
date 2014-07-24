package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.InputParam;
import java.io.Serializable;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link InputParam} object operations.
 * 
 * @author vrouvine
 */
@Stateless
@LocalBean
public class InputParamEJB extends CrudFunctionality<InputParam> implements Serializable {
    
    private static final Logger log = LoggerFactory.getLogger(InputParamEJB.class);
    
    public InputParamEJB() {
        super(InputParam.class);
    }
}
