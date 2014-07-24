/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.BuildCustomParameter;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.BuildVerificationConf_;
import java.io.Serializable;
import java.util.List;
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
public class BuildVerificationConfEJB extends CrudFunctionality<BuildVerificationConf> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(BuildVerificationConfEJB.class);

    public BuildVerificationConfEJB() {
        super(BuildVerificationConf.class);
    }

    public List<BuildCustomParameter> getCustomParameters(Long id) {
        return getJoinList(id, BuildVerificationConf_.customParameters);
    }
}
