package com.nokia.ci.ejb.sanity;

import java.nio.charset.Charset;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author miikka
 */
@Startup
@Singleton
public class SanityCheck {

    private static Logger log = LoggerFactory.getLogger(SanityCheck.class);

    @PostConstruct
    protected void check() {
        if (!Charset.defaultCharset().displayName().equals("UTF-8")) {
            log.error("\n"
                    + "***********************************************************\n"
                    + "           WARNING! SYSTEM CHARSET IS NOT UTF8 ("
                    + Charset.defaultCharset().displayName()
                    + ")\n***********************************************************");
        }
    }
}
