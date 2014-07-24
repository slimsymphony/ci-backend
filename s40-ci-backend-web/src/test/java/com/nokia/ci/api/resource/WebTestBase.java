package com.nokia.ci.api.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.nokia.ci.client.model.BranchView;
import com.nokia.ci.client.model.BuildEventView;
import com.nokia.ci.client.model.BuildView;
import com.nokia.ci.client.model.IncidentView;
import com.nokia.ci.client.model.JobVerificationConfView;
import com.nokia.ci.client.model.JobView;
import com.nokia.ci.client.model.MetricsBuildEventSetView;
import com.nokia.ci.client.model.ProductView;
import com.nokia.ci.client.model.ProjectVerificationConfView;
import com.nokia.ci.client.model.ProjectView;
import com.nokia.ci.client.model.VerificationView;
import com.nokia.ci.ejb.CITestBase;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Build;
import com.nokia.ci.ejb.model.Incident;
import com.nokia.ci.ejb.model.Job;
import com.nokia.ci.ejb.model.JobVerificationConf;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.Verification;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base test class for API unit test cases.
 *
 * @author vrouvine
 */
public abstract class WebTestBase extends CITestBase {

    /**
     * RestEasy mock dispatcher.
     */
    protected Dispatcher dispatcher;
    /**
     * JSon parser.
     */
    protected static Gson gson;

