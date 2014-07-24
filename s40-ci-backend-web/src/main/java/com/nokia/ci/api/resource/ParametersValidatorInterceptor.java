package com.nokia.ci.api.resource;

import com.nokia.ci.ejb.event.IncidentEventContent;
import com.nokia.ci.ejb.model.IncidentType;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.core.ServerResponse;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 3/27/13
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
@Provider
@ServerInterceptor
public class ParametersValidatorInterceptor implements PreProcessInterceptor {

    private static final String PATH_PARAM_DESC = "Undefined path parameter '%s' for REST api call '%s'";
    private static final String QUERY_PARAM_DESC = "Undefined query parameter '%s' for REST api call '%s'";

    @Inject
    Event<IncidentEventContent> incidentEvents;

    @Override
    public ServerResponse preProcess(HttpRequest request, ResourceMethod method) throws Failure, WebApplicationException {
        MultivaluedMap<String, String> pathParameters = request.getUri().getPathParameters();
        for (String pathParamKey : pathParameters.keySet()) {
            if (!isPathParamAllowed(pathParamKey, method)) {
                IncidentEventContent event = new IncidentEventContent(IncidentType.SECURITY, String.format(PATH_PARAM_DESC, pathParamKey, request.getUri().getPath()));
                incidentEvents.fire(event);
                return new ServerResponse("Undefined parameter '" + pathParamKey + "'", 400, new Headers<Object>());
            }
        }
        MultivaluedMap<String, String> queryParameters = request.getUri().getQueryParameters();
        for (String queryParamKey : queryParameters.keySet()) {
            if (!isQueryParamAllowed(queryParamKey, method)) {
                IncidentEventContent event = new IncidentEventContent(IncidentType.SECURITY, String.format(QUERY_PARAM_DESC, queryParamKey, request.getUri().getPath()));
                incidentEvents.fire(event);
                return new ServerResponse("Undefined parameter '" + queryParamKey + "'", 400, new Headers<Object>());
            }
        }
        return null;
    }


    private boolean isPathParamAllowed(String pathParamKey, ResourceMethod method) {
        List<PathParam> pathParamAnnotations = findAnnotationsByType(PathParam.class, method.getMethod().getParameterAnnotations());
        for (PathParam pathParamAnnotation : pathParamAnnotations) {
            if (pathParamKey.equals(pathParamAnnotation.value())) {
                return true;
            }
        }
        return false;
    }

    private boolean isQueryParamAllowed(String queryParamKey, ResourceMethod method) {
        List<QueryParam> queryParamAnnotations = findAnnotationsByType(QueryParam.class, method.getMethod().getParameterAnnotations());
        for (QueryParam queryParamAnnotation : queryParamAnnotations) {
            if (queryParamKey.equals(queryParamAnnotation.value())) {
                return true;
            }
        }
        return false;
    }

    private <T> List<T> findAnnotationsByType(Class<T> annotationType, Annotation[][] parametersAnnotations) {
        List<T> ret = new ArrayList<T>();
        for (Annotation[] parameterAnnotations : parametersAnnotations) {
            for (Annotation annotation : parameterAnnotations) {
                if (annotationType.isInstance(annotation)) {
                    ret.add((T) annotation);
                }
            }
        }
        return ret;
    }
}
