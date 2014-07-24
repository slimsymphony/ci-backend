/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.model.EntityTest;
import java.lang.reflect.Constructor;
import java.util.Set;
import javax.ejb.LocalBean;
import org.junit.Before;
import org.junit.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hhellgre
 */
public class EJBConstructorTest extends EJBTestBase {

    private static final Logger log = LoggerFactory.getLogger(EJBConstructorTest.class);

    @Override
    @Before
    public void before() {
        super.before();
    }

    /* This tests that all EJB beans have constructors and cb + em inherited from CrudFunctionality */
    @Test
    public void crudFunctionalityConstructorTests() throws Exception {
        Reflections reflections = new Reflections("com.nokia.ci.ejb");
        Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(LocalBean.class);

        for (Class clazz : allClasses) {
            if (!CrudFunctionality.class.isAssignableFrom(clazz)) {
                continue;
            }

            log.info("Testing crudFunctionality constructor for class {}", clazz.getCanonicalName());

            Constructor<? extends CrudFunctionality> ctor = clazz.getConstructor();
            CrudFunctionality cf = ctor.newInstance();
            cf.cb = cb;
            cf.em = em;
            cf.init();
        }
    }

    @Test
    public void otherLocalBeanClassTests() throws Exception {
        Reflections reflections = new Reflections("com.nokia.ci.ejb");
        Set<Class<?>> allClasses = reflections.getTypesAnnotatedWith(LocalBean.class);

        for (Class clazz : allClasses) {
            if (CrudFunctionality.class.isAssignableFrom(clazz)) {
                continue;
            }

            log.info("Testing constructor for class {}", clazz.getCanonicalName());

            Constructor<?> ctor = clazz.getConstructor();
            Object cf = ctor.newInstance();
        }
    }
}
