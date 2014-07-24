/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildInputParam;
import java.io.Serializable;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jarno
 */
@Stateless
@LocalBean
public class BuildInputParamEJB extends CrudFunctionality<BuildInputParam> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(BuildInputParamEJB.class);

    public BuildInputParamEJB() {
        super(BuildInputParam.class);
    }
}
