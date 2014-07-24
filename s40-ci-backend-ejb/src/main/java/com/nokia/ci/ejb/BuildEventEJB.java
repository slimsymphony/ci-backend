/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildEvent;
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
public class BuildEventEJB extends CrudFunctionality<BuildEvent> {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(BuildEventEJB.class);

    public BuildEventEJB() {
        super(BuildEvent.class);
    }
}
