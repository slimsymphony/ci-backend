package com.nokia.ci.ejb;

import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Build_;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectGroup;
import com.nokia.ci.ejb.model.ProjectGroup_;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 10/10/12
 * Time: 9:42 AM
 * To change this template use File | Settings | File Templates.
 */

@Stateless
@LocalBean
public class ProjectGroupEJB extends CrudFunctionality<ProjectGroup> implements Serializable {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(ProjectGroupEJB.class);

    public ProjectGroupEJB() {
        super(ProjectGroup.class);
    }

    @Override
    public void delete(ProjectGroup object) throws NotFoundException {
        ProjectGroup projectGroup = read(object.getId());
        solveAggregationRelationToProjects(projectGroup);
        super.delete(projectGroup);
    }

    /**
     * Project hasn't composition but aggregation relation to ProjectGroup, we don't want to delete all Project related to specific Group
     * Hibernate not supported vendor specific cascade type "ON DELETE SET NULL" therefore we must hand-coded removing references
     * @param projectGroup
     */
    private void solveAggregationRelationToProjects(ProjectGroup projectGroup){
        List<Project> projects = projectGroup.getProjects();
        for (Project project : projects) {
             project.setProjectGroup(null);
        }
        projectGroup.getProjects().clear();
    }

    public int getMaxOrder() {
        CriteriaQuery<Integer> criteria = cb.createQuery(Integer.class);
        Root<ProjectGroup> projectGroupRoot = criteria.from(ProjectGroup.class);
        criteria.select(cb.max(projectGroupRoot.get(ProjectGroup_.order)));
        Integer maxOrder = em.createQuery(criteria).getSingleResult();
        if (maxOrder==null){
            return 0;
        }
        return maxOrder;
    }
}
