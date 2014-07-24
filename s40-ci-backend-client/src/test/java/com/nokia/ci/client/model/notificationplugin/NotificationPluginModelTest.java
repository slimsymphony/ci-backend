package com.nokia.ci.client.model.notificationplugin;

import com.nokia.ci.ejb.testtools.PojoTestUtil;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import junit.framework.Assert;
import org.junit.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for job notification models.
 *
 * @author vrouvine
 */
public class NotificationPluginModelTest {

    private static Logger log = LoggerFactory.getLogger(NotificationPluginModelTest.class);

    @Test
    public void notificationPluginModelTests() throws Exception {
        Reflections reflections = new Reflections("com.nokia.ci.client.model.notificationplugin");
        Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(XmlRootElement.class);

        for (Class clazz : allClasses) {
            PojoTestUtil.testGetAndSet(clazz);
        }
    }

    @Test
    public void notificationPluginEnumTests() throws Exception {
        Reflections reflections = new Reflections("com.nokia.ci.client.model.notificationplugin");
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
