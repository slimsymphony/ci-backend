/**
 * 
 */
package com.nokia.ci.ejb.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nokia.ci.ejb.CITestBase;
import com.nokia.ci.ejb.gerrit.GerritClient;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.BuildResultDetailsParam;
import com.nokia.ci.ejb.model.BuildVerificationConf;
import com.nokia.ci.ejb.model.Change;

/**
 * @author ttyppo
 *
 */
public class ReportingUtilTest extends CITestBase {

    private static Logger log = LoggerFactory.getLogger(ReportingUtilTest.class);
    
    /**
     * Test method for {@link com.nokia.ci.ejb.util.ReportingUtil#formatBuildResults(java.util.List)}.
     */
    @Test
    public void testFormatBuildResultsWithBuildVerificationConf() {
        
        List<Build> childBuilds = new ArrayList<Build>();
        List<BuildVerificationConf> childBuildVerificationConfs = new ArrayList<BuildVerificationConf>();
        
        for (int i = 0; i < 15; i++) {
            Build build = new Build();
            build.setId((long)i);
            childBuilds.add(build);
            BuildVerificationConf buildVerificationConf = new BuildVerificationConf();
            buildVerificationConf.setId((long)i);
            childBuildVerificationConfs.add(buildVerificationConf);
        }
        
        populateBuildsWithBuildVerificationConfs(childBuilds, childBuildVerificationConfs);
        
        childBuildVerificationConfs.get(0).setVerificationDisplayName("This is really a too big verification display name");
        childBuildVerificationConfs.get(0).setProductDisplayName("This is really a too big product display name");
        List<String> results = ReportingUtil.formatBuildResults(childBuilds);

        for (String result : results) {
            log.info(result);
        }
        
        Assert.assertEquals(15, results.size());
        Assert.assertEquals("[M] This is really a too b... This is really a ... SUCCESS    0 seconds           ", results.get(0));
        Assert.assertEquals("[M] Verification 10           Product 10           SUCCESS    0 seconds           ", results.get(1));
    }

    /**
     * Test method for {@link com.nokia.ci.ejb.util.ReportingUtil#formatBuildResults(java.util.List)}.
     */
    @Test
    public void testFormatBuildResults() {
        
        List<Build> childBuilds = new ArrayList<Build>();
        
        for (int i = 0; i < 15; i++) {
            Build build = new Build();
            build.setId((long)i);
            childBuilds.add(build);
        }
        
        populateBuildsWithBuildVerificationConfs(childBuilds, null);
        
        childBuilds.get(0).setJobDisplayName("This is really a too big job display name");
        List<String> results = ReportingUtil.formatBuildResults(childBuilds);

        for (String result : results) {
            log.info(result);
        }
        
        Assert.assertEquals(15, results.size());
        Assert.assertEquals("This is really a too big job display ... SUCCESS    0 seconds           ", results.get(0));
        Assert.assertEquals("Verification1---Product1                 SUCCESS    10 seconds          ", results.get(1));
        
    }
    
    @Test
    public void testFormatChanges() {
        
        List<Change> changes = new ArrayList<Change>();
        for (int i = 0; i < 5; i++) {
            Change change = new Change();
            change.setId((long)i);
            changes.add(change);
        }
        
        populateChanges(changes);
        
        List<String> results = ReportingUtil.formatChanges(changes);

        for (String result : results) {
            log.info(result);
        }

        Assert.assertEquals(5, results.size());
        Assert.assertEquals("01234567890abcdef1234567890abcdef1234567 This is change subject for commit 0 a... Change Author 0     ", results.get(0));
        
    }

    @Test
    public void testFormatBuildResultDetailsParams() {
        
        List<BuildResultDetailsParam> resultDetails = new ArrayList<BuildResultDetailsParam>();
        for (int i = 0; i < 5; i++) {
            BuildResultDetailsParam resultDetail = new BuildResultDetailsParam();
            resultDetail.setId((long)i);
            
            Build build = new Build();
            build.setId((long)i);
          
            BuildVerificationConf bvc = new BuildVerificationConf();
            bvc.setId((long) i);
            
            populateBuildWithBuildVerificationConf(build, bvc);
            resultDetail.setBuildVerificationConf(bvc);
            
            resultDetails.add(resultDetail);
        }
        
        populateBuildResultDetailsParams(resultDetails);
        
        List<String> results = ReportingUtil.formatBuildResultDetailsParams(resultDetails);

        for (String result : results) {
            log.info(result);
        }

        Assert.assertEquals(5, results.size());
        Assert.assertEquals("Verification 0       Product 0       Link to Jenkins build 0   https://test.jenkins.server.com:8080/job/test_project---test_verification_1/0", results.get(0));
        
    }
}
