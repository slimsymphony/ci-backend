/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildFailureReason;
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
public class BuildFailureReasonEJB extends CrudFunctionality<BuildFailureReason> {

    private static Logger log = LoggerFactory.getLogger(BuildFailureReasonEJB.class);

    public BuildFailureReasonEJB() {
        super(BuildFailureReason.class);
    }
}
