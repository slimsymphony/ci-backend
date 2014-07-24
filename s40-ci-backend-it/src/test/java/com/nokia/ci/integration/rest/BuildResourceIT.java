package com.nokia.ci.integration.rest;

import com.nokia.ci.client.model.BuildEventView;
import com.nokia.ci.client.model.BuildFailureView;
import com.nokia.ci.client.model.BuildView;
import com.nokia.ci.client.model.MemConsumptionView;
import com.nokia.ci.client.model.MetricsDatasetView;
import com.nokia.ci.client.model.TestCaseStatView;
import com.nokia.ci.client.model.TestCoverageView;
import com.nokia.ci.client.rest.BuildResource;
import com.nokia.ci.ejb.model.BuildEventPhase;
import com.nokia.ci.integration.CITestBase;
import dataset.RowFilter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
 * HTTP API tests for Build resource.
 *
 * @author vrouvine
 */
public class BuildResourceIT extends CITestBase {

    private static BuildResource proxy;

    @BeforeClass
    public static void setUpClass() {
        proxy = ProxyFactory.create(BuildResource.class, API_BASE_URL);
    }

    @Ignore
    @Test
    public void getBuild() throws Exception {
        String id = DatasetTool.getDatasetValue(
                dataset, DatasetTool.BUILD_TABLE_NAME, 0, "ID");
        ClientResponse response = (ClientResponse) proxy.getBuild(
                Long.valueOf(id));
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());
        BuildView build = (BuildView) response.getEntity(BuildView.class);
        Assert.assertNotNull(build);
        Assert.assertEquals(Long.valueOf(id), build.getId());        
    }    
    
    @Ignore
    @Test
    public void getBuildsByRefSpec() throws Exception {
        GenericType genericType = (new GenericType<List<BuildView>>() {
        });
        String refSpec = DatasetTool.getDatasetValue(dataset, DatasetTool.BUILD_TABLE_NAME, 0, "REFSPEC");
        ClientResponse response = (ClientResponse) proxy.getBuilds(refSpec);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        List<BuildView> builds = (List<BuildView>) response.getEntity(genericType);
        ITable filteredTable = new RowFilterTable(dataset.getTable(DatasetTool.BUILD_TABLE_NAME),
                new RowFilter(refSpec, "REFSPEC"));
        Assert.assertEquals(filteredTable.getRowCount(), builds.size());
        Assert.assertTrue(builds.size() > 0);
        for(BuildView build : builds) {
            Assert.assertEquals(build.getRefSpec(), refSpec);
        }
    }
    
    @Ignore
    @Test
    public void getBuildsByEmptyRefSpec() {
        ClientResponse response = (ClientResponse) proxy.getBuilds("");
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }
    
    @Ignore
    @Test
    public void getBuildsByNullRefSpec() {
        ClientResponse response = (ClientResponse) proxy.getBuilds(null);
        Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
    }

    @Ignore
    @Test
    public void getBuildsByInvalidRefSpec() {
        GenericType genericType = (new GenericType<List<BuildView>>() {
        });
        String refSpec = "not found";
        ClientResponse response = (ClientResponse) proxy.getBuilds(refSpec);
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        List<BuildView> builds = (List<BuildView>) response.getEntity(genericType);
        Assert.assertEquals(builds.size(), 0);
    }
    
    @Test
    public void postBuildEvent(){

        List<BuildEventView> buildEventViews = new ArrayList<BuildEventView>();
        
        BuildEventView buildEventView = new BuildEventView();
        buildEventView.setName("Test event");
        buildEventView.setDescription("Test description");
        buildEventView.setPhase(BuildEventPhase.START.toString());
        buildEventView.setTimestamp(System.currentTimeMillis());
        buildEventViews.add(buildEventView);
        
        String id = DatasetTool.getDatasetValue(
                dataset, DatasetTool.BUILD_TABLE_NAME, 0, "ID");
        
        ClientResponse response = (ClientResponse) proxy.createBuildEvents(Long.valueOf(id), buildEventViews);
        
        Assert.assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
    }
    
    @Test
    public void postMetricsData(){
        
        List<BuildEventView> buildEventViews = new ArrayList<BuildEventView>();
        List<MemConsumptionView> memConsumptionViews = new ArrayList<MemConsumptionView>();
        List<TestCaseStatView> testCaseStatViews = new ArrayList<TestCaseStatView>();
        List<TestCoverageView> testCoverageViews = new ArrayList<TestCoverageView>();
        List<BuildFailureView> buildFailureViews = new ArrayList<BuildFailureView>();
        
        BuildEventView buildEventView = new BuildEventView();
        buildEventView.setName("Test event");
        buildEventView.setDescription("Test description");
        buildEventView.setPhase(BuildEventPhase.START.toString());
        buildEventView.setTimestamp(System.currentTimeMillis());
        buildEventViews.add(buildEventView);
        
        MemConsumptionView memConsumptionView = new MemConsumptionView();
        memConsumptionView.setComponentName("Test component");
        memConsumptionView.setRam(10f);
        memConsumptionView.setRom(15f);
        memConsumptionViews.add(memConsumptionView);
        
        TestCaseStatView testCaseStatView = new TestCaseStatView();
        testCaseStatView.setComponentName("Test component 2");
        testCaseStatView.setPassedCount(80);
        testCaseStatView.setFailedCount(8);
        testCaseStatView.setNaCount(2);
        testCaseStatView.setTotalCount(100);
        testCaseStatViews.add(testCaseStatView);
        
        TestCoverageView testCoverageView = new TestCoverageView();
        testCoverageView.setComponentName("Test component 2");
        testCoverageView.setCondCov(0.8f);
        testCoverageView.setStmtCov(0.65f);
        testCoverageViews.add(testCoverageView);
        
        BuildFailureView buildFailureView = new BuildFailureView();
        buildFailureView.setTestcaseName("Failed test case 1");
        buildFailureView.setType("Test failure");
        buildFailureView.setMessage("Test failure message");
        buildFailureView.setRelativePath("/path/to/testcase");
        buildFailureViews.add(buildFailureView);
        
        MetricsDatasetView metricsDatasetView = new MetricsDatasetView();
        
        String id = DatasetTool.getDatasetValue(
                dataset, DatasetTool.BUILD_TABLE_NAME, 0, "ID");
        
        metricsDatasetView.setBuildId(Long.valueOf(id));
        metricsDatasetView.setExecutor("TestExecutor");
        metricsDatasetView.setBuildEventViews(buildEventViews);
        metricsDatasetView.setMemConsumptions(memConsumptionViews);
        metricsDatasetView.setTestCaseStats(testCaseStatViews);
        metricsDatasetView.setTestCoverages(testCoverageViews);
        metricsDatasetView.setBuildFailures(buildFailureViews);
        
        ClientResponse response = (ClientResponse) proxy.createMetricsData(Long.valueOf(id), metricsDatasetView);
        
        Assert.assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
    }
}
