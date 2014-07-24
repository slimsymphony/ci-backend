package com.nokia.ci.ejb.util;

import com.nokia.ci.ejb.model.BuildGroup;
import com.nokia.ci.ejb.model.CIFaq;
import com.nokia.ci.ejb.model.CIHelpTopic;
import com.nokia.ci.ejb.model.Change;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SysUser;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link SearchUtil} class.
 *
 * @author vrouvine
 */
public class SearchUtilTest {

    private static final Logger log = LoggerFactory.getLogger(SearchUtilTest.class);

    @Test
    public void getIndexedClasses() throws Exception {
        Class[] expectedClasses = new Class[]{Project.class, Job.class, BuildGroup.class, SysUser.class, Change.class, CIHelpTopic.class, CIFaq.class};
        Set<Class<?>> indexedClasses = SearchUtil.getIndexedClasses();
        Assert.assertEquals("Indexed classes size should match!", expectedClasses.length, indexedClasses.size());
        for (Class ec : expectedClasses) {
            log.info("Expected class {}", ec);
            boolean found = false;
            for (Class ic : indexedClasses) {
                if (ec.equals(ic)) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("Indexed " + ec + " should found!", found);
        }
    }

    @Test
    public void getIndexedFieldsForBuildGroup() throws Exception {
        String[] expectedFields = new String[]{"bgId", "bgGerritBranch", "changes.changeAuthorEmail",
            "changes.changeCommitId", "job.jobDisplayName", "job.jobName"};
        Class c = BuildGroup.class;
        Set<String> indexedFields = SearchUtil.getIndexedFields(c);
        logFields(indexedFields, c);
        assertIndexedFields(indexedFields, expectedFields);
    }

    @Test
    public void getIndexedFieldsForChange() throws Exception {
        String[] expectedFields = new String[]{"changeCommitId", "changeAuthorName", "changeAuthorEmail",
            "changeMessage", "changeFiles.changeFilePath"};
        Class c = Change.class;
        Set<String> indexedFields = SearchUtil.getIndexedFields(c);
        logFields(indexedFields, c);
        assertIndexedFields(indexedFields, expectedFields);
    }

    private void logFields(Set<String> indexedFields, Class c) {
        log.info("******* Indexed fields for {} ********", c);
        for (String field : indexedFields) {
            log.info(field);
        }
        log.info("**************************************");
    }

    private void assertIndexedFields(Set<String> indexedFields, String... expectedFields) {
        for (String expected : expectedFields) {
            Assert.assertTrue("Indexed fields should contain [" + expected + "] field!", indexedFields.contains(expected));
        }
    }
}
