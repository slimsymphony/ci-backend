package com.nokia.ci.ui.it;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.jsfunit.api.InitialPage;
import org.jboss.jsfunit.jsfsession.JSFClientSession;
import org.jboss.jsfunit.jsfsession.JSFServerSession;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.nokia.ci.ejb.SysConfigEJB;
import com.nokia.ci.ejb.model.SysConfig;
import com.nokia.ci.ejb.model.SysConfigKey;
import org.jboss.arquillian.persistence.TransactionMode;
import org.jboss.arquillian.persistence.Transactional;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * JSFUnit test for loginAdmin page. Test is running inside the container.
 *
 * @author vrouvine
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@UsingDataSet(value={"users_dataset.yml"})
public class LoginPageIT {

    @Inject
    SysConfigEJB sysConfigEJB;

    @Deployment
    public static WebArchive createDeployment() {
        return UIDeploymentCreator.createJSFDeployment();
    }
    @Test
    @InitialPage("/login.xhtml")
    public void loginAdmin(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        // Test navigation to initial viewID
        Assert.assertEquals(server.getCurrentViewID(), "/login.xhtml");
        
        // Submit loginAdmin form
        client.setValue("usernameInput", "admin");
        client.setValue("passwordInput", "admin");
        client.click("loginButton");
        
        Assert.assertEquals("/secure/pages/myToolbox.xhtml", server.getCurrentViewID());
    }
    
    @Test
    @InitialPage("/login.xhtml")
    public void loginUser(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        // Test navigation to initial viewID
        Assert.assertEquals(server.getCurrentViewID(), "/login.xhtml");
        
        // Submit loginAdmin form
        client.setValue("usernameInput", "user");
        client.setValue("passwordInput", "user");
        client.click("loginButton");
        
        Assert.assertEquals("/secure/pages/myToolbox.xhtml", server.getCurrentViewID());
    }
    
    @Test
    @InitialPage("/login.xhtml")
    public void invalidLogin(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        // Test navigation to initial viewID
        Assert.assertEquals(server.getCurrentViewID(), "/login.xhtml");
        
        // Submit loginAdmin form
        client.setValue("usernameInput", "admin");
        client.setValue("passwordInput", "notarealpassword");
        client.click("loginButton");
        
        Assert.assertEquals("/login.xhtml", server.getCurrentViewID());
        Assert.assertTrue("There should be faces message!", client.getElement("loginMessages") != null);
    }

    @Test
    @InitialPage("/login.xhtml")
    @Transactional(TransactionMode.DISABLED)
    public void nextLogin(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        // Test navigation to initial viewID
        Assert.assertEquals(server.getCurrentViewID(), "/login.xhtml");
        
        // Set allow next users configuration
        SysConfig allowNextUsers = new SysConfig();
        allowNextUsers.setConfigKey(SysConfigKey.ALLOW_NEXT_USERS.toString());
        allowNextUsers.setConfigValue("TRUE");
        sysConfigEJB.create(allowNextUsers);

        // Submit loginAdmin form
        client.setValue("usernameInput", "e0123456");
        client.setValue("passwordInput", "e0123456");
        client.click("loginButton");
        
        Assert.assertEquals("/secure/pages/myToolbox.xhtml", server.getCurrentViewID());
    }
    
    @Test
    @InitialPage("/login.xhtml")
    public void invalidNextLogin(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        // Test navigation to initial viewID
        Assert.assertEquals(server.getCurrentViewID(), "/login.xhtml");
        
        // Submit loginAdmin form
        client.setValue("usernameInput", "e0123456");
        client.setValue("passwordInput", "e0123456");
        client.click("loginButton");
        
        Assert.assertEquals("/login.xhtml", server.getCurrentViewID());
        Assert.assertTrue("There should be faces message!", client.getElement("loginMessages") != null);
    }
    
}
