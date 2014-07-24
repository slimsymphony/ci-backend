package com.nokia.ci.ejb.annotation;

import com.nokia.ci.ejb.model.ReportAction;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 *
 * @author vrouvine
 */
@Qualifier
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportActionExecutorType {

    Class<? extends ReportAction> type();
}
