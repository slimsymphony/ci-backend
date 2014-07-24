package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CustomParam;
import com.nokia.ci.ejb.model.CustomParamValue;
import com.nokia.ci.ejb.model.CustomParamValue_;
import com.nokia.ci.ejb.model.CustomParam_;
import java.io.Serializable;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Business logic implementation for {@link CustomParam} object operations.
 *
 * @author vrouvine
 */
@Stateless
@LocalBean
public class CustomParamEJB extends CrudFunctionality<CustomParam> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(CustomParamEJB.class);

    public CustomParamEJB() {
        super(CustomParam.class);
    }

    /**
     * Gets custom parameter values for custom parameter.
     *
     * @param id Custom parameter id
     * @return List of custom parameter values
     * @throws NotFoundException If given custom parameter not found.
     */
    public List<CustomParamValue> getCustomParamValues(Long id) {
        return getJoinList(id, CustomParam_.customParamValues);
    }

    /**
     * Checks if custom parameter already contains a particular value.
     *
     * @param value
     * @return
     */
    public Boolean hasParamValue(Long id, CustomParamValue value) {

        CriteriaQuery<CustomParamValue> query = cb.createQuery(CustomParamValue.class);
        Root<CustomParam> customParam = query.from(CustomParam.class);
        ListJoin<CustomParam, CustomParamValue> customParamValues = customParam.join(CustomParam_.customParamValues);
        query.select(customParamValues);
        query.where(cb.and(cb.equal(customParam, id), cb.equal(customParamValues.get(CustomParamValue_.paramValue), value.getParamValue())));

        List<CustomParamValue> results = em.createQuery(query).getResultList();
        if (!results.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if custom parameter contains same value with different id.
     *
     * @param value
     * @return
     */
    public Boolean hasDuplicateValue(Long id, CustomParamValue value) {

        CriteriaQuery<CustomParamValue> query = cb.createQuery(CustomParamValue.class);
        Root<CustomParam> customParam = query.from(CustomParam.class);
        ListJoin<CustomParam, CustomParamValue> customParamValues = customParam.join(CustomParam_.customParamValues);
        query.select(customParamValues);
        Predicate paramIdEqual = cb.equal(customParam, id);
        Predicate paramValueEqual = cb.equal(customParamValues.get(CustomParamValue_.paramValue), value.getParamValue());
        Predicate paramValueIdNotEqual = cb.notEqual(customParamValues.get(CustomParamValue_.id), value.getId());
        query.where(cb.and(paramIdEqual, paramValueEqual, paramValueIdNotEqual));

        List<CustomParamValue> results = em.createQuery(query).getResultList();
        if (!results.isEmpty()) {
            return true;
        }
        return false;

    }
}
