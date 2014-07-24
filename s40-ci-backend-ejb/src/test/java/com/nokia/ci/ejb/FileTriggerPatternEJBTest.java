/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import static com.nokia.ci.ejb.CITestBase.createEntity;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.ChangeFile;
import com.nokia.ci.ejb.model.FileTriggerPattern;
import com.nokia.ci.ejb.model.Job;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author hhellgre
 */
public class FileTriggerPatternEJBTest extends EJBTestBase {

    private FileTriggerPatternEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new FileTriggerPatternEJB();
        ejb.em = em;
        ejb.cb = cb;
        ejb.context = context;
        ejb.jobEJB = Mockito.mock(JobEJB.class);
    }

    @Test
    public void testFilePatternMatchStartOk() throws BackendAppException {
        List<FileTriggerPattern> triggerPatterns = createEntityList(FileTriggerPattern.class, 2);
        for (int i = 0; i < 2; i++) {
            triggerPatterns.get(i).setFilepath("/tmp/");
        }

        List<Change> changes = createEntityList(Change.class, 5);
        List<ChangeFile> changeFiles = createEntityList(ChangeFile.class, 1);
        changeFiles.get(0).setFilePath("/tmp/");
        changes.get(0).setChangeFiles(changeFiles);

        Mockito.when(ejb.jobEJB.getFileTriggerPatterns(1L)).thenReturn(triggerPatterns);

        boolean ret = ejb.checkFilePathMatch(1L, changes);

        Assert.assertEquals(true, ret);
    }

    @Test
    public void testFilePatternMatchPartOk() throws BackendAppException {
        List<FileTriggerPattern> triggerPatterns = createEntityList(FileTriggerPattern.class, 2);
        for (int i = 0; i < 2; i++) {
            triggerPatterns.get(i).setFilepath("tmp/");
        }

        List<Change> changes = createEntityList(Change.class, 5);
        List<ChangeFile> changeFiles = createEntityList(ChangeFile.class, 1);
        changeFiles.get(0).setFilePath("/test/where/is/tmp/");
        changes.get(0).setChangeFiles(changeFiles);

        Mockito.when(ejb.jobEJB.getFileTriggerPatterns(1L)).thenReturn(triggerPatterns);

        boolean ret = ejb.checkFilePathMatch(1L, changes);

        Assert.assertEquals(true, ret);
    }

    @Test
    public void testFilePatternMatchFail() throws BackendAppException {
        List<FileTriggerPattern> triggerPatterns = createEntityList(FileTriggerPattern.class, 2);
        for (int i = 0; i < 2; i++) {
            triggerPatterns.get(i).setFilepath("/tmp/");
        }

        List<Change> changes = createEntityList(Change.class, 5);
        List<ChangeFile> changeFiles = createEntityList(ChangeFile.class, 1);
        changeFiles.get(0).setFilePath("/notFound/");
        changes.get(1).setChangeFiles(changeFiles);

        Mockito.when(ejb.jobEJB.getFileTriggerPatterns(1L)).thenReturn(triggerPatterns);

        boolean ret = ejb.checkFilePathMatch(1L, changes);

        Assert.assertEquals(false, ret);
    }
}
