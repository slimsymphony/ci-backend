package com.nokia.ci.ui.it;

import com.nokia.ci.ejb.it.EJBDeploymentCreator;
import java.io.File;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.NonTransitiveStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates deployment packages for JSFUnit tests.
 *
 * @author vrouvine
 */
public class UIDeploymentCreator {

    private static final Logger log = LoggerFactory.getLogger(UIDeploymentCreator.class);
    private static final String WEBAPP_SRC = "src/main/webapp";

    public static WebArchive createJSFDeployment() {
        WebArchive archive = EJBDeploymentCreator.createEJBDeploymentAsWar("../s40-ci-backend-ejb/pom.xml");
        File[] libs = Maven.resolver().offline(true)
                .loadPomFromFile("pom.xml", "it-test").importRuntimeDependencies(NonTransitiveStrategy.INSTANCE).asFile();
        archive.addAsWebResource(new File("src/test/resources/META-INF/MANIFEST.MF"), "META-INF/MANIFEST.MF");
        archive.setWebXML(new File("target/classes/web.xml"))
                .addAsWebInfResource("jboss-web.xml")
                .addAsResource(new File("src/main/resources/com/nokia/ci/ui/props/buildInfo.properties"), "com/nokia/ci/ui/props/buildInfo.properties")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addPackages(true, "com.nokia.ci.ui")
                .addAsLibraries(libs);
        addWebSources(archive);
        log.debug(archive.toString(true));

        // Create archive to disk. Enable for debugging
        //archive.as(ZipExporter.class).exportTo(new File("target/ui-deployment.war"), true);

        return archive;
    }

    private static WebArchive addWebSources(WebArchive archive) {
        final File webAppDirectory = new File(WEBAPP_SRC);

        Collection<File> files = FileUtils.listFiles(webAppDirectory, null, true);
        for (File file : files) {
            if (!file.isDirectory()) {
                String path = file.getPath().substring(WEBAPP_SRC.length());
                path = path.replaceAll("\\\\", "/");
                archive.addAsWebResource(file, path);
            }
        }
        return archive;
    }
}
