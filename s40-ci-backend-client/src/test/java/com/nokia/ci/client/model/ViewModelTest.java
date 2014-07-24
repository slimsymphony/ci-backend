package com.nokia.ci.client.model;

import com.nokia.ci.ejb.testtools.PojoTestUtil;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import org.junit.Test;
import org.reflections.Reflections;

/**
 * Tests for view models.
 *
 * @author vrouvine
 */
public class ViewModelTest {

    @Test
    public void testClientModelViews() throws Exception {
        Reflections reflections = new Reflections("com.nokia.ci.client.model");
        Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(XmlRootElement.class);

        for (Class clazz : allClasses) {
            PojoTestUtil.testGetAndSet(clazz);
        }
    }
}
