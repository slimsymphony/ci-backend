package com.nokia.ci.api.resource;

import com.nokia.ci.client.model.ProductView;
import com.nokia.ci.client.model.ProjectVerificationConfView;
import com.nokia.ci.client.model.ProjectView;
import com.nokia.ci.client.model.VerificationView;
import com.nokia.ci.ejb.ProjectEJB;
import com.nokia.ci.ejb.model.Product;
import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.ProjectVerificationConf;
import com.nokia.ci.ejb.model.Verification;
import com.nokia.ci.ejb.exception.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import junit.framework.Assert;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test class for ProjectResource.
 * @author vrouvine
 */
public class ProjectResourceTest extends WebTestBase {

    private static final String PROJECT_BASE_URL = "/projects/";
    private static final String PROJECT_PRODUCTS_URL = PROJECT_BASE_URL + "%d/products";
    private static final String PROJECT_VERIFICATIONS_URL = PROJECT_BASE_URL + "%d/verifications";
    private static final String PROJECT_VERIFICATION_CONFS_URL = PROJECT_BASE_URL + "%d/verificationConfs";
    private static final long PROJECT_ID = 1;
    private static final int DEFAULT_SIZE = 5;

    @Test
    public void getProjects() throws Exception {
        List<Project> projects = createEntityList(Project.class, 2);
        populateProjects(projects);

        mockGetProjects(projects);

        MockHttpResponse response = invokeGetRequest(PROJECT_BASE_URL, HttpServletResponse.SC_OK);
        List<ProjectView> projectViews = gson.fromJson(response.getContentAsString(),
                getProjectViewCollectionType());
        verifyProjectViewList(projectViews, projects);
    }

    @Test
    public void getProject() throws Exception {
        Project project = createEntity(Project.class, PROJECT_ID);
        populateProject(project);
        mockGetProject(PROJECT_ID, project);

        MockHttpResponse response = invokeGetRequest(PROJECT_BASE_URL + PROJECT_ID,
                HttpServletResponse.SC_OK);
        ProjectView pv = gson.fromJson(response.getContentAsString(), ProjectView.class);
        verifyProjectView(pv, project);
    }

