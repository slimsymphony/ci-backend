/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.gerrit.model;

import com.nokia.ci.ejb.model.BaseEntity;
import com.nokia.ci.ejb.testtools.PojoTestUtil;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import junit.framework.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jajuutin
 */
public class GerritDetailTest {

    private static final Logger log = LoggerFactory.getLogger(GerritDetailTest.class);

    @Test
    public void testGerritModels() throws Exception {
        String packageName = "com.nokia.ci.ejb.gerrit.model";
        ClassLoader cl = BaseEntity.class.getClassLoader();
        Enumeration<URL> resources = cl.getResources("");
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            log.info("Classpath resource {}", url.getPath());
            if (url.getPath().contains("/classes/")) {
                File models = new File(url.getPath() + packageName.replace(".", "/"));
                log.info("File to scan {}", models);
                handleModels(models, cl, packageName);
            }
        }
    }

    private void handleModels(File models, ClassLoader cl, String packageName) throws Exception {
        if (models == null) {
            return;
        }

        String[] strList = models.list();
        if (strList == null) {
            return;
        }

        for (String s : strList) {
            String className = s.replace(".class", "");
            if (s.endsWith("_")) {
                continue;
            }
            Class<?> clazz = cl.loadClass(packageName + "." + className);

            if (Modifier.isAbstract(clazz.getModifiers()) || Enum.class.isAssignableFrom(clazz)) {
                continue;
            }

            PojoTestUtil.testGetAndSet(clazz);
        }
    }

    @Test
    public void enumEntitiesTest() throws Exception {
        Reflections reflections = new Reflections("com.nokia.ci.ejb.gerrit.model");
        Set<Class<? extends Enum>> allClasses = reflections.getSubTypesOf(Enum.class);
        for (Class clazz : allClasses) {
            List<Object> objs = Arrays.asList(clazz.getEnumConstants());

            for (Object o : objs) {
                Enum e = (Enum) o;
                log.info("{} - Testing enum {}", clazz, e);
                Enum e2 = Enum.valueOf(clazz, e.toString());
                Assert.assertEquals(e, e2);
            }
        }
    }
}
