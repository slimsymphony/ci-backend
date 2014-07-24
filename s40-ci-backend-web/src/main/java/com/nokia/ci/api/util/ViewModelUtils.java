package com.nokia.ci.api.util;

import com.nokia.ci.client.exception.ViewTransformException;
import com.nokia.ci.client.model.AbstractView;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for handling view models.
 * @author vrouvine
 */
public class ViewModelUtils {

    /**
     * Logger.
     */
    private static final Logger log = 
            LoggerFactory.getLogger(ViewModelUtils.class);

    private ViewModelUtils() {
    }

    public static <T, V extends AbstractView> List<T> transformToList(
            List<V> views, Class<T> target) {

        List<T> list = new ArrayList<T>();

        for (AbstractView view : views) {
            list.add(view.transformTo(target));
        }

        return list;
    }

    /**
     * Creates list of {@link AbstractView} child class instances from the object list.
     * @param <T> Return type that extends {@link AbstractView}
     * @param objects List of objects to convert
     * @param target Child class of {@link AbstractView}
     * @return List of instances of target class
     * @throws ViewTransformException If conversion fails.
     */
    public static <T extends AbstractView> List<T> copyValuesFromList(
            List objects, Class<T> target) throws ViewTransformException {

        List<T> list = new ArrayList<T>();
        for (Object object : objects) {
            T view = getModel(object, target);
            if (view == null) {
                continue;
            }
            list.add(view);
        }
        return list;
    }

    /**
     * Creates single instance of {@link AbstractView} child class object.
     * @param <T> Return type that extends {@link AbstractView}
     * @param object Object to convert
     * @param target Child class of {@link AbstractView}
     * @return Instance of target class. {@code null} if target class cannot not be instantiated.
     * @throws ViewTransformException If conversion fails.
     */
    public static <T extends AbstractView> T getModel(Object object,
            Class<T> target) throws ViewTransformException {
        
        try {
            T view = target.newInstance();
            view.copyValuesFrom(object);
            return view;
        } catch (InstantiationException ex) {
            log.error("Can not instantiate!", ex);
        } catch (IllegalAccessException ex) {
            log.error("Can not access!", ex);
        }
        return null;
    }
}
