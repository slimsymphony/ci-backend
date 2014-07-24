package com.nokia.ci.api.resource;

import com.nokia.ci.api.session.SessionManager;
import com.nokia.ci.api.util.ViewModelUtils;
import com.nokia.ci.client.model.JobView;
import com.nokia.ci.client.rest.Header;
import com.nokia.ci.ejb.event.IncidentEventContent;
import com.nokia.ci.ejb.exception.BackendAppException;
import com.nokia.ci.ejb.model.Job;
import org.apache.http.client.utils.URIBuilder;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.enterprise.event.Event;
import javax.jms.JMSException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 3/28/13
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class ParametersValidatorInterceptorTest extends WebTestBase {

    private static final String SLAVE_ACTION_BASE_URL = "slaves/slaPool0";
    private static final String PROJECT_PRODUCTS_URL = "/projects/" + "%d/products";
    private static final String JOBS_CREATE = "/jobs";
    @Mock
    Event<IncidentEventContent> eventQueue;
    @Mock
    SlaveResourceImpl slaveResource;
    @Mock
    ProjectResourceImpl projectResource;
    @Mock
    JobResourceImpl jobResource;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        registerInterceptor();
        registerSlaveResource();
        registerJobResource();
        registerProjectResource();
    }

    private void registerInterceptor() {
        ParametersValidatorInterceptor interceptor = new ParametersValidatorInterceptor();
        interceptor.incidentEvents = eventQueue;
        dispatcher.getProviderFactory().getServerPreProcessInterceptorRegistry().register(interceptor);
    }

    private void registerSlaveResource() {
        Mockito.when(slaveResource.action(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString())).thenReturn(Response.status(Response.Status.OK).build());
        dispatcher.getRegistry().addSingletonResource(slaveResource);
    }

    private void registerJobResource() {
        try {
            Mockito.when(jobResource.createJob(Mockito.any(JobView.class), Mockito.anyLong(), Mockito.anyString())).thenReturn(Response.status(Response.Status.OK).build());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        dispatcher.getRegistry().addSingletonResource(jobResource);
    }

    private void registerProjectResource() {
        Mockito.when(projectResource.getProducts(Mockito.anyLong())).thenReturn(Response.status(Response.Status.OK).build());
        dispatcher.getRegistry().addSingletonResource(projectResource);
    }





    @Test
    public void slaveActionGoodParameters() throws BackendAppException, JMSException {
        try {
            URI baseUri = new URI("");
            URIBuilder ub = new URIBuilder(baseUri);
            ub.setPath(SLAVE_ACTION_BASE_URL);
            ub.setParameter("type", "available");
            ub.setParameter("labels", "labels0");
            ub.setParameter("executors", "0");
            ub.setParameter("master", "master0");
            ub.setParameter("release_id", "release_id0");
            URI uri = ub.build();
            String completeUrl = uri.toASCIIString();
            MockHttpResponse response = invokeGetRequest(completeUrl, 200);
            Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Mockito.verify(eventQueue, Mockito.never())
                .fire(Mockito.any(IncidentEventContent.class));
    }


    @Test
    public void slaveActionBadParameters() throws BackendAppException, JMSException {
        try {
            URI baseUri = new URI("");
            URIBuilder ub = new URIBuilder(baseUri);
            ub.setPath(SLAVE_ACTION_BASE_URL);
            ub.setParameter("type", "type0");
            ub.setParameter("labels", "labels0");
            ub.setParameter("executors", "0");
            ub.setParameter("master", "master0");
            ub.setParameter("release_id", "release_id0");
            ub.setParameter("release_idd", "release_idd0");
            URI uri = ub.build();
            String completeUrl = uri.toASCIIString();
            MockHttpResponse response = invokeGetRequest(completeUrl, 400);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Mockito.verify(eventQueue, Mockito.times(1))
                .fire(Mockito.any(IncidentEventContent.class));

    }

    @Test
    public void projectProductGoodParameters() throws BackendAppException, JMSException {
        try {
            URI baseUri = new URI("");
            URIBuilder ub = new URIBuilder(baseUri);
            ub.setPath(String.format(PROJECT_PRODUCTS_URL, 1));
            URI uri = ub.build();
            String completeUrl = uri.toASCIIString();
            MockHttpResponse response = invokeGetRequest(completeUrl, 200);
            Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Mockito.verify(eventQueue, Mockito.never())
                .fire(Mockito.any(IncidentEventContent.class));
    }

    @Test
    public void projectProductBadParameters() throws BackendAppException, JMSException {
        try {
            URI baseUri = new URI("");
            URIBuilder ub = new URIBuilder(baseUri);
            ub.setPath(String.format(PROJECT_PRODUCTS_URL, 1));
            ub.setParameter("type", "type0");
            URI uri = ub.build();
            String completeUrl = uri.toASCIIString();
            MockHttpResponse response = invokeGetRequest(completeUrl, 400);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Mockito.verify(eventQueue, Mockito.times(1))
                .fire(Mockito.any(IncidentEventContent.class));
    }

    @Test
    public void jobCreateGoodParameters() throws BackendAppException, JMSException {
        Long sysUserId = 1L;
        String token = mockSessionManagement(jobResource, sysUserId);
        try {
            URI baseUri = new URI("");
            URIBuilder ub = new URIBuilder(baseUri);
            ub.setPath(JOBS_CREATE);
            ub.setParameter("projectId", "1");
            URI uri = ub.build();
            String completeUrl = uri.toASCIIString();
            Job job = createEntity(Job.class, 1L);
            populateJob(job);
            JobView jobView = ViewModelUtils.getModel(job, JobView.class);
            MockHttpRequest request = createJsonPostRequest(completeUrl, jobView);
            request.header(Header.AUTH_TOKEN, token);
            MockHttpResponse response = new MockHttpResponse();
            dispatcher.invoke(request, response);
            Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Mockito.verify(eventQueue, Mockito.never())
                .fire(Mockito.any(IncidentEventContent.class));
    }

    @Test
    public void jobCreateBadParameters() throws BackendAppException, JMSException {
        Long sysUserId = 1L;
        String token = mockSessionManagement(jobResource, sysUserId);
        try {
            URI baseUri = new URI("");
            URIBuilder ub = new URIBuilder(baseUri);
            ub.setPath(JOBS_CREATE);
            ub.setParameter("projectIdd", "1");
            URI uri = ub.build();
            String completeUrl = uri.toASCIIString();
            Job job = createEntity(Job.class, 1L);
            populateJob(job);
            JobView jobView = ViewModelUtils.getModel(job, JobView.class);
            MockHttpRequest request = createJsonPostRequest(completeUrl, jobView);
            request.header(Header.AUTH_TOKEN, token);
            MockHttpResponse response = new MockHttpResponse();
            dispatcher.invoke(request, response);
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        Mockito.verify(eventQueue, Mockito.times(1))
                .fire(Mockito.any(IncidentEventContent.class));
    }

    protected String mockSessionManagement(JobResourceImpl jobResourceImpl, Long sysUserId) {
        String token = UUID.randomUUID().toString();
        SessionManager.Session session = new SessionManager.Session();
        session.setSysUserId(sysUserId);
        session.setToken(token);
        jobResourceImpl.sessionManager = Mockito.mock(SessionManager.class);
        Mockito.when(jobResourceImpl.sessionManager.getSession(token)).
                thenReturn(session);
        return token;
    }


}
