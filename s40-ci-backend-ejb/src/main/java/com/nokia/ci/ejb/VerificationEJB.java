/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CustomParam;
import com.nokia.ci.ejb.model.InputParam;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ResultDetailsParam;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.TestResultType;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.model.VerificationFailureReason;
import com.nokia.ci.ejb.model.Verification_;
import java.io.Serializable;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
@Stateless
@LocalBean
public class VerificationEJB extends CrudFunctionality<Verification> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(VerificationEJB.class);

    public VerificationEJB() {
        super(Verification.class);
    }

    public Verification getVerificationByUuid(String uuid) {
        if (StringUtils.isEmpty(uuid)) {
            log.debug("No verification found with uuid {}", uuid);
            return null;
        }
        log.debug("Finding verification with uuid {}", uuid);
        CriteriaQuery<Verification> query = cb.createQuery(Verification.class);
        Root<Verification> root = query.from(Verification.class);

        query.where(cb.equal(root.get(Verification_.uuid), uuid));

        try {
            return em.createQuery(query).getSingleResult();
        } catch (NoResultException nre) {
            log.debug("No verification found with uuid {}", uuid);
        }

        return null;
    }

    /**
     * Gets all custom parameters for verification.
     *
     * @param id Verification ID
     * @return List of custom parameters
     */
    public List<CustomParam> getCustomParams(Long id) {
        return getJoinList(id, Verification_.customParams);
    }

    /**
     * Gets all input parameters for verification.
     *
     * @param id Verification ID
     * @return List of input parameters.
     */
    public List<InputParam> getInputParams(Long id) {
        return getJoinList(id, Verification_.inputParams);
    }

    /**
     * Gets all result details parameters for verification.
     *
     * @param id Verification ID
     * @return List of result details parameters.
     */
    public List<ResultDetailsParam> getResultDetailsParams(Long id) {
        return getJoinList(id, Verification_.resultDetailsParams);
    }

    /**
     * Gets all parent verifications for verification.
     *
     * @param id Verification ID
     * @return List of parent verifications
     */
    public List<Verification> getParentVerifications(Long id) {
        return getJoinList(id, Verification_.parentVerifications);
    }

    /**
     * Gets all SlaveLabels for verification.
     *
     * @param id Verification ID
     * @return List of SlaveLabels
     */
    public List<SlaveLabel> getSlaveLabels(Long id) throws NotFoundException {
        log.debug("Querying slavelabels for verification id {}", id);
        return getJoinList(id, Verification_.slaveLabels);
    }

    /**
     * Gets all child verifications for verification.
     *
     * @param id Verification ID
     * @return List of child verifications
     */
    public List<Verification> getChildVerifications(Long id) {
        return getJoinList(id, Verification_.childVerifications);
    }

    /**
     * Gets projects for verification.
     *
     * @param id Verification ID
     * @return List of child verifications
     */
    public List<Project> getProjects(Long id) {
        return getJoinList(id, Verification_.projects);
    }

    public List<VerificationFailureReason> getFailureReasons(Long id) {
        return getJoinList(id, Verification_.failureReasons);
    }
}
