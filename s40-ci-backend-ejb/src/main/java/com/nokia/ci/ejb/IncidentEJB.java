package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.Incident;
import com.nokia.ci.ejb.model.IncidentType;
import com.nokia.ci.ejb.model.Incident_;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;
import javax.persistence.criteria.Predicate;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 3/7/13 Time: 3:23 PM To change
 * this template use File | Settings | File Templates.
 */
@Stateless
@LocalBean
public class IncidentEJB extends CrudFunctionality<Incident> implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(Incident.class);

    public IncidentEJB() {
        super(Incident.class);
    }

    public Long getNumberOfUncheckedIncidents() {
        log.debug("Finding incidents where checkUser is null and checkTime is null");
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Incident> incidentRoot = query.from(Incident.class);
        query.select(cb.count(incidentRoot));
        query.where(cb.and(incidentRoot.get(Incident_.checkUser).isNull(), incidentRoot.get(Incident_.checkTime).isNull()));
        query.orderBy(cb.desc(incidentRoot.get(Incident_.time)));
        Long count = 0L;
        try {
            count = em.createQuery(query).getSingleResult();
        } catch (Exception e) {
            return 0L;
        }

        return count;
    }

    public List<Incident> getUncheckedIncidents() {
        log.debug("Finding incidents where checkUser is null and checkTime is null");
        CriteriaQuery<Incident> query = cb.createQuery(Incident.class);
        Root<Incident> incidentRoot = query.from(Incident.class);
        query.where(cb.and(incidentRoot.get(Incident_.checkUser).isNull(), incidentRoot.get(Incident_.checkTime).isNull()));
        query.orderBy(cb.desc(incidentRoot.get(Incident_.time)));
        List<Incident> incidents = em.createQuery(query).getResultList();

        return incidents;
    }

    public List<Incident> getUncheckedIncidentsByType(IncidentType type) {
        log.debug("Finding incident wher checkUser is null and checkTime is null and type is {}", type);
        CriteriaQuery<Incident> query = cb.createQuery(Incident.class);
        Root<Incident> incidentRoot = query.from(Incident.class);
        Predicate notChecked = cb.and(incidentRoot.get(Incident_.checkUser).isNull(), incidentRoot.get(Incident_.checkTime).isNull());
        Predicate typeCheck = cb.equal(incidentRoot.get(Incident_.type), type);
        query.where(cb.and(notChecked, typeCheck));
        query.orderBy(cb.desc(incidentRoot.get(Incident_.time)));
        List<Incident> incidents = em.createQuery(query).getResultList();

        return incidents;
    }

    public List<Incident> getCheckedIncidents() {
        log.debug("Finding incidents where checkUser is not null and checkTime is not null");
        CriteriaQuery<Incident> query = cb.createQuery(Incident.class);
        Root<Incident> incidentRoot = query.from(Incident.class);
        query.where(cb.and(incidentRoot.get(Incident_.checkUser).isNotNull(), incidentRoot.get(Incident_.checkTime).isNotNull()));
        query.orderBy(cb.desc(incidentRoot.get(Incident_.time)));
        List<Incident> incidents = em.createQuery(query).getResultList();

        return incidents;
    }

    public List<Incident> getCheckedIncidentsByType(IncidentType type) {
        log.debug("Finding incident wher checkUser is not null and checkTime is not null and type is {}", type);
        CriteriaQuery<Incident> query = cb.createQuery(Incident.class);
        Root<Incident> incidentRoot = query.from(Incident.class);
        Predicate notChecked = cb.and(incidentRoot.get(Incident_.checkUser).isNotNull(), incidentRoot.get(Incident_.checkTime).isNotNull());
        Predicate typeCheck = cb.equal(incidentRoot.get(Incident_.type), type);
        query.where(cb.and(notChecked, typeCheck));
        query.orderBy(cb.desc(incidentRoot.get(Incident_.time)));
        List<Incident> incidents = em.createQuery(query).getResultList();

        return incidents;
    }
}
