package com.nokia.ci.api.resource;

import com.nokia.ci.api.util.ViewModelUtils;
import com.nokia.ci.client.model.IncidentView;
import com.nokia.ci.ejb.IncidentEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Incident;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;

/**
 * Created by IntelliJ IDEA. User: djacko Date: 4/8/13 Time: 1:16 PM To change
 * this template use File | Settings | File Templates.
 */
public class IncidentResourceTest extends WebTestBase {

    private static final String INCIDENTS_BASE_URL = "/incidents/";
    private IncidentResourceImpl incidentResource;

    @Before
    @Override
    public void before() {
        super.before();
        incidentResource = new IncidentResourceImpl();
        incidentResource.incidentEJB = Mockito.mock(IncidentEJB.class);
        dispatcher.getRegistry().addSingletonResource(incidentResource);
    }

    @Test
    public void createIncident() throws URISyntaxException, NotFoundException {
        // setup
        Long sysUserId = 1L;
        Incident incident = createEntity(Incident.class, 1L);
        populateIncident(incident);
        IncidentView incidentView = ViewModelUtils.getModel(incident, IncidentView.class);
        // run
        MockHttpRequest request = createJsonPostRequest(INCIDENTS_BASE_URL, incidentView);
        MockHttpResponse response = invokeRequest(request, HttpServletResponse.SC_CREATED);

        // verify
        Mockito.verify(incidentResource.incidentEJB, Mockito.atLeastOnce()).create(Mockito.any(Incident.class));
    }

    @Test
    public void getIncident() throws URISyntaxException, NotFoundException {
        // setup
        Incident incident = createEntity(Incident.class, 1L);
        populateIncident(incident);
        Mockito.when(incidentResource.incidentEJB.read(incident.getId())).thenReturn(incident);
        // run
        MockHttpRequest request = MockHttpRequest.get(INCIDENTS_BASE_URL + incident.getId());
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);

        // assert
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        IncidentView view = gson.fromJson(response.getContentAsString(), IncidentView.class);
        verifyIncidentView(view, incident);
    }

    @Test
    public void getIncidentNotFound() throws Exception {
        // setup
        Mockito.when(incidentResource.incidentEJB.read(1L)).thenThrow(new NotFoundException());
        // run
        MockHttpRequest request = MockHttpRequest.get(INCIDENTS_BASE_URL + 1L);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        // assert
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
}
