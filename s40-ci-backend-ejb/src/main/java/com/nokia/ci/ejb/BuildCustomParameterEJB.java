/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildCustomParameter;
import java.io.Serializable;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class BuildCustomParameterEJB extends CrudFunctionality<BuildCustomParameter> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(BuildCustomParameterEJB.class);

    public BuildCustomParameterEJB() {
        super(BuildCustomParameter.class);
    }
}