    @BeforeClass
    public static void setUpClass() throws Exception {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        gson = builder.create();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void before() {
        // Create new dispatcher for every test
        dispatcher = MockDispatcherFactory.createDispatcher();
    }

    /**
     * Generic type of JobView collection for GSon parsing.
     *
     * @return Generic type
     */
    protected static Type getJobViewCollectionType() {
        Type collectionType = new TypeToken<Collection<JobView>>() {
        }.getType();
        return collectionType;
    }

    /**
     * Generic type of BranchView collection for GSon parsing.
     *
     * @return Generic type
     */
    protected static Type getBranchViewCollectionType() {
        Type collectionType = new TypeToken<Collection<BranchView>>() {
        }.getType();
        return collectionType;
    }

    /**
     * Generic type of BuildView collection for GSon parsing.
     *
     * @return Generic type
     */
    protected static Type getBuildViewCollectionType() {
        Type collectionType = new TypeToken<Collection<BuildView>>() {
        }.getType();
        return collectionType;
    }

    /**
     * Generic type of ProjectView collection for GSon parsing.
     *
     * @return Generic type
     */
    protected static Type getProjectViewCollectionType() {
        Type collectionType = new TypeToken<Collection<ProjectView>>() {
        }.getType();
        return collectionType;
    }

    /**
     * Generic type of ProductView collection for GSon parsing.
     *
     * @return Generic type
     */
    protected static Type getProductViewCollectionType() {
        Type collectionType = new TypeToken<Collection<ProductView>>() {
        }.getType();
        return collectionType;
    }

    /**
     * Generic type of VerificationView collection for GSon parsing.
     *
     * @return Generic type
     */
    protected static Type getVerificationViewCollectionType() {
        Type collectionType = new TypeToken<Collection<VerificationView>>() {
        }.getType();
        return collectionType;
    }

    /**
     * Generic type of ProjectVerificationConfView collection for GSon parsing.
     *
     * @return Generic type
     */
    protected static Type getProjectVerificationConfViewCollectionType() {
        Type collectionType = new TypeToken<Collection<ProjectVerificationConfView>>() {
        }.getType();
        return collectionType;
    }

    /**
     * Generic type of JobVerificationConfView collection for GSon parsing.
     *
     * @return Generic type
     */
    protected static Type getJobVerificationConfViewCollectionType() {
        Type collectionType = new TypeToken<Collection<JobVerificationConfView>>() {
        }.getType();
        return collectionType;
    }

    protected static void verifyBuildViewList(List<BuildView> buildViewList,
            List<Build> buildList) {
        verifyLists(buildViewList, buildList);
        for (int i = 0; i < buildList.size(); i++) {
            verifyBuildView(buildViewList.get(i), buildList.get(i));
        }
    }

    protected static void verifyBuildView(BuildView bv, Build b) {
        Assert.assertNotNull("BuildView can not be null!", bv);
        Assert.assertNotNull("Build can not be null!", b);
        Assert.assertEquals("IDs should match!", bv.getId(), b.getId());
        Assert.assertEquals(bv.getRefSpec(), b.getRefSpec());
    }

    protected static void verifyBranchView(BranchView bv, Branch b) {
        Assert.assertNotNull("BranchView can not be null!", bv);
        Assert.assertNotNull("Branch can not be null!", b);
        Assert.assertEquals("IDs should match!", bv.getId(), b.getId());
        Assert.assertEquals("Names should match!", bv.getName(), b.getName());
        Assert.assertEquals(bv.getProjectId(), b.getProject() == null ? null : b.getProject().getId());
    }

    protected static void verifyBranchViewList(List<BranchView> branchViewList,
            List<Branch> branchList) {
        verifyLists(branchViewList, branchList);
        for (int i = 0; i < branchViewList.size(); i++) {
            verifyBranchView(branchViewList.get(i), branchList.get(i));
        }
    }

    protected static void verifyBranchView(BranchView branchView, BranchView result) {
        Assert.assertNotNull("BranchView can not be null!", branchView);
        Assert.assertNotNull("Result BranchView can not be null!", result);
        Assert.assertEquals("IDs should match!", result.getId(), result.getId());
        Assert.assertEquals("Names should match!", result.getName(), result.getName());
    }

    protected static void verifyJobView(JobView jv, Job j) {
        Assert.assertNotNull("JobView can not be null!", jv);
        Assert.assertNotNull("Job can not be null!", j);
        Assert.assertEquals("IDs should match!", jv.getId(), j.getId());
        Assert.assertEquals("Names should match!", jv.getName(), j.getName());
    }

    protected static void verifyIncidentView(IncidentView iv, Incident i) {
        Assert.assertNotNull("IncidentView can not be null!", iv);
        Assert.assertNotNull("Incident can not be null!", i);
        Assert.assertEquals("IDs should match!", iv.getId(), i.getId());
        Assert.assertEquals("Times should match!", iv.getTime(), i.getTime());
        Assert.assertEquals("Types should match!", iv.getType(), i.getType());
        Assert.assertEquals("Descriptions should match!", iv.getDescription(), i.getDescription());
        Assert.assertEquals("CheckUsers should match!", iv.getCheckUser(), i.getCheckUser());
        Assert.assertEquals("CheckTimes should match!", iv.getCheckTime(), i.getCheckTime());
    }

    protected static void verifyJobViewList(List<JobView> jobViewList, List<Job> jobList) {
        verifyLists(jobViewList, jobList);
        for (int i = 0; i < jobViewList.size(); i++) {
            verifyJobView(jobViewList.get(i), jobList.get(i));
        }
    }

    protected static void verifyProjectView(ProjectView pv, Project p) {
        Assert.assertNotNull("ProjectView can not be null!", pv);
        Assert.assertNotNull("Project can not be null!", p);
        Assert.assertEquals("IDs should match!", pv.getId(), p.getId());
        Assert.assertEquals("Names should match!", pv.getName(), p.getName());
    }

    protected static void verifyProjectViewList(List<ProjectView> projectViewList,
            List<Project> projectList) {
        verifyLists(projectViewList, projectList);
        for (int i = 0; i < projectViewList.size(); i++) {
            verifyProjectView(projectViewList.get(i), projectList.get(i));
        }
    }

    protected static void verifyProductView(ProductView pv, Product p) {
        Assert.assertNotNull("ProductView can not be null!", pv);
        Assert.assertNotNull("Product can not be null!", p);
        Assert.assertEquals("IDs should match!", pv.getId(), p.getId());
        Assert.assertEquals("Names should match!", pv.getName(), p.getName());
    }

    protected static void verifyProductViewList(List<ProductView> productViewList,
            List<Product> productList) {
        verifyLists(productViewList, productList);
        for (int i = 0; i < productViewList.size(); i++) {
            verifyProductView(productViewList.get(i), productList.get(i));
        }
    }

    protected static void verifyVerificationView(VerificationView vv, Verification v) {
        Assert.assertNotNull("VerificationView can not be null!", vv);
        Assert.assertNotNull("Verification can not be null!", v);
        Assert.assertEquals("IDs should match!", vv.getId(), v.getId());
        Assert.assertEquals("Names should match!", vv.getName(), v.getName());
    }

    protected static void verifyVerificationViewList(List<VerificationView> verificationViewList,
            List<Verification> verificationList) {
        verifyLists(verificationViewList, verificationList);
        for (int i = 0; i < verificationViewList.size(); i++) {
            verifyVerificationView(verificationViewList.get(i), verificationList.get(i));
        }
    }

    protected static void verifyProjectVerificationConfView(ProjectVerificationConfView confView,
            ProjectVerificationConf conf) {
        Assert.assertNotNull("ProjectVerificationConfView can not be null!", confView);
        Assert.assertNotNull("ProjectVerificationConf can not be null!", conf);
        Assert.assertEquals(confView.getId(), conf.getId());
        Assert.assertEquals(confView.getProjectId(), conf.getProject().getId());
        Assert.assertEquals(confView.getProductId(), conf.getProduct().getId());
        Assert.assertEquals(confView.getVerificationId(), conf.getVerification().getId());
    }

    protected static void verifyJobVerificationConfView(JobVerificationConfView confView,
            JobVerificationConf conf) {
        Assert.assertNotNull("JobVerificationConfView can not be null!", confView);
        Assert.assertNotNull("JobVerificationConf can not be null!", conf);
        Assert.assertEquals(confView.getId(), conf.getId());
        Assert.assertEquals(confView.getJobId(), conf.getJob().getId());
        Assert.assertEquals(confView.getProductId(), conf.getProduct().getId());
        Assert.assertEquals(confView.getVerificationId(), conf.getVerification().getId());
    }

    protected static void verifyJobVerificationConfViewList(List<JobVerificationConfView> confViews,
            List<JobVerificationConf> confs) {
        verifyLists(confViews, confs);
        for (int i = 0; i < confViews.size(); i++) {
            verifyJobVerificationConfView(confViews.get(i), confs.get(i));
        }
    }

    protected static void verifyProjectVerificationConfViewList(List<ProjectVerificationConfView> confViews,
            List<ProjectVerificationConf> confs) {
        verifyLists(confViews, confs);
        for (int i = 0; i < confViews.size(); i++) {
            verifyProjectVerificationConfView(confViews.get(i), confs.get(i));
        }
    }

    protected static MockHttpRequest createJsonPostRequest(String url,
            Object object) {
        return createJsonPostRequest(url, gson.toJson(object).getBytes());
    }

    protected static MockHttpRequest createJsonPostRequest(String url,
            byte[] content) {
        MockHttpRequest request = null;

        try {
            request = MockHttpRequest.post(url);
        } catch (URISyntaxException ex) {
        }

        request.contentType(MediaType.APPLICATION_JSON);
        request.content(content);

        return request;
    }

    protected static MockHttpRequest createJsonPutRequest(String url,
            Object object) {
        return createJsonPutRequest(url, gson.toJson(object).getBytes());
    }

    protected static MockHttpRequest createJsonPutRequest(String url,
            byte[] content) {
        MockHttpRequest request = null;

        try {
            request = MockHttpRequest.put(url);
        } catch (URISyntaxException ex) {
        }

        request.contentType(MediaType.APPLICATION_JSON);
        request.content(content);

        return request;
    }

    protected MockHttpResponse invokeRequest(MockHttpRequest request,
            int expectedStatusCode) {
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(expectedStatusCode, response.getStatus());
        return response;
    }

    /**
     * Invokes GET method of {@link MockHttpRequest} object with given URL and
     * checks response status code against given response code.
     *
     * @param Url URL to request
     * @param httpResponseCode wanted response code
     * @return {@link MockHttpResponse} object
     * @throws URISyntaxException If given URL is invalid
     */
    protected MockHttpResponse invokeGetRequest(String Url, int httpResponseCode) throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.get(Url);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(httpResponseCode, response.getStatus());
        return response;
    }

