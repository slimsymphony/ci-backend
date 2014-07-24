/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.resource;

import com.nokia.ci.ejb.UserFileEJB;
import com.nokia.ci.ejb.exception.NotFoundException;
import com.nokia.ci.ejb.model.AccessScope;
import com.nokia.ci.ejb.model.UserFile;
import java.net.URISyntaxException;
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
public class FileResourceTest extends WebTestBase {

    private static final String FILE_BASE_URL = "/files/";
    private FileResourceImpl fileResourceImpl;

    @Before
    @Override
    public void before() {
        super.before();
        fileResourceImpl = new FileResourceImpl();
        fileResourceImpl.userFileEJB = Mockito.mock(UserFileEJB.class);
        dispatcher.getRegistry().addSingletonResource(fileResourceImpl);
    }

    @Test
    public void restFileForbiddenTest() throws Exception {
        UserFile file = createEntity(UserFile.class, 1L);
        file.setAccessScope(AccessScope.SYSTEM);
        file.setUuid("12345678");

        mockUserFile(file);

        MockHttpResponse response = getRestFile("12345678", HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void restFileNotFoundTest() throws Exception {
        UserFile file = createEntity(UserFile.class, 1L);
        file.setAccessScope(AccessScope.SYSTEM);
        file.setUuid("12345678");

        mockUserFileNotFound(file);

        MockHttpResponse response = getRestFile("12345678", HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void systemFileForbiddenTest() throws Exception {
        UserFile file = createEntity(UserFile.class, 1L);
        file.setAccessScope(AccessScope.REST);
        file.setUuid("12345678");

        mockUserFile(file);

        MockHttpResponse response = getSystemFile("12345678", HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void systemFileNotFoundTest() throws Exception {
        UserFile file = createEntity(UserFile.class, 1L);
        file.setAccessScope(AccessScope.REST);
        file.setUuid("12345678");

        mockUserFileNotFound(file);

        MockHttpResponse response = getSystemFile("12345678", HttpServletResponse.SC_NOT_FOUND);
    }

    private void mockUserFile(UserFile userFile) throws NotFoundException {
        Mockito.when(fileResourceImpl.userFileEJB.getUserFileByUuid(userFile.getUuid())).thenReturn(userFile);
    }

    private void mockUserFileNotFound(UserFile userFile) throws NotFoundException {
        Mockito.when(fileResourceImpl.userFileEJB.getUserFileByUuid(userFile.getUuid())).thenThrow(new NotFoundException());
    }

    private MockHttpResponse getRestFile(String uuid, int responseCode)
            throws URISyntaxException, NotFoundException {

        MockHttpRequest request = MockHttpRequest.get(FILE_BASE_URL + uuid);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(responseCode, response.getStatus());
        return response;
    }

    private MockHttpResponse getSystemFile(String uuid, int responseCode)
            throws URISyntaxException, NotFoundException {

        MockHttpRequest request = MockHttpRequest.get(FILE_BASE_URL + "system/" + uuid);
        MockHttpResponse response = new MockHttpResponse();
        dispatcher.invoke(request, response);
        Assert.assertEquals(responseCode, response.getStatus());
        return response;
    }
}
