package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.BranchType;
import com.nokia.ci.ejb.model.Gerrit;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.util.RelationUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link ProjectEJB} class.
 *
 * @author vrouvine
 */
public class ProjectEJBTest extends EJBTestBase {

    private static final long PROJECT_ID = 1;
    private ProjectEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new ProjectEJB();
        ejb.em = em;
        ejb.cb = cb;
    }

    private Project mockFindProject() {
        Project p = createEntity(Project.class, PROJECT_ID);
        Mockito.when(em.find(Project.class, PROJECT_ID)).thenReturn(p);
        return p;
    }

    @Test
    public void addBranch() throws Exception {
        // setup
        Project p = mockFindProject();
        Branch b = createEntity(Branch.class, 1L);
        b.setType(BranchType.TOOLBOX);

        Mockito.when(em.find(Branch.class, 1L)).thenReturn(b);

        // run
        ejb.addBranch(p.getId(), b);

        // verify
        Assert.assertTrue(p.getBranches().contains(b));
        Assert.assertTrue(b.getProject() == p);
    }

    @Test
    public void deleteProject() throws Exception {
        // setup
        Project p = mockFindProject();
        Branch b = createEntity(Branch.class, 1L);
        b.setType(BranchType.TOOLBOX);
        RelationUtil.relate(p, b);

        // run
        ejb.delete(p);

        // verify
        Assert.assertFalse(p.getBranches().contains(b));
        Assert.assertTrue(b.getProject() == null);
        Mockito.verify(em, Mockito.times(1)).remove(p);
    }

    @Test
    public void setGerrit() throws Exception {
        // constants
        final long oldGerritId = 1L;
        final long newGerritId = 2L;

        // setup
        Project project = mockFindProject();
        Gerrit oldGerrit = new Gerrit();
        oldGerrit.setId(oldGerritId);
        RelationUtil.relate(oldGerrit, project);

        Gerrit newGerrit = new Gerrit();
        newGerrit.setId(newGerritId);
        Mockito.when(em.find(Gerrit.class, Long.valueOf(newGerritId))).thenReturn(newGerrit);

        // run
        ejb.setGerrit(PROJECT_ID, newGerrit);

        // verify
        Assert.assertTrue(project.getGerrit() == newGerrit);
        Assert.assertTrue(newGerrit.getProjects().contains(project));
        Assert.assertFalse(oldGerrit.getProjects().contains(project));
    }
}
