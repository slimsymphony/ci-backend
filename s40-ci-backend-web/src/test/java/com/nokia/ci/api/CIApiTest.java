package com.nokia.ci.api;

import com.nokia.ci.api.resource.WebTestBase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author miikka
 */
public class CIApiTest extends WebTestBase {

    @Test
    public void testApi() {
        CIApi ciApi = new CIApi();
        Assert.assertNotNull(ciApi);
    }
}
