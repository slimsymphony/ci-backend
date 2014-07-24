/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.CIHelpTopic;
import com.nokia.ci.ejb.model.CIHelpTopic_;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author hhellgre
 */
@Stateless
@LocalBean
public class CIHelpTopicEJB extends CrudFunctionality<CIHelpTopic> {

    public CIHelpTopicEJB() {
        super(CIHelpTopic.class);
    }

    public List<CIHelpTopic> readAllSortByTopic() {
        CriteriaQuery<CIHelpTopic> criteria = cb.createQuery(CIHelpTopic.class);
        Root<CIHelpTopic> topic = criteria.from(CIHelpTopic.class);
        criteria.orderBy(cb.asc(topic.get(CIHelpTopic_.topic)));
        return em.createQuery(criteria).getResultList();
    }

    public CIHelpTopic getByName(String page) throws NotFoundException {
        CriteriaQuery<CIHelpTopic> criteria = cb.createQuery(CIHelpTopic.class);
        Root<CIHelpTopic> topic = criteria.from(CIHelpTopic.class);
        criteria.where(cb.equal(topic.get(CIHelpTopic_.page), page));
        List<CIHelpTopic> pages = em.createQuery(criteria).getResultList();

        if (pages.isEmpty()) {
            throw new NotFoundException("Help topic not found with page: " + page);
        }

        return pages.get(0);
    }
}
