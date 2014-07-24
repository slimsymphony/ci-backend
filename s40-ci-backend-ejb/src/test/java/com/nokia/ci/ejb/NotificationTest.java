/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import com.nokia.ci.ejb.testtools.PojoTestUtil;
import org.junit.Test;

/**
 *
 * @author jajuutin
 */
public class NotificationTest {

    @Test
    public void NotificationTest() throws Exception {
        PojoTestUtil.testGetAndSet(BuildNotification.class);
    }
}
