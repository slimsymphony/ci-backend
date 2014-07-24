package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.ResultDetailsParam;

import java.io.Serializable;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link ResultDetailsParam} object operations.
 * 
 * @author ttyppo
 */
@Stateless
@LocalBean
public class ResultDetailsParamEJB extends CrudFunctionality<ResultDetailsParam> implements Serializable {
    
    private static final Logger log = LoggerFactory.getLogger(ResultDetailsParamEJB.class);
    
    public ResultDetailsParamEJB() {
        super(ResultDetailsParam.class);
    }
}
