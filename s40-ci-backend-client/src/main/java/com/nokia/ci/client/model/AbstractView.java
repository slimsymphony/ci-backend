package com.nokia.ci.client.model;

import com.nokia.ci.client.exception.ViewTransformException;
import java.lang.reflect.Field;
import javax.xml.bind.annotation.XmlElement;

/**
 * Abstraction of view models.
 *
 * @author vrouvine
 */
public abstract class AbstractView {

    /**
     * Transforms view model to wanted target class. Only field with {@link XmlElement}
     * annotation and identical name with target class are transformed.
     *
     * @param <E> Return type
     * @param clazz Target class where to transform
     * @return new instance of target class
     * @throws ViewTransformException when target class can not be instantiated
     */
    public <E> E transformTo(Class<E> clazz) throws ViewTransformException {
        try {
            E target = clazz.newInstance();
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                XmlElement annotation = field.getAnnotation(XmlElement.class);
                if (annotation == null) {
                    continue;
                }
                try {
                    Field targetField = clazz.getDeclaredField(field.getName());
                    copyFieldValue(field, this, targetField, target);
                } catch (NoSuchFieldException ex) {
                } catch (SecurityException ex) {
                }
            }
            return target;
        } catch (InstantiationException ex) {
            throw new ViewTransformException("Instantiation of class " + clazz.getName() + " failed!", ex);
        } catch (IllegalAccessException ex) {
            throw new ViewTransformException("Accessing constructor of class " + clazz.getName() + " failed!", ex);
        }
    }

    /**
     * Copies values from source object. Fields with {@link XmlElement}
     * annotation and identical field name with source object are copied.
     *
     * @param source Source object
     * @throws ViewTransformException if source object is {@code null}
     */
    public void copyValuesFrom(Object source) throws ViewTransformException {
        if (source == null) {
            throw new ViewTransformException("Can not copy values from NULL object!");
        }

        Class clazz = source.getClass();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field targetField : fields) {

            XmlElement annotation = targetField.getAnnotation(XmlElement.class);
            if (annotation == null) {
                continue;
            }
            try {
                Field field = clazz.getDeclaredField(targetField.getName());
                copyFieldValue(field, source, targetField, this);
            } catch (NoSuchFieldException ex) {
            }
        }
    }

    /**
     * Copies field values.
     *
     * @param fromField Source field
     * @param from Object Source object
     * @param targetField Target field
     * @param target Target object
     * @throws ViewTransformException field types are not matching or field is
     * not accessible.
     */
    private void copyFieldValue(Field fromField, Object from, Field targetField, Object target) throws ViewTransformException {
        try {
            fromField.setAccessible(true);
            Object value = fromField.get(from);

            targetField.setAccessible(true);
            targetField.set(target, value);
        } catch (IllegalArgumentException ex) {
            throw new ViewTransformException("Invalid argument for field!", ex);
        } catch (IllegalAccessException ex) {
            throw new ViewTransformException("Can not access field!", ex);
        }
    }
}