    @Test
    public void getProjectWithInvalidId() throws Exception {
        mockGetProject(PROJECT_ID, null);
        invokeGetRequest(PROJECT_BASE_URL + PROJECT_ID, HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void getProducts() throws Exception {
        List<Product> products = createEntityList(Product.class, DEFAULT_SIZE);
        populateProducts(products);

        mockGetProducts(PROJECT_ID, products);

        MockHttpResponse response = invokeGetRequest(String.format(PROJECT_PRODUCTS_URL, PROJECT_ID),
                HttpServletResponse.SC_OK);
        List<ProductView> productViews = gson.fromJson(response.getContentAsString(), getProductViewCollectionType());
        verifyProductViewList(productViews, products);
    }

    @Test
    public void getProductsWithInvalidId() throws Exception {
        ProjectEJB ejb = Mockito.mock(ProjectEJB.class);
        Mockito.when(ejb.getProducts(PROJECT_ID)).thenReturn(new ArrayList<Product>());
        registerProjectResource(ejb);
        MockHttpResponse response = invokeGetRequest(String.format(PROJECT_PRODUCTS_URL, PROJECT_ID),
                HttpServletResponse.SC_OK);
        List<ProductView> productViews = gson.fromJson(response.getContentAsString(), getProductViewCollectionType());
        Assert.assertTrue("Products should be empty", productViews.isEmpty());
    }

    @Test
    public void getVerifications() throws Exception {
        List<Verification> verifications = createEntityList(Verification.class, DEFAULT_SIZE);
        populateVerifications(verifications);

        mockGetVerifications(PROJECT_ID, verifications);

        MockHttpResponse response = invokeGetRequest(String.format(PROJECT_VERIFICATIONS_URL, PROJECT_ID),
                HttpServletResponse.SC_OK);
        List<VerificationView> verificationViews = gson.fromJson(response.getContentAsString(), getVerificationViewCollectionType());
        verifyVerificationViewList(verificationViews, verifications);
    }

    @Test
    public void getVerificationsWithInvalidId() throws Exception {
        ProjectEJB ejb = Mockito.mock(ProjectEJB.class);
        Mockito.when(ejb.getVerifications(PROJECT_ID)).thenReturn(new ArrayList<Verification>());
        registerProjectResource(ejb);
        MockHttpResponse response = invokeGetRequest(String.format(PROJECT_VERIFICATIONS_URL, PROJECT_ID),
                HttpServletResponse.SC_OK);
        List<VerificationView> verificationViews = gson.fromJson(response.getContentAsString(), getVerificationViewCollectionType());
        Assert.assertTrue("Verifications should be empty", verificationViews.isEmpty());
    }
    
    @Test
    public void getVerificationConfs() throws Exception {
        List<ProjectVerificationConf> confs = createEntityList(ProjectVerificationConf.class, DEFAULT_SIZE);
        populateProjectVerificationConfs(confs, createEntity(Project.class, PROJECT_ID));

        mockGetVerificationConfs(PROJECT_ID, confs);

        MockHttpResponse response = invokeGetRequest(String.format(PROJECT_VERIFICATION_CONFS_URL, PROJECT_ID),
                HttpServletResponse.SC_OK);
        List<ProjectVerificationConfView> confViews = gson.fromJson(response.getContentAsString(), getProjectVerificationConfViewCollectionType());
        verifyProjectVerificationConfViewList(confViews, confs);
    }

    @Test
    public void getVerificationConfsWithInvalidId() throws Exception {
        ProjectEJB ejb = Mockito.mock(ProjectEJB.class);
        Mockito.when(ejb.getVerificationConfs(PROJECT_ID)).thenReturn(new ArrayList<ProjectVerificationConf>());
        registerProjectResource(ejb);
        MockHttpResponse response = invokeGetRequest(String.format(PROJECT_VERIFICATION_CONFS_URL, PROJECT_ID), HttpServletResponse.SC_OK);
        List<ProjectVerificationConfView> confViews = gson.fromJson(response.getContentAsString(), getProjectVerificationConfViewCollectionType());
        Assert.assertTrue("Conf views should be empty", confViews.isEmpty());
    }

    /**
     * Register Project implementation with mocked EJB to dispatcher.
     * 
     * @param projectEJB Mocked EJB
     */
    private void registerProjectResource(ProjectEJB projectEJB) {
        ProjectResourceImpl projectResource = new ProjectResourceImpl();
        projectResource.projectEJB = projectEJB;
        dispatcher.getRegistry().addSingletonResource(projectResource);
    }

    private void mockGetProjects(List<Project> projects) {
        ProjectEJB projectEJB = Mockito.mock(ProjectEJB.class);
        Mockito.when(projectEJB.readAll()).thenReturn(projects);

        registerProjectResource(projectEJB);
    }

    private void mockGetProject(long id, Project project) throws 
            NotFoundException {
        
        ProjectEJB projectEJB = Mockito.mock(ProjectEJB.class);
        
        if(project == null) {
            Mockito.when(projectEJB.read(id)).thenThrow(
                    new NotFoundException());
        } else {
            Mockito.when(projectEJB.read(id)).thenReturn(project);
        }

        registerProjectResource(projectEJB);
    }

    private void mockGetProducts(long projectId, List<Product> products) {
        ProjectEJB projectEJB = Mockito.mock(ProjectEJB.class);
        Mockito.when(projectEJB.getProducts(projectId)).thenReturn(products);

        registerProjectResource(projectEJB);
    }

    private void mockGetVerifications(long projectId,
            List<Verification> verifications) {
        ProjectEJB projectEJB = Mockito.mock(ProjectEJB.class);
        Mockito.when(projectEJB.getVerifications(projectId)).thenReturn(verifications);

        registerProjectResource(projectEJB);
    }
    
    private void mockGetVerificationConfs(long projectId, 
            List<ProjectVerificationConf> confs) {
        ProjectEJB projectEJB = Mockito.mock(ProjectEJB.class);
        Mockito.when(projectEJB.getVerificationConfs(projectId)).thenReturn(confs);
        
        registerProjectResource(projectEJB);
    }
}