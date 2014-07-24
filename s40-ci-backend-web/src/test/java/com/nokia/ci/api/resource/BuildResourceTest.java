package com.nokia.ci.api.resource;

import static com.nokia.ci.api.resource.WebTestBase.gson;
import com.nokia.ci.client.model.BuildEventView;
import com.nokia.ci.client.model.BuildView;
import com.nokia.ci.client.rest.BuildResource;
import com.nokia.ci.ejb.BuildEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Build;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test cases for {@link BuildResource}
 *
 * @author vrouvine
 */
public class BuildResourceTest extends WebTestBase {

    private static final Logger log = LoggerFactory.getLogger(BuildResourceTest.class);
    private static final String BUILDS_BASE_URL = "/builds/";
    private static final String REFSPEC_SEARCH_URL = BUILDS_BASE_URL + "?refSpec=%s";
    private BuildResourceImpl buildResourceImpl;

    @Before
    @Override
    public void before() {
        super.before();
        buildResourceImpl = new BuildResourceImpl();
        buildResourceImpl.buildEJB = Mockito.mock(BuildEJB.class);
        dispatcher.getRegistry().addSingletonResource(buildResourceImpl);
    }

    @Test
    public void createBuild() throws Exception {
        final Long buildId = 1L;
        Build b = createEntity(Build.class, buildId);
        populateBuild(b);

        BuildView bv = new BuildView();
        bv.copyValuesFrom(b);

        MockHttpResponse response = invokePostRequest(BUILDS_BASE_URL, gson.toJson(bv),
                HttpServletResponse.SC_CREATED);
    }

    @Test
    public void getBuild() throws Exception {
        final Long buildId = 1L;
        Build b = createEntity(Build.class, buildId);
        populateBuild(b);

        Mockito.when(buildResourceImpl.buildEJB.read(buildId)).thenReturn(b);

        MockHttpResponse response = invokeGetRequest(BUILDS_BASE_URL + buildId,
                HttpServletResponse.SC_OK);

        BuildView bv = gson.fromJson(response.getContentAsString(), BuildView.class);
        verifyBuildView(bv, b);
    }

    @Test
    public void getBuildNotFound() throws Exception {
        final Long buildId = 1L;

        Mockito.when(buildResourceImpl.buildEJB.read(buildId)).thenThrow(new NotFoundException());

        MockHttpResponse response = invokeGetRequest(BUILDS_BASE_URL + buildId,
                HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void getBuildsByRefSpec() throws Exception {
        List<Build> builds = createEntityList(Build.class, 1);
        populateBuilds(builds);

        Build b = builds.get(0);
        Mockito.when(buildResourceImpl.buildEJB.getBuildsByRefSpec(
                b.getRefSpec())).thenReturn(builds);

        MockHttpRequest request = MockHttpRequest.get(String.format(REFSPEC_SEARCH_URL, b.getRefSpec()));
        log.info("Request from url {}", request.getUri().getAbsolutePath());
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        List<BuildView> results = gson.fromJson(response.getContentAsString(), getBuildViewCollectionType());
        verifyBuildViewList(results, builds);
    }

    @Test
    public void getBuildsWithEmptyRefSpec() throws Exception {
        MockHttpRequest request = MockHttpRequest.get(String.format(REFSPEC_SEARCH_URL, ""));
        log.info("Request from url {}", request.getUri().getAbsolutePath());
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void getBuildsWithoutRefSpec() throws Exception {
        MockHttpRequest request = MockHttpRequest.get(BUILDS_BASE_URL);
        log.info("Request from url {}", request.getUri().getAbsolutePath());
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void setExecutor() throws Exception {
        final Long buildId = 1L;
        final String executor = "executor.domain.com";
        Build originalBuild = createEntity(Build.class, buildId);
        populateBuild(originalBuild);

        Mockito.when(buildResourceImpl.buildEJB.read(buildId)).thenReturn(originalBuild);

        MockHttpRequest request = MockHttpRequest.post(BUILDS_BASE_URL + buildId + "/executor");
        request.content(executor.getBytes());
        invokeRequest(request, HttpServletResponse.SC_OK);

        Mockito.verify(buildResourceImpl.buildEJB, Mockito.times(1)).update(originalBuild);

        Assert.assertEquals("Executor should match!", executor, originalBuild.getExecutor());
    }

    @Test
    public void setExecutorInvalidBuildId() throws Exception {
        final Long buildId = 1L;
        final String executor = "executor.domain.com";
        Build originalBuild = createEntity(Build.class, buildId);
        populateBuild(originalBuild);

        Mockito.when(buildResourceImpl.buildEJB.read(buildId)).thenThrow(new NotFoundException("Not found!"));

        MockHttpRequest request = MockHttpRequest.post(BUILDS_BASE_URL + buildId + "/executor");
        request.content(executor.getBytes());
        invokeRequest(request, HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void createBuildEventsEmptySetTest() throws Exception {
        final Long buildId = 1L;
        List<BuildEventView> buildEventViews = new ArrayList<BuildEventView>();

        MockHttpResponse response = invokePostRequest(BUILDS_BASE_URL + buildId + "/buildEvents", gson.toJson(buildEventViews),
                HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void createBuildEventsSanityCheckFailTest() throws Exception {
        final Long buildId = 1L;
        List<BuildEventView> buildEventViews = generateBuildEventViews(5, "NOT_GOOD_PHASE");

        MockHttpResponse response = invokePostRequest(BUILDS_BASE_URL + buildId + "/buildEvents", gson.toJson(buildEventViews),
                HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void createBuildEventsOkTest() throws Exception {
        final Long buildId = 1L;
        List<BuildEventView> buildEventViews = generateBuildEventViews(5, "START");

        MockHttpResponse response = invokePostRequest(BUILDS_BASE_URL + buildId + "/buildEvents", gson.toJson(buildEventViews),
                HttpServletResponse.SC_CREATED);
    }

    @Test
    public void createBuildEventsNotFoundTest() throws Exception {
        final Long buildId = 1L;
        List<BuildEventView> buildEventViews = generateBuildEventViews(5, "START");

        Mockito.when(buildResourceImpl.buildEJB.addBuildEvents(Mockito.any(Long.class), Mockito.any(List.class))).thenThrow(new NotFoundException());

        MockHttpResponse response = invokePostRequest(BUILDS_BASE_URL + buildId + "/buildEvents", gson.toJson(buildEventViews),
                HttpServletResponse.SC_NOT_FOUND);
    }
    // TODO: TestCaseStats, testCoverages, buildFailures and metricsData tests!!
}
