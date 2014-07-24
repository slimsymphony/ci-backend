/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.api.util;

import java.lang.reflect.Constructor;
import java.util.Set;
import org.junit.Test;
import org.reflections.Reflections;

/**
 *
 * @author hhellgre
 */
public class ApiUtilConstructorTest {

    @Test
    public void constructTest() throws Exception {
        Reflections reflections = new Reflections("com.nokia.ci.api.util");
        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

        for (Class clazz : allClasses) {
            Constructor<? extends Object> ctor = clazz.getConstructor();
            Object cf = ctor.newInstance();
        }
    }
}
