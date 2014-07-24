/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 *
 * @author jajuutin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE, ElementType.METHOD})
@Qualifier
public @interface BuildGroupReleasedEvent {
}
