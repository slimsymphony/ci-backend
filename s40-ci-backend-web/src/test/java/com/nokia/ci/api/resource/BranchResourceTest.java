package com.nokia.ci.api.resource;

import static com.nokia.ci.api.resource.WebTestBase.gson;
import com.nokia.ci.client.model.BranchView;
import com.nokia.ci.ejb.BranchEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.Branch;
import com.nokia.ci.ejb.model.Project;
import java.net.URISyntaxException;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test cases for BranchResource
 *
 * @author vrouvine
 */
public class BranchResourceTest extends WebTestBase {

    private static final String BRANCH_BASE_URL = "/branches/";
    private static final long BRANCH_ID = 1;

    @Test
    public void createBranch() throws Exception {
        BranchResourceImpl branchResource = new BranchResourceImpl();
        branchResource.branchEJB = Mockito.mock(BranchEJB.class);
        dispatcher.getRegistry().addSingletonResource(branchResource);

        Long projectId = 1L;
        Project p = createEntity(Project.class, projectId);
        Branch b = createEntity(Branch.class, BRANCH_ID);
        populateBranch(b, p);

        BranchView bv = new BranchView();
        bv.copyValuesFrom(b);

        MockHttpResponse response = invokePostRequest(BRANCH_BASE_URL, gson.toJson(bv), HttpServletResponse.SC_CREATED);
    }

    @Test
    public void getBranch() throws Exception {
        Long projectId = 1L;
        Project p = createEntity(Project.class, projectId);
        Branch b = createEntity(Branch.class, BRANCH_ID);
        populateBranch(b, p);

        mockGetBranch(BRANCH_ID, b);

        MockHttpResponse response = invokeGetRequest(BRANCH_BASE_URL + BRANCH_ID, HttpServletResponse.SC_OK);
        BranchView bv = gson.fromJson(response.getContentAsString(), BranchView.class);
        verifyBranchView(bv, b);
    }

    @Test
    public void getBranchWithInvalidId() throws Exception {
        mockGetBranch(BRANCH_ID, null);
        invokeGetRequest(BRANCH_BASE_URL + BRANCH_ID, HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void getBranches() throws Exception {
        Long projectId = 1L;
        Project p = createEntity(Project.class, projectId);
        List<Branch> branches = createEntityList(Branch.class, 2);
        populateBranches(branches, p);

        mockGetBranches(branches);
        MockHttpResponse response = invokeGetRequest(BRANCH_BASE_URL, HttpServletResponse.SC_OK);
        List<BranchView> bvList = gson.fromJson(response.getContentAsString(),
                getBranchViewCollectionType());
        verifyBranchViewList(bvList, branches);
    }

    @Test
    public void updateBranch() throws URISyntaxException, NotFoundException {
        final String contentType = MediaType.APPLICATION_JSON;

        Branch b = createEntity(Branch.class, BRANCH_ID);
        populateBranch(b);

        BranchView bv = new BranchView();
        bv.copyValuesFrom(b);

        mockUpdateBranch(b);

        MockHttpResponse response = updateBranch(BRANCH_ID, contentType,
                gson.toJson(bv).getBytes(), HttpServletResponse.SC_OK);
        BranchView result = gson.fromJson(response.getContentAsString(),
                BranchView.class);
        verifyBranchView(bv, result);
    }

    @Test
    public void updateBranchWithInvalidContentType() throws URISyntaxException,
            NotFoundException {

        final String contentType = MediaType.APPLICATION_OCTET_STREAM;

        Branch b = createEntity(Branch.class, BRANCH_ID);

        BranchView bv = new BranchView();
        bv.copyValuesFrom(b);

        mockUpdateBranch(b);

        updateBranch(BRANCH_ID, contentType, gson.toJson(bv).getBytes(),
                HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    public void updateBranchWithInvalidContent() throws URISyntaxException,
            NotFoundException {

        final String contentType = MediaType.APPLICATION_JSON;

        Branch b = createEntity(Branch.class, BRANCH_ID);

        mockUpdateBranch(b);

        updateBranch(BRANCH_ID, contentType, "Invalid content".getBytes(),
                HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void updateNonExistingBranch() throws URISyntaxException,
            NotFoundException {

        final String contentType = MediaType.APPLICATION_JSON;

        Branch b = createEntity(Branch.class, BRANCH_ID);
        BranchView bv = new BranchView();
        bv.copyValuesFrom(b);

        mockUpdateBranch(null);

        updateBranch(12324, contentType, gson.toJson(bv).getBytes(),
                HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Register BranchResource implementation with mocked EJB to dispatcher.
     *
     * @param branchEJB Mocked EJB
     */
    private void registerBranchResource(BranchEJB branchEJB) {
        BranchResourceImpl branchResource = new BranchResourceImpl();
        branchResource.branchEJB = branchEJB;
        dispatcher.getRegistry().addSingletonResource(branchResource);
    }

    private MockHttpResponse updateBranch(long id,
            String contentType, byte[] content, int responseCode)
            throws URISyntaxException, NotFoundException {

        MockHttpRequest request = MockHttpRequest.put(BRANCH_BASE_URL + id);
        request.contentType(contentType).content(content);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(responseCode, response.getStatus());
        return response;
    }

    private void mockGetBranches(List<Branch> branches) {
        BranchEJB branchEJB = Mockito.mock(BranchEJB.class);
        Mockito.when(branchEJB.readAll()).thenReturn(branches);

        registerBranchResource(branchEJB);
    }

    private void mockGetBranch(long id, Branch b) throws NotFoundException {
        BranchEJB branchEJB = Mockito.mock(BranchEJB.class);

        if (b == null) {
            Mockito.when(branchEJB.read(id)).thenThrow(
                    new NotFoundException());
        } else {
            Mockito.when(branchEJB.read(id)).thenReturn(b);
        }

        registerBranchResource(branchEJB);
    }

    private void mockUpdateBranch(Branch b) throws NotFoundException {
        BranchEJB branchEJB = Mockito.mock(BranchEJB.class);
        if (b == null) {
            Mockito.when(branchEJB.read(Mockito.anyLong())).thenThrow(
                    new NotFoundException());
            Mockito.when(branchEJB.update((Branch) Mockito.anyObject())).thenThrow(
                    new NotFoundException());
        } else {
            Mockito.when(branchEJB.read(b.getId())).thenReturn(b);
            Mockito.when(branchEJB.update(b)).thenReturn(b);
        }

        registerBranchResource(branchEJB);
    }
}
