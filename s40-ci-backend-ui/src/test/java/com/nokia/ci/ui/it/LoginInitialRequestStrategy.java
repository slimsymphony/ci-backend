package com.nokia.ci.ui.it;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import org.jboss.jsfunit.framework.SimpleInitialRequestStrategy;
import org.jboss.jsfunit.framework.WebClientSpec;
import org.jboss.jsfunit.framework.WebConversationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs login before actual initial page navigation.
 * 
 * @author vrouvine
 */
public class LoginInitialRequestStrategy extends SimpleInitialRequestStrategy {
    
    private static final Logger log = LoggerFactory.getLogger(LoginInitialRequestStrategy.class);
    
    @Override
    public Page doInitialRequest(WebClientSpec wcSpec) throws IOException {
        log.info("Do login...");
        WebClient webClient = wcSpec.getWebClient();
        webClient.setThrowExceptionOnScriptError(false);
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        log.info("Using Browser {}", webClient.getBrowserVersion().getUserAgent());
        HtmlPage page = webClient.getPage(WebConversationFactory.getWARURL() + "/login.xhtml");
        log.debug(page.asXml());
        HtmlForm form = page.getFormByName("login");
        form.getElementById("login:usernameInput").setAttribute("value", "admin");
        form.getElementById("login:passwordInput").setAttribute("value", "admin");
        form.getElementById("login:loginButton").click();
        log.info("Login done");
        log.info("Loading page {}", wcSpec.getInitialPage());
        return super.doInitialRequest(wcSpec);
    }

}
