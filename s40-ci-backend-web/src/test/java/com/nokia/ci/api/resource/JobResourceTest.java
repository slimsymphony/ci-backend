/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.resource;

import static com.nokia.ci.api.resource.WebTestBase.gson;
import com.nokia.ci.api.session.SessionManager;
import com.nokia.ci.api.session.SessionManager.Session;
import com.nokia.ci.api.util.ViewModelUtils;
import com.nokia.ci.client.model.JobVerificationConfView;
import com.nokia.ci.client.model.JobView;
import com.nokia.ci.client.rest.Header;
import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.JobEJB;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.exception.UnauthorizedException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobVerificationConf;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author jajuutin
 */
public class JobResourceTest extends WebTestBase {

    private static final String JOBS_BASE_URL = "/jobs/";
    private static final String JOBS_VERIFICATION_CONFS_URL = JOBS_BASE_URL + "%d/verificationConfs";
    private static final String JOBS_START_URL = "/jobs/%d/start?refSpec=%s";
    private static final String JOBS_BUILD_URL = "/jobs/%d/builds?start=%d&maxResults=%d&order=%s";
    private static final String JOBS_CREATE_WITH_PROJ_ID = "/jobs?projectId=%s";
    private JobResourceImpl jobResourceImpl;

    @Before
    @Override
    public void before() {
        super.before();

        jobResourceImpl = new JobResourceImpl();
        jobResourceImpl.jobEJB = Mockito.mock(JobEJB.class);
        jobResourceImpl.buildEJB = Mockito.mock(BuildEJB.class);
        jobResourceImpl.sessionManager = Mockito.mock(SessionManager.class);
        jobResourceImpl.projectEJB = Mockito.mock(ProjectEJB.class);

        dispatcher.getRegistry().addSingletonResource(jobResourceImpl);
    }

    protected String mockSessionManagement(Long sysUserId) {
        String token = UUID.randomUUID().toString();
        Session session = new SessionManager.Session();
        session.setSysUserId(sysUserId);
        session.setToken(token);
        Mockito.when(jobResourceImpl.sessionManager.getSession(token)).
                thenReturn(session);

        return token;
    }

