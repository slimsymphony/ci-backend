package com.nokia.ci.ejb.it;

import com.nokia.ci.ejb.hasingleton.HATimerService;
import com.nokia.ci.ejb.hasingleton.HATimerServiceActivator;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jboss.shrinkwrap.api.Filters;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.CoordinateParseException;
import org.jboss.shrinkwrap.resolver.api.InvalidConfigurationFileException;
import org.jboss.shrinkwrap.resolver.api.ResolutionException;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating deployment archives for Arquillian tests.
 *
 * @author vrouvine
 */
public class EJBDeploymentCreator {
    
    private static final Logger log = LoggerFactory.getLogger(EJBDeploymentCreator.class);
    
    /**
     * Create EJB deployment archive.
     * Archive wraps all EJB module dependency libraries and EJB class to single EAR archive.
     * NOTE! Timer classes are excluded because of problems with database locks.
     * 
     * @return EAR archive for EJB testing deployment.
     */
    public static EnterpriseArchive createEJBDeployment(String ejbPom) {
        File[] libs = getLibraries(ejbPom);
        JavaArchive ejbJar = createEJBJar();

        EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "s40-ci-backend-test.ear")
                .addAsApplicationResource("META-INF/test-MANIFEST.MF", "MANIFEST.MF")
                .addAsModule(ejbJar)
                .addAsLibraries(libs);
//                .addAsLibraries(mockLibs)
//                .addAsLibraries(subethamailLib);
        log.debug(ear.toString(true));
        
        // Create archive to disk. Enable for debugging
        //ear.as(ZipExporter.class).exportTo(new File("target/s40-ci-backend-test.ear"), true);
        
        return ear;
    }
    
    /**
     * Create EJB deployment archive.
     * Archive wraps all EJB module dependency libraries and EJB class to single WAR archive.
     * NOTE! Timer classes are excluded because of problems with database locks.
     * 
     * @return WAR archive for EJB testing deployment.
     */
    public static WebArchive createEJBDeploymentAsWar(String ejbPom) {
        File[] libs = getLibraries(ejbPom);
        JavaArchive ejbJar = createEJBJar();

        WebArchive archive = ShrinkWrap.create(WebArchive.class)
                .addAsLibrary(ejbJar)
                .addAsLibraries(libs);
        log.debug(archive.toString(true));
        
        // Create archive to disk. Enable for debugging
        //archive.as(ZipExporter.class).exportTo(new File("target/ejb-deployment.war"), true);
        
        return archive;
    }

    private static JavaArchive createEJBJar() throws IllegalArgumentException {
        JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "s40-ci-backend-ejb-test.jar")
                .addPackages(true, Filters.exclude(HATimerService.class, HATimerServiceActivator.class), "com.nokia.ci.ejb")
                .addAsManifestResource("META-INF/test-persistence.xml", "persistence.xml")
                .addAsManifestResource("META-INF/test-ejb-jar.xml", "ejb-jar.xml")
                .addAsManifestResource("META-INF/test-jboss-ejb3.xml", "jboss-ejb3.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ejbJar;
    }

    private static File[] getLibraries(String ejbPom) throws ResolutionException, InvalidConfigurationFileException, CoordinateParseException, IllegalArgumentException {
        List<File> libList = new ArrayList<File>();
        File[] ejbLibs = Maven.resolver().offline(true)
                .loadPomFromFile(ejbPom).importRuntimeDependencies().asFile();
        libList.addAll(Arrays.asList(ejbLibs));
        File[] subethamailLib = Maven.resolver().offline(true)
                .resolve("org.subethamail:subethasmtp:3.1.7").withoutTransitivity().asFile();
        libList.addAll(Arrays.asList(subethamailLib));
        File[] mockLibs = Maven.resolver().offline(true)
                .loadPomFromFile("../s40-ci-backend-mock/pom.xml").importRuntimeDependencies().asFile();
        libList.addAll(Arrays.asList(mockLibs));
        
        return libList.toArray(new File[0]);
    }
}
