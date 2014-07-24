package com.nokia.ci.integration.rest;

import com.nokia.ci.client.rest.FileResource;
import com.nokia.ci.integration.CITestBase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * HTTP API tests for File resource.
 *
 * @author larryang
 */
@Ignore
public class FileResourceIT extends CITestBase {

    private static FileResource proxy;

    @BeforeClass
    public static void setUpClass() {
        proxy = ProxyFactory.create(FileResource.class, API_BASE_URL);
    }

    @Test
    public void getFile() throws Exception {
        ClientResponse response = (ClientResponse) proxy.getRestFile("52a2bbec-c1ce-4e47-a3d2-5f1581e40eb9");
        Assert.assertEquals(ClientResponse.Status.OK.getStatusCode(), response.getStatus());

        File file = (File) (response.getEntity(File.class));
        InputStreamReader readStream = new InputStreamReader(new FileInputStream(file));
        BufferedReader reader = new BufferedReader(readStream);
        String content = "";
        String line;
        while ((line = reader.readLine()) != null) {
            content += line;
        }
        Assert.assertNotSame("", content);
    }
}
