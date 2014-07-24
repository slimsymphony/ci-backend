/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.client.model.exception;

import com.nokia.ci.client.exception.ViewTransformException;
import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class ViewTransformExceptionTest {
    @Test(expected=ViewTransformException.class)
    public void constructAndThrow1() {
        ViewTransformException ex = new ViewTransformException();
        throw ex;
    }
    
    @Test(expected=ViewTransformException.class)
    public void constructAndThrow2() {
        final String msg = "MESSAGE";
        ViewTransformException ex = new ViewTransformException(msg);
        Assert.assertTrue(ex.getMessage().compareTo(msg) == 0);
        throw ex;
    }
    
    @Test(expected=ViewTransformException.class)
    public void constructAndThrow3() {
        final String msg = "MESSAGE";
        final Exception e = new NullPointerException();
        ViewTransformException ex = new ViewTransformException(msg, e);
        Assert.assertTrue(ex.getMessage().compareTo(msg) == 0);
        Assert.assertEquals(ex.getCause(), e);
        throw ex;
    }    
}
