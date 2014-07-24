/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildFailure;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class BuildFailureEJB extends CrudFunctionality<BuildFailure> {

    private static Logger log = LoggerFactory.getLogger(BuildFailureEJB.class);

    public BuildFailureEJB() {
        super(BuildFailure.class);
    }
}
