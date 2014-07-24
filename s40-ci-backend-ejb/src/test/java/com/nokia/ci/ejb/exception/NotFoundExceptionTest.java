package com.nokia.ci.ejb.exception;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Tests {@link NotFoundException} exception.
 * @author vrouvine
 */
public class NotFoundExceptionTest {
    
    @Test
    public void instance() {
        Assert.assertNotNull(new NotFoundException());
    }
    
    @Test
    public void instanceWithMessage() {
        String message = "Test message";
        NotFoundException ex = new NotFoundException(message);
        Assert.assertNotNull(ex);
        Assert.assertEquals(message, ex.getMessage());
    }
}
