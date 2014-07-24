/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.SlaveLabel;
import com.nokia.ci.ejb.model.SlaveLabel_;
import com.nokia.ci.ejb.model.SlaveStatPerLabel;
import com.nokia.ci.ejb.util.RelationUtil;
import java.io.Serializable;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jaakkpaa
 */
@Stateless
@LocalBean
public class SlaveLabelEJB extends CrudFunctionality<SlaveLabel> implements Serializable {

    private static Logger log = LoggerFactory.getLogger(SlaveLabelEJB.class);

    public SlaveLabelEJB() {
        super(SlaveLabel.class);
        log.debug("SlaveLavelEJB()");
    }

    public List<SlaveLabel> getSlaveLabelList() {
        log.debug("SlaveLavelEJB.getSlaveLabelList()");

        CriteriaQuery<SlaveLabel> cq = cb.createQuery(SlaveLabel.class);
        Root<SlaveLabel> root = cq.from(SlaveLabel.class);

        cq.select(root);
        cq.orderBy(cb.asc(root.get(SlaveLabel_.name)));

        List<SlaveLabel> slaveLabels = em.createQuery(cq).getResultList();
        log.debug("SlaveLavelEJB.getSlaveLabelList() return(" + slaveLabels + ")");

        return slaveLabels;
    }
    
    public SlaveLabel getSlaveLabelByName(String name){
        log.debug("SlaveLavelEJB.getIdByName()");
        
        if (name == null){
            return null;
        }

        CriteriaQuery<SlaveLabel> cq = cb.createQuery(SlaveLabel.class);
        Root<SlaveLabel> root = cq.from(SlaveLabel.class);

        cq.select(root);
        cq.where(cb.equal(root.get(SlaveLabel_.name), name));

        SlaveLabel slaveLabel = em.createQuery(cq).getResultList().get(0);
        log.debug("SlaveLavelEJB.getSlaveLabelByName() return(" + slaveLabel + ")");

        return slaveLabel;
    }
    
    public SlaveLabel addSlaveStat(Long id, SlaveStatPerLabel slaveStatPerLabel) throws NotFoundException {
        log.debug("Received {} slave stat for label {}", id);
        long startTime = System.currentTimeMillis();

        SlaveLabel slaveLabel = read(id);

        RelationUtil.relate(slaveLabel, slaveStatPerLabel);

        log.debug("Finished adding slave stat for label {}. task done in {}ms", id, System.currentTimeMillis() - startTime);

        return slaveLabel;
    }
}