    @Test
    public void createJob() throws Exception {
        Job job = createEntity(Job.class, 1L);
        populateJob(job);

        JobView jobView = ViewModelUtils.getModel(job, JobView.class);
        jobView.setBranchId(1L);

        // Returns BAD_REQUEST for now until this is actually supported
        MockHttpResponse response = invokePostRequest(JOBS_BASE_URL, gson.toJson(jobView), HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void updateJob() throws URISyntaxException, NotFoundException {
        // setup
        Job job = createEntity(Job.class, 1L);
        populateJob(job);
        JobView jobView = ViewModelUtils.getModel(job, JobView.class);
        jobView.setBranchId(1L);

        Mockito.when(jobResourceImpl.jobEJB.update(job)).thenReturn(job);

        // run
        MockHttpRequest request = createJsonPutRequest(JOBS_BASE_URL
                + job.getId().toString(), jobView);
        MockHttpResponse response = invokeRequest(request,
                HttpServletResponse.SC_OK);

        // verify
        Mockito.verify(jobResourceImpl.jobEJB,
                Mockito.atLeastOnce()).update(job);
    }

    @Test
    public void deleteJob() throws URISyntaxException, NotFoundException,
            UnauthorizedException {

        // setup
        long sysUserId = 1;
        String token = mockSessionManagement(sysUserId);

        Job job = createEntity(Job.class, 1L);
        populateJob(job);

        Mockito.when(jobResourceImpl.jobEJB.read(job.getId())).thenReturn(job);

        // run
        MockHttpRequest request = MockHttpRequest.delete(JOBS_BASE_URL
                + job.getId().toString());
        request.header(Header.AUTH_TOKEN, token);
        MockHttpResponse response = invokeRequest(request,
                HttpServletResponse.SC_OK);

        // verify
        Mockito.verify(jobResourceImpl.jobEJB,
                Mockito.atLeastOnce()).delete(job, sysUserId);
    }

    @Test
    public void deleteJobNotFound() throws URISyntaxException, NotFoundException,
            UnauthorizedException {
        long sysUserId = 1;
        String token = mockSessionManagement(sysUserId);
        Job job = createEntity(Job.class, 1L);
        populateJob(job);

        Mockito.when(jobResourceImpl.jobEJB.read(job.getId())).thenThrow(new NotFoundException());

        // run
        MockHttpRequest request = MockHttpRequest.delete(JOBS_BASE_URL
                + job.getId().toString());
        request.header(Header.AUTH_TOKEN, token);
        MockHttpResponse response = invokeRequest(request,
                HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void deleteJobUnauthorized() throws URISyntaxException, NotFoundException,
            UnauthorizedException {
        Job job = createEntity(Job.class, 1L);
        populateJob(job);

        // run
        MockHttpRequest request = MockHttpRequest.delete(JOBS_BASE_URL
                + job.getId().toString());

        MockHttpResponse response = invokeRequest(request,
                HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void getJob() throws URISyntaxException, NotFoundException {
        // setup
        Job job = createEntity(Job.class, 1L);
        populateJob(job);
        job.setBranch(createEntity(Branch.class, 1L));

        Mockito.when(jobResourceImpl.jobEJB.read(job.getId())).thenReturn(job);

        // run
        MockHttpRequest request = MockHttpRequest.get(JOBS_BASE_URL + job.getId());
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);

        // assert
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        JobView view = gson.fromJson(response.getContentAsString(), JobView.class);
        verifyJobView(view, job);
    }

    @Test
    public void getJobNotFound() throws URISyntaxException, NotFoundException {
        Mockito.when(jobResourceImpl.jobEJB.read(1L)).thenThrow(new NotFoundException());

        // run
        MockHttpRequest request = MockHttpRequest.get(JOBS_BASE_URL + 1L);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);

        // assert
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    @Test
    public void getJobs() throws Exception {
        // setup
        long sysUserId = 1;
        String token = mockSessionManagement(sysUserId);

        // run
        MockHttpRequest request = MockHttpRequest.get(JOBS_BASE_URL + "jobs");
        request.header(Header.AUTH_TOKEN, token);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);

        // assert
        // No implementation here actually:
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void getBuildsUnexistingJob() throws URISyntaxException,
            NotFoundException {

        // constants
        final Long jobId = 1L;

        // setup
        Mockito.when(jobResourceImpl.jobEJB.read(jobId)).thenThrow(
                new NotFoundException());

        // run
        MockHttpRequest request = createBuildsRequest(jobId,
                1, 10, "asc");
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);

        // verify
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND,
                response.getStatus());
    }

    @Test
    public void startJob() throws URISyntaxException, BackendAppException {
        // constants
        final Long jobId = 1L;
        final String refSpec = "refspec";

        // run
        final String url = String.format(JOBS_START_URL, jobId, refSpec);
        invokeGetRequest(url, HttpServletResponse.SC_OK);

        // verify
        Mockito.verify(jobResourceImpl.jobEJB, Mockito.atLeastOnce()).start(
                jobId, refSpec, null);
    }

    @Test
    public void startJobFail() throws URISyntaxException, BackendAppException {
        // constants
        final Long jobId = 1L;
        final String refSpec = "refspec";

        Mockito.when(jobResourceImpl.jobEJB.start(jobId, refSpec, null)).thenThrow(new NotFoundException());

        // run
        final String url = String.format(JOBS_START_URL, jobId, refSpec);
        invokeGetRequest(url, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void getVerificationConfs() throws Exception {
        long jobId = 1;
        int size = 4;
        Job job = createEntity(Job.class, jobId);
        List<JobVerificationConf> confs = createEntityList(JobVerificationConf.class, size);
        populateJobVerificationConfs(confs, job);

        Mockito.when(jobResourceImpl.jobEJB.getVerificationConfs(jobId)).thenReturn(confs);

        MockHttpResponse response = invokeGetRequest(String.format(JOBS_VERIFICATION_CONFS_URL, jobId),
                HttpServletResponse.SC_OK);

        List<JobVerificationConfView> confList = gson.fromJson(response.getContentAsString(),
                getJobVerificationConfViewCollectionType());
        verifyJobVerificationConfViewList(confList, confs);
    }

    @Test
    public void getVerificationConfsWithInvalidId() throws Exception {
        long jobId = 1;
        Mockito.when(jobResourceImpl.jobEJB.getVerificationConfs(jobId)).thenReturn(new ArrayList<JobVerificationConf>());

        MockHttpResponse response = invokeGetRequest(String.format(JOBS_VERIFICATION_CONFS_URL, jobId),
                HttpServletResponse.SC_OK);
        List<JobVerificationConfView> confList = gson.fromJson(response.getContentAsString(),
                getJobVerificationConfViewCollectionType());
        Assert.assertTrue("Confs should be empty", confList.isEmpty());
    }

    @Test
    public void saveVerificationConfs() throws Exception {
        long jobId = 1;
        int size = 4;
        Job job = createEntity(Job.class, jobId);
        List<JobVerificationConf> confs = createEntityList(JobVerificationConf.class, size);
        populateJobVerificationConfs(confs, job);

        List<JobVerificationConfView> views = new ArrayList<JobVerificationConfView>(confs.size());
        for (JobVerificationConf conf : confs) {
            JobVerificationConfView view = new JobVerificationConfView();
            view.setProductId(conf.getProduct().getId());
            view.setVerificationId(conf.getVerification().getId());
            views.add(view);
        }

        Mockito.when(jobResourceImpl.jobEJB.saveVerificationConfs(jobId, confs)).thenReturn(job);

        invokePutRequest(String.format(JOBS_VERIFICATION_CONFS_URL, jobId),
                gson.toJson(views), HttpServletResponse.SC_OK);
        Mockito.verify(jobResourceImpl.jobEJB, Mockito.atLeastOnce()).saveVerificationConfs(Mockito.eq(jobId), Mockito.anyListOf(JobVerificationConf.class));
    }

    @Test
    public void saveVerificationConfsWithInvalidId() throws Exception {
        long jobId = -1;
        Mockito.when(jobResourceImpl.jobEJB.saveVerificationConfs(Mockito.eq(jobId),
                Mockito.anyListOf(JobVerificationConf.class))).thenThrow(new NotFoundException());

        invokePutRequest(String.format(JOBS_VERIFICATION_CONFS_URL, jobId),
                gson.toJson(new ArrayList<JobVerificationConfView>()), HttpServletResponse.SC_NOT_FOUND);
    }

    private static MockHttpRequest createBuildsRequest(Long jobId,
            int start,
            int maxResults,
            String order) {
        MockHttpRequest request = null;

        final String url = String.format(JOBS_BUILD_URL,
                jobId, start, maxResults, order);

        try {
            request = MockHttpRequest.get(url);
        } catch (URISyntaxException ex) {
            Assert.assertTrue(false);
        }

        return request;
    }
}
