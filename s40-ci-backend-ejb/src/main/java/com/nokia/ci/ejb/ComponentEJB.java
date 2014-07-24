/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.Component;
import com.nokia.ci.ejb.model.Component_;
import com.nokia.ci.ejb.model.MemConsumption;
import com.nokia.ci.ejb.model.TestCaseStat;
import com.nokia.ci.ejb.model.TestCoverage;
import java.io.Serializable;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author larryang
 */

@Stateless
@LocalBean
public class ComponentEJB extends CrudFunctionality<Component> implements Serializable {
    
    private static Logger log = LoggerFactory.getLogger(ComponentEJB.class);

    public ComponentEJB() {
        super(Component.class);
    }
    
    public List<MemConsumption> getMemConsumptions(long id) {
        return getJoinList(id, Component_.memConsumptions);
    }
    
    public List<TestCaseStat> getTestCaseStats(long id) {
        return getJoinList(id, Component_.testCaseStats);
    }
    
    public List<TestCoverage> getTestCoverages(long id) {
        return getJoinList(id, Component_.testCoverages);
    }
}