    /**
     * Invokes POST method of {@link MockHttpRequest} object with given URL and
     * checks response status code against given response code.
     *
     * @param Url URL to request
     * @param content Body content of POST request
     * @param httpResponseCode wanted response code
     * @return {@link MockHttpResponse} object
     * @throws URISyntaxException If given URL is invalid
     */
    protected MockHttpResponse invokePostRequest(String Url, String content, int httpResponseCode) throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post(Url);
        request.contentType(MediaType.APPLICATION_JSON);
        request.content(content.getBytes());
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(httpResponseCode, response.getStatus());
        return response;
    }

    /**
     * Invokes PUT method of {@link MockHttpRequest} object with given URL and
     * content. Method also checks that response status code matches to given
     * status code.
     *
     * @param Url URL to request
     * @param content Body content of PUT request
     * @param httpResponseCode Wanted response status code
     * @return {@link MockHttpResponse} object
     * @throws URISyntaxException If given URL is invalid
     */
    protected MockHttpResponse invokePutRequest(String Url, String content, int httpResponseCode) throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.put(Url);
        request.contentType(MediaType.APPLICATION_JSON);
        request.content(content.getBytes());
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(httpResponseCode, response.getStatus());
        return response;
    }

    protected void populateMetricsBuildEventSet(MetricsBuildEventSetView view) {
        view.setBuildId(1L);
        view.setExecutor("executor");
        view.setEvents(new ArrayList<BuildEventView>());
    }

    protected List<BuildEventView> generateBuildEventViews(int amount, String phase) {
        List<BuildEventView> events = new ArrayList<BuildEventView>();
        for (int i = 0; i < amount; i++) {
            BuildEventView v = new BuildEventView();
            v.setName("event" + i);
            v.setDescription("desc" + i);
            v.setPhase(phase);
            Date date = new Date();
            v.setTimestamp(date.getTime());
            events.add(v);
        }
        return events;
    }
}
