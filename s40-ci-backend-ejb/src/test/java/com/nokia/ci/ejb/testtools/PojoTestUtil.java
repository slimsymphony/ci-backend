package com.nokia.ci.ejb.testtools;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Version;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test utility class for testing POJOs.
 *
 * @author vrouvine
 */
public class PojoTestUtil {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(PojoTestUtil.class);
    /**
     * Test value map for property types.
     */
    private static final Map<Class, Object> propertyValues;

    /**
     * Static initialization.
     */
    static {
        Map<Class, Object> map = new HashMap<Class, Object>();
        map.put(float.class, 1.2f);
        map.put(long.class, 123456789L);
        map.put(double.class, 3.443d);
        map.put(int.class, 2);
        map.put(Long.class, Long.valueOf(3));
        map.put(Double.class, Double.valueOf(3.21));
        map.put(Integer.class, Integer.valueOf(4));
        map.put(String.class, "Test String");
        map.put(Date.class, new Date());
        map.put(List.class, new ArrayList());
        map.put(Set.class, new HashSet());
        map.put(boolean.class, true);
        propertyValues = Collections.unmodifiableMap(map);
    }

    /**
     * Tests basic getter and setter functionality from given Class. All non
     * final and non static property field must have corresponding getter and
     * setter method.
     *
     * @param clazz Class for testing
     * @throws Exception if test hash problem.
     */
    public static void testGetAndSet(Class clazz) throws Exception {
        log.info("*** Testing class [{}] ****", clazz.getCanonicalName());
        Class c = clazz;
        Object object = clazz.newInstance();
        while (c != null) {
            for (Field field : c.getDeclaredFields()) {
                int mods = field.getModifiers();
                if (Modifier.isFinal(mods)) {
                    log.info("Skipping final field [{}]", field.getName());
                    continue;
                }
                if (Modifier.isStatic(mods)) {
                    log.info("Skipping static field [{}]", field.getName());
                    continue;
                }
                Version version = field.getAnnotation(javax.persistence.Version.class);
                if (version != null) {
                    log.info("Skipping @Version annotated field [{}]", field.getName());
                    continue;
                }
                testProperty(object, field);
            }
            c = c.getSuperclass();
        }
    }

    /**
     * Tests getter and setter methods for one property field.
     *
     * @param object Instance to test
     * @param field Field to test
     * @throws Exception if testing field has problem.
     */
    private static void testProperty(Object object, Field field) throws Exception {
        log.info("Testing field [{}] of class [{}]", field.getName(), object.getClass());
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(field.getName(), object.getClass());
        Class<?> propertyType = propertyDescriptor.getPropertyType();
        Object testValue = propertyValues.get(propertyType);

        log.info("Testing field [{}] with value [{}]", propertyDescriptor.getDisplayName(), testValue);

        Method writeMethod = propertyDescriptor.getWriteMethod();
        Method readMethod = propertyDescriptor.getReadMethod();

        Assert.assertNotNull("There is no setter for field!", writeMethod);
        Assert.assertNotNull("There is no getter for field!", readMethod);

        writeMethod.invoke(object, testValue);
        Assert.assertEquals(readMethod.invoke(object), testValue);
    }
}
