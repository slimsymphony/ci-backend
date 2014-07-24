package com.nokia.ci.ejb.it;

import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeTracker;
import com.nokia.ci.ejb.model.Project;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for ProjectEJB class. Tests are running inside container.
 *
 * @author vrouvine
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@UsingDataSet(value = {"projects_dataset.yml"})
public class ProjectEJBIT {

    private static final Logger log = LoggerFactory.getLogger(ProjectEJBIT.class);
    @Inject
    private ProjectEJB projectEJB;

    @Deployment
    public static EnterpriseArchive createDeployment() {
        return EJBDeploymentCreator.createEJBDeployment("pom.xml");
    }

    @Test
    public void createProject() {
        projectEJB.create(new Project());
        List<Project> projects = projectEJB.readAll();
        Assert.assertEquals("Project count does not match!", 5, projects.size());
    }

    @Test
    public void getProjects() {
        List<Project> projects = projectEJB.readAll();
        Assert.assertEquals("Project count does not match!", 4, projects.size());
    }

    @Test
    public void getChangeTrackers() throws NotFoundException {
        List<Change> changes = new ArrayList<Change>();

        Change c = new Change();
        c.setCommitId("linked1");
        changes.add(c);

        c = new Change();
        c.setCommitId("linked2");
        changes.add(c);

        c = new Change();
        c.setCommitId("unlinked1");
        changes.add(c);

        c = new Change();
        c.setCommitId("unlinked2");
        changes.add(c);

        c = new Change();
        c.setCommitId("new1");
        changes.add(c);

        c = new Change();
        c.setCommitId("new2");
        changes.add(c);

        Collection<ChangeTracker> cts = projectEJB.getChangeTrackers(1L, changes);
        Assert.assertEquals("Change tracker count does not match!", 6, cts.size());
        
        Collection<ChangeTracker> projectCts = projectEJB.getChangeTrackers(1L);
        Assert.assertEquals("Project change tracker count does not match!", 6, projectCts.size());
    }
}
