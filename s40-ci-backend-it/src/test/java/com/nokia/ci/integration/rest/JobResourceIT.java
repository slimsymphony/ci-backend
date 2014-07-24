/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.integration.rest;

import com.nokia.ci.client.model.JobVerificationConfView;
import com.nokia.ci.client.model.JobView;
import com.nokia.ci.client.rest.JobResource;
import com.nokia.ci.integration.CITestBase;
import dataset.RowFilter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.RowFilterTable;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.util.GenericType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
@Ignore
public class JobResourceIT extends CITestBase {

    private static JobResource proxy;

    @BeforeClass
    public static void setUpClass() {
        proxy = ProxyFactory.create(JobResource.class, API_BASE_URL);
    }

    /* This case is commented due to mocking issue with LDAP.
    @Test
    public void createJob() throws URISyntaxException {
    // setup
    final String jobName = "newly created job";
    JobView jobView = new JobView();
    jobView.setBranchId(-1L);
    jobView.setDisplayName("display name");
    jobView.setName(jobName);
    jobView.setUrl("url of new job");
    
    // create job
    ClientResponse r = (ClientResponse) proxy.createJob(jobView, 0L);
    Assert.assertEquals(r.getResponseStatus(),
    ClientResponse.Status.CREATED);
    
    // verify that it exists
    Assert.assertNotNull(r.getLocation());
    String resultLocation = r.getLocation().getHref();
    String resultId = parseIdFromLocationUrl(resultLocation);
    r = (ClientResponse) proxy.getJob(Long.valueOf(resultId));
    JobView created = (JobView) r.getEntity(JobView.class);
    Assert.assertEquals(jobName, created.getName());
    }*/

    /* This case is commented due to mocking issue with LDAP.
    @Test
    public void createJobForProject() throws URISyntaxException {
    // setup
    final String jobName = "newly created job";
    JobView jobView = new JobView();
    jobView.setBranchId(-1L);
    jobView.setDisplayName("display name");
    jobView.setName(jobName);
    jobView.setUrl("url of new job");
    
    // create job
    ClientResponse r = (ClientResponse) proxy.createJob(jobView, -1L);
    Assert.assertEquals(r.getResponseStatus(),
    ClientResponse.Status.CREATED);
    
    // verify that it exists
    Assert.assertNotNull(r.getLocation());
    String resultLocation = r.getLocation().getHref();
    String resultId = parseIdFromLocationUrl(resultLocation);
    r = (ClientResponse) proxy.getJob(Long.valueOf(resultId));
    JobView created = (JobView) r.getEntity(JobView.class);
    Assert.assertEquals(jobName, created.getName());
    }    
     */
    @Test
    public void updateJob() throws Exception {
        // setup
        final Long jobId = -1L;

        // get existing job
        ClientResponse r = (ClientResponse) proxy.getJob(jobId);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(),
                r.getStatus());
        JobView originalJob = (JobView) r.getEntity(JobView.class);

        // update
        JobView updateJob = new JobView();
        updateJob.setName("new name");
        updateJob.setUrl("new url");
        updateJob.setDisplayName("new display name");

        r = (ClientResponse) proxy.updateJob(jobId, updateJob);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(),
                r.getStatus());
        JobView result = (JobView) r.getEntity(JobView.class);

        // verify
        r = (ClientResponse) proxy.getJob(jobId);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(),
                r.getStatus());
        JobView currentJob = (JobView) r.getEntity(JobView.class);                
        
        Assert.assertEquals(updateJob.getName(), currentJob.getName());
        Assert.assertEquals(updateJob.getDisplayName(), 
                currentJob.getDisplayName());
        Assert.assertEquals(updateJob.getUrl(), currentJob.getUrl());
        Assert.assertEquals(currentJob.getBranchId().longValue(), -1L);
    }

    @Test
    public void getVerificationConfs() throws Exception {
        long jobId = -1;
        List<JobVerificationConfView> confs = fetchVerificationConfs(jobId);
        Assert.assertNotNull(confs);

        ITable filteredTable = new RowFilterTable(dataset.getTable(DatasetTool.JOB_VERIFICATION_CONF_TABLE_NAME),
                new RowFilter("" + jobId, "JOB_ID"));
        Assert.assertEquals(filteredTable.getRowCount(), confs.size());
        for (int i = 0; i < confs.size(); i++) {
            JobVerificationConfView conf = confs.get(i);
            long id = Long.valueOf((String) filteredTable.getValue(i, "ID"));
            long productId = Long.valueOf((String) filteredTable.getValue(i, "PRODUCT_ID"));
            long verificationId = Long.valueOf((String) filteredTable.getValue(i, "VERIFICATION_ID"));
            Assert.assertEquals("Ids should match!", id, conf.getId().longValue());
            Assert.assertEquals("Job id should match!", Long.valueOf(jobId).longValue(), conf.getJobId().longValue());
            Assert.assertEquals("Product id should match!", productId, conf.getProductId().longValue());
            Assert.assertEquals("Verification id should match!", verificationId, conf.getVerificationId().longValue());
        }
    }

    @Test
    public void saveVerificationConfs() throws Exception {
        long jobId = -1;
        List<JobVerificationConfView> views = new ArrayList<JobVerificationConfView>();
        JobVerificationConfView view = new JobVerificationConfView();
        view.setProductId(-2L);
        view.setVerificationId(-3L);
        views.add(view);
        Response response = proxy.saveVerificationConfs(jobId, views);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        List<JobVerificationConfView> updatedViews = fetchVerificationConfs(jobId);
        Assert.assertEquals(1, updatedViews.size());

        for (int i = 0; i < views.size(); i++) {
            JobVerificationConfView ref = views.get(i);
            JobVerificationConfView updated = updatedViews.get(i);
            Assert.assertEquals(ref.getProductId(), updated.getProductId());
            Assert.assertEquals(ref.getVerificationId(), updated.getVerificationId());
        }
    }

    private List<JobVerificationConfView> fetchVerificationConfs(long jobId) {
        ClientResponse response = (ClientResponse) proxy.getVerificationConfs(jobId);

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());

        GenericType genericType = (new GenericType<List<JobVerificationConfView>>() {
        });
        return (List<JobVerificationConfView>) response.getEntity(genericType);
    }
}
