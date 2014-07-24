/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.resource.metrics;

import com.nokia.ci.api.resource.WebTestBase;
import com.nokia.ci.client.model.BuildEventView;
import com.nokia.ci.client.model.MetricsBuildEventSetView;
import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author hhellgre
 */
public class MetricsBuildEventSetResourceTest extends WebTestBase {

    private final static String EVENT_SET_URL = "/metricsBuildEventSet/";
    MetricsBuildEventSetResourceImpl impl;

    @Before
    @Override
    public void before() {
        super.before();
        impl = new MetricsBuildEventSetResourceImpl();
        impl.buildEJB = Mockito.mock(BuildEJB.class);
        dispatcher.getRegistry().addSingletonResource(impl);
    }

    @Test
    public void addTestEventSetEmpty() {
        MetricsBuildEventSetView view = new MetricsBuildEventSetView();
        invokeBuildEventSet(view, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void addTestEventSetEmptySet() {
        MetricsBuildEventSetView view = new MetricsBuildEventSetView();
        populateMetricsBuildEventSet(view);
        invokeBuildEventSet(view, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void addTestEventSetStartOk() {
        MetricsBuildEventSetView view = new MetricsBuildEventSetView();
        populateMetricsBuildEventSet(view);

        List<BuildEventView> events = generateBuildEventViews(5, "START");
        view.setEvents(events);

        invokeBuildEventSet(view, HttpServletResponse.SC_CREATED);
    }

    @Test
    public void addTestEventSetEndOk() {
        MetricsBuildEventSetView view = new MetricsBuildEventSetView();
        populateMetricsBuildEventSet(view);

        List<BuildEventView> events = generateBuildEventViews(5, "END");
        view.setEvents(events);

        invokeBuildEventSet(view, HttpServletResponse.SC_CREATED);
    }

    @Test
    public void addTestEventSetNameNull() {
        MetricsBuildEventSetView view = new MetricsBuildEventSetView();
        populateMetricsBuildEventSet(view);

        List<BuildEventView> events = generateBuildEventViews(1, "END");
        events.get(0).setName(null);
        view.setEvents(events);

        invokeBuildEventSet(view, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void addTestEventSetPhaseNull() {
        MetricsBuildEventSetView view = new MetricsBuildEventSetView();
        populateMetricsBuildEventSet(view);

        List<BuildEventView> events = generateBuildEventViews(1, "END");
        events.get(0).setPhase(null);
        view.setEvents(events);

        invokeBuildEventSet(view, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void addTestEventSetEventTypeMismatch() {
        MetricsBuildEventSetView view = new MetricsBuildEventSetView();
        populateMetricsBuildEventSet(view);

        List<BuildEventView> events = generateBuildEventViews(1, "NOT_OK");
        view.setEvents(events);

        invokeBuildEventSet(view, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void addTestEventSetTimeNull() {
        MetricsBuildEventSetView view = new MetricsBuildEventSetView();
        populateMetricsBuildEventSet(view);

        List<BuildEventView> events = generateBuildEventViews(1, "END");
        events.get(0).setTimestamp(null);
        view.setEvents(events);

        invokeBuildEventSet(view, HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void addTestEventSetBuildNotFound() throws Exception {
        MetricsBuildEventSetView view = new MetricsBuildEventSetView();
        populateMetricsBuildEventSet(view);

        List<BuildEventView> events = generateBuildEventViews(1, "START");
        view.setEvents(events);

        Mockito.when(impl.buildEJB.addBuildEvents(Mockito.any(Long.class), Mockito.any(List.class), Mockito.any(String.class))).thenThrow(new NotFoundException());

        invokeBuildEventSet(view, HttpServletResponse.SC_NOT_FOUND);
    }

    private MetricsBuildEventSetView invokeBuildEventSet(MetricsBuildEventSetView requestView,
            int expectedStatusCode) {

        MockHttpRequest request = createJsonPostRequest(EVENT_SET_URL,
                requestView);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);

        Assert.assertEquals(expectedStatusCode, response.getStatus());

        MetricsBuildEventSetView responseView = gson.fromJson(
                response.getContentAsString(), MetricsBuildEventSetView.class);

        return responseView;
    }
}
