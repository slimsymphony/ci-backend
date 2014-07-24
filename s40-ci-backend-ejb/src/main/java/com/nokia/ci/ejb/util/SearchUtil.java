/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.util;

import com.nokia.ci.ejb.model.Project;
import com.nokia.ci.ejb.model.SecurityEntity;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.reflections.Reflections;

/**
 *
 * @author hhellgre
 */
public class SearchUtil {

    public static Set<Class<?>> getIndexedSecurityClasses() {
        Set<Class<?>> all = getIndexedClasses();
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Class c : all) {
            if (SecurityEntity.class.isAssignableFrom(c)) {
                classes.add(c);
            }
        }

        // Project class is special case
        classes.add(Project.class);

        return classes;
    }

    public static Set<Class<?>> getIndexedNonSecurityClasses() {
        Set<Class<?>> all = getIndexedClasses();
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Class c : all) {
            if (c.equals(Project.class)) {
                continue;
            }
            if (!SecurityEntity.class.isAssignableFrom(c)) {
                classes.add(c);
            }
        }
        return classes;
    }

    public static Set<Class<?>> getIndexedClasses() {
        Reflections reflections = new Reflections("com.nokia.ci.ejb.model");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Indexed.class);
        return classes;
    }

    public static Set<String> getIndexedFields(Class c) {
        Set<String> ret = new HashSet<String>();
        Field[] allFields = c.getDeclaredFields();
        for (Field f : allFields) {
            if (f.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
                String annotationName = f.getAnnotation(org.hibernate.search.annotations.Field.class).name();
                if (StringUtils.isEmpty(annotationName)) {
                    annotationName = f.getName();
                }
                if (annotationName.equals("projectId")) {
                    continue;
                }
                ret.add(annotationName);
            } else if (f.isAnnotationPresent(IndexedEmbedded.class)) {
                Type memberType = f.getGenericType();
                Class<?> clazz;
                if (memberType instanceof ParameterizedType) {
                    ParameterizedType listType = (ParameterizedType) memberType;
                    clazz = (Class<?>) listType.getActualTypeArguments()[0];
                } else {
                    clazz = (Class<?>) memberType;
                }
                Field[] memberFields = clazz.getDeclaredFields();
                for (Field mf : memberFields) {
                    if (mf.isAnnotationPresent(org.hibernate.search.annotations.Field.class)) {
                        String annotationName = mf.getAnnotation(org.hibernate.search.annotations.Field.class).name();
                        if (StringUtils.isEmpty(annotationName)) {
                            annotationName = mf.getName();
                        }
                        ret.add(f.getName() + "." + annotationName);
                    }
                }
            }
        }
        return ret;
    }
}
