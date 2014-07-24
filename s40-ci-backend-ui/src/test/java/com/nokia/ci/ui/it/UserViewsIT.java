package com.nokia.ci.ui.it;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.ApplyScriptAfter;
import org.jboss.arquillian.persistence.ApplyScriptBefore;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.jsfunit.api.InitialPage;
import org.jboss.jsfunit.api.InitialRequest;
import org.jboss.jsfunit.framework.WebConversationFactory;
import org.jboss.jsfunit.jsfsession.JSFClientSession;
import org.jboss.jsfunit.jsfsession.JSFServerSession;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test class for testing user view page loading. Tests are just rendering JSF
 * pages and their should be loaded without errors. No functional testing
 * involved.
 *
 * @author vrouvine
 */
@RunWith(Arquillian.class)
@ApplyScriptBefore(value = {"sequences.sql"})
@ApplyScriptAfter(value = {"set_referential_integrity_false.sql"})
@UsingDataSet(value = {"users_dataset.yml", "user_views_dataset.yml"})
public class UserViewsIT {

    private String warURL;

    @Deployment
    public static WebArchive createDeployment() {
        return UIDeploymentCreator.createJSFDeployment();
    }

    @Before
    public void init() {
        warURL = WebConversationFactory.getWARURL();
    }

    @Test
    @InitialPage("/secure/pages/projects.xhtml")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void projectsPage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/projects.xhtml", client.getContentPage().getUrl().toString());
    }

    @Test
    @InitialPage("/secure/pages/projectDetails.xhtml?projectId=1")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void projectDetailsPage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/projectDetails.xhtml?projectId=1", client.getContentPage().getUrl().toString());
    }

    @Test
    @InitialPage("/secure/pages/verificationDetails.xhtml?verificationId=1")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void verificationDetailsPage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/verificationDetails.xhtml?verificationId=1", client.getContentPage().getUrl().toString());
    }

    @Test
    @InitialPage("/secure/pages/verificationEditor.xhtml?verificationId=1")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void verificationEditorPage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/verificationEditor.xhtml?verificationId=1", client.getContentPage().getUrl().toString());
    }

    @Test
    @InitialPage("/secure/pages/verificationShare.xhtml?verificationId=1")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void verificationSharePage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/verificationShare.xhtml?verificationId=1", client.getContentPage().getUrl().toString());
    }

    @Test
    @InitialPage("/secure/pages/verificationStart.xhtml?verificationId=1")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void verificationStartPage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/verificationStart.xhtml?verificationId=1", client.getContentPage().getUrl().toString());
    }
    
    @Test
    @InitialPage("/secure/pages/buildDetails.xhtml?buildId=1")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void buildDetailsPage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/buildDetails.xhtml?buildId=1", client.getContentPage().getUrl().toString());
    }

    @Test
    @InitialPage("/secure/pages/gerritTrigger.xhtml?projectId=1")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void gerritTriggerPage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/gerritTrigger.xhtml?projectId=1", client.getContentPage().getUrl().toString());
    }

    @Test
    @InitialPage("/secure/pages/info.xhtml")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void infoPage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/info.xhtml", client.getContentPage().getUrl().toString());
    }
    
    @Test
    @InitialPage("/secure/pages/help.xhtml?page=projects")
    @InitialRequest(LoginInitialRequestStrategy.class)
    public void helpPage(JSFServerSession server, JSFClientSession client) throws IOException {
        Assert.assertEquals(HttpServletResponse.SC_OK, client.getContentPage().getWebResponse().getStatusCode());
        Assert.assertEquals(warURL + "/secure/pages/help.xhtml?page=projects", client.getContentPage().getUrl().toString());
    }
}
