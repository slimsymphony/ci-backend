/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import java.io.Serializable;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ttyppo
 */
@Stateless
@LocalBean
public class BuildResultDetailsParamEJB extends CrudFunctionality<BuildResultDetailsParam> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(BuildResultDetailsParamEJB.class);

    public BuildResultDetailsParamEJB() {
        super(BuildResultDetailsParam.class);
    }
}
