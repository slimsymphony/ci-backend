package com.nokia.ci.integration.rest;

import com.nokia.ci.client.model.ProductView;
import com.nokia.ci.client.model.ProjectVerificationConfView;
import com.nokia.ci.client.model.ProjectView;
import com.nokia.ci.client.model.VerificationView;
import com.nokia.ci.client.rest.ProjectResource;
import com.nokia.ci.integration.CITestBase;
import dataset.RowFilter;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP API tests for project resource.
 * @author vrouvine
 */
@Ignore
public class ProjectResourceIT extends CITestBase {

    private static Logger log = LoggerFactory.getLogger(ProjectResourceIT.class);
    private static ProjectResource proxy;

    @BeforeClass
    public static void setUpClass() {
        proxy = ProxyFactory.create(ProjectResource.class, API_BASE_URL);
    }

    @Test
    public void getProjects() throws Exception {
        ClientResponse response = (ClientResponse) proxy.getProjects();
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        GenericType genericType = (new GenericType<List<ProjectView>>() {
        });
        List<ProjectView> projects = (List<ProjectView>) response.getEntity(genericType);
        Assert.assertEquals(projects.size(),
                DatasetTool.getRowCount(dataset, DatasetTool.PROJECT_TABLE_NAME));
    }

    @Test
    public void getProject() throws Exception {
        String id = DatasetTool.getDatasetValue(
                dataset, DatasetTool.PROJECT_TABLE_NAME, 0, "ID");
        String name = DatasetTool.getDatasetValue(
                dataset, DatasetTool.PROJECT_TABLE_NAME, 0, "NAME");
        log.info("Project name: {}, id: {}", name, id);
        ClientResponse response = (ClientResponse) proxy.getProject(Long.valueOf(id));
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        ProjectView project = (ProjectView) response.getEntity(ProjectView.class);
        Assert.assertNotNull(project);
        Assert.assertEquals(Long.valueOf(id), project.getId());
        Assert.assertEquals(name, project.getName());
    }

    @Test
    public void getProjectWithInvalidId() throws Exception {
        ClientResponse response = (ClientResponse) proxy.getProject(Long.MAX_VALUE);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    @Test
    public void getProducts() throws Exception {
        GenericType genericType = (new GenericType<List<ProductView>>() {
        });
        String id = DatasetTool.getDatasetValue(
                dataset, DatasetTool.PROJECT_TABLE_NAME, 0, "ID");
        ClientResponse response = (ClientResponse) proxy.getProducts(Long.valueOf(id));
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        List<ProductView> products = (List<ProductView>) response.getEntity(genericType);
        Assert.assertNotNull(products);
        ITable filteredTable = new RowFilterTable(dataset.getTable(DatasetTool.PROJECT_PRODUCT_TABLE_NAME),
                new RowFilter(id, "PROJECTS_ID"));
        Assert.assertEquals(filteredTable.getRowCount(), products.size());
        for (int i = 0; i < products.size(); i++) {
            long value = products.get(i).getId();
            String s = (String) filteredTable.getValue(i, "PRODUCTS_ID");
            long ref = Long.valueOf(s);
            Assert.assertEquals("Ids should match!", ref, value);
        }
    }

    @Test
    public void getProductsWithInvalidId() throws Exception {
        ClientResponse response = (ClientResponse) proxy.getProducts(Long.MIN_VALUE);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    @Test
    public void getVerifications() throws Exception {
        GenericType genericType = (new GenericType<List<VerificationView>>() {
        });
        String id = DatasetTool.getDatasetValue(
                dataset, DatasetTool.PROJECT_TABLE_NAME, 0, "ID");
        ClientResponse response = (ClientResponse) proxy.getVerifications(Long.valueOf(id));
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        List<VerificationView> verifications = (List<VerificationView>) response.getEntity(genericType);
        Assert.assertNotNull(verifications);
        ITable filteredTable = new RowFilterTable(dataset.getTable(DatasetTool.PROJECT_VERIFICATION_TABLE_NAME),
                new RowFilter(id, "PROJECTS_ID"));
        Assert.assertEquals(filteredTable.getRowCount(), verifications.size());
        for (int i = 0; i < verifications.size(); i++) {
            long value = verifications.get(i).getId();
            String s = (String) filteredTable.getValue(i, "VERIFICATIONS_ID");
            long ref = Long.valueOf(s);
            Assert.assertEquals("Ids should match!", ref, value);
        }
    }

    @Test
    public void getVerificationsWithInvalidId() throws Exception {
        ClientResponse response = (ClientResponse) proxy.getVerifications(Long.MIN_VALUE);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    @Test
    public void getVerificationConfs() throws Exception {
        GenericType genericType = (new GenericType<List<ProjectVerificationConfView>>() {
        });
        String projectId = DatasetTool.getDatasetValue(
                dataset, DatasetTool.PROJECT_TABLE_NAME, 0, "ID");
        ClientResponse response = (ClientResponse) proxy.getVerificationConfs(Long.valueOf(projectId));
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        List<ProjectVerificationConfView> confs = (List<ProjectVerificationConfView>) response.getEntity(genericType);
        Assert.assertNotNull(confs);
        ITable filteredTable = new RowFilterTable(dataset.getTable(DatasetTool.PROJECT_VERIFICATION_CONF_TABLE_NAME),
                new RowFilter(projectId, "PROJECT_ID"));
        Assert.assertEquals(filteredTable.getRowCount(), confs.size());
        for (int i = 0; i < confs.size(); i++) {
            ProjectVerificationConfView conf = confs.get(i);
            long id = Long.valueOf((String) filteredTable.getValue(i, "ID"));
            long productId = Long.valueOf((String) filteredTable.getValue(i, "PRODUCT_ID"));
            long verificationId = Long.valueOf((String) filteredTable.getValue(i, "VERIFICATION_ID"));
            Assert.assertEquals("Ids should match!", id, conf.getId().longValue());
            Assert.assertEquals("Project id should match!", Long.valueOf(projectId).longValue(), conf.getProjectId().longValue());
            Assert.assertEquals("Product id should match!", productId, conf.getProductId().longValue());
            Assert.assertEquals("Verification id should match!", verificationId, conf.getVerificationId().longValue());
        }
    }

    @Test
    public void getVerificationConfsWithInvalidId() throws Exception {
        ClientResponse response = (ClientResponse) proxy.getVerifications(Long.MIN_VALUE);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }
}
