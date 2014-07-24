package com.nokia.ci.api.util;

import com.nokia.ci.api.resource.WebTestBase;
import com.nokia.ci.client.exception.ViewTransformException;
import com.nokia.ci.client.model.BranchView;
import com.nokia.ci.client.model.JobView;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Job;
import java.util.List;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Unit test for ViewModelUtils.
 * @author vrouvine
 */
public class ViewModelUtilsTest extends WebTestBase {
    
    @Test
    public void branchListToBranchViewList() throws ViewTransformException {
        List<Branch> branches = createEntityList(Branch.class, 3);        
        populateBranches(branches);
        
        List<BranchView> views = ViewModelUtils.copyValuesFromList(branches, BranchView.class);
        
        verifyBranchViewList(views, branches);
    }
    
    @Test
    public void jobListToJobViewList() throws ViewTransformException {
        List<Job> jobs = createEntityList(Job.class, 3);
        populateJobs(jobs);

        List<JobView> views = ViewModelUtils.copyValuesFromList(jobs, JobView.class);
        verifyJobViewList(views, jobs);
    }
    
    @Test
    public void jobListViceVersa() {
        List<Job> jobs = createEntityList(Job.class, 3);
        populateJobs(jobs);

        List<JobView> views = ViewModelUtils.copyValuesFromList(jobs, JobView.class);
        verifyJobViewList(views, jobs);
        
        List<Job> jobsAgain = ViewModelUtils.transformToList(views, Job.class);
        Assert.assertEquals(jobs, jobsAgain);
    }    
}
