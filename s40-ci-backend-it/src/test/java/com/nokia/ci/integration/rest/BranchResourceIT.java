package com.nokia.ci.integration.rest;

import com.nokia.ci.integration.CITestBase;
import com.nokia.ci.client.model.BranchView;
import com.nokia.ci.client.rest.BranchResource;
import java.util.List;
import junit.framework.Assert;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.util.GenericType;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP API tests for branch resource.
 * 
 * @author vrouvine
 */
@Ignore
public class BranchResourceIT extends CITestBase {
    
    private static Logger log = LoggerFactory.getLogger(BranchResourceIT.class);

    private static BranchResource proxy;

    @BeforeClass
    public static void setUpClass() {
        proxy = ProxyFactory.create(BranchResource.class, API_BASE_URL);
    }

    @Test
    public void createBranch() throws Exception {
        String name = "New branch";
        BranchView b = new BranchView();
        b.setName(name);
        ClientResponse r = (ClientResponse) proxy.createBranch(b);
        Assert.assertNotNull(r.getLocation());
        String resultLocation = r.getLocation().getHref();
        Assert.assertEquals(ClientResponse.Status.CREATED.getStatusCode(), r.getStatus());

        String resultId = parseIdFromLocationUrl(resultLocation);
        r = (ClientResponse) proxy.getBranch(Long.valueOf(resultId));
        BranchView created = (BranchView) r.getEntity(BranchView.class);
        Assert.assertEquals(name, created.getName());
    }

    @Test
    public void getBranches() throws Exception {
        ClientResponse response = (ClientResponse) proxy.getBranches();
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());
        GenericType genericType = (new GenericType<List<BranchView>>() {
        });
        List<BranchView> branches = (List<BranchView>) response.getEntity(genericType);
        Assert.assertEquals(branches.size(), 
                DatasetTool.getRowCount(dataset, DatasetTool.BRANCH_TABLE_NAME));
        for(int i = 0; i < branches.size(); i++) {
            String projectId = DatasetTool.getDatasetValue(
                dataset, DatasetTool.BRANCH_TABLE_NAME, i, "PROJECT_ID");
            Assert.assertEquals(Long.valueOf(projectId), branches.get(i).getProjectId());
        }
    }

    @Test
    public void getBranch() throws Exception {
        String id = DatasetTool.getDatasetValue(
                dataset, DatasetTool.BRANCH_TABLE_NAME, 0, "ID");
        String name = DatasetTool.getDatasetValue(
                dataset, DatasetTool.BRANCH_TABLE_NAME, 0, "NAME");
        String projectId = DatasetTool.getDatasetValue(
                dataset, DatasetTool.BRANCH_TABLE_NAME, 0, "PROJECT_ID");
        log.info("Branch name: " + name);
        ClientResponse response = (ClientResponse) proxy.getBranch(Long.valueOf(id));
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());
        BranchView branch = (BranchView) response.getEntity(BranchView.class);
        Assert.assertNotNull(branch);
        Assert.assertEquals(Long.valueOf(id), branch.getId());
        Assert.assertEquals(name, branch.getName());
        Assert.assertEquals(Long.valueOf(projectId), branch.getProjectId());
    }

    @Test
    public void updateBranch() throws Exception {
        final String name = "New branch 2";
        BranchView b = new BranchView();
        b.setName(name);
        ClientResponse r = (ClientResponse) proxy.createBranch(b);
        Assert.assertEquals(ClientResponse.Status.CREATED.getStatusCode(), r.getStatus());
        String createdId = parseIdFromLocationUrl(r.getLocation().getHref());

        String changedName = "Changed";
        b.setName(changedName);
        r = (ClientResponse) proxy.updateBranch(Long.valueOf(createdId), b);
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), r.getStatus());
        BranchView view = (BranchView) r.getEntity(BranchView.class);
        Assert.assertEquals(Long.valueOf(createdId), view.getId());
        Assert.assertEquals(changedName, view.getName());
    }
}
