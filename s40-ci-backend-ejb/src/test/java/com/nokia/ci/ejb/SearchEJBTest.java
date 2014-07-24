/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ejb;

import org.junit.Before;

/**
 *
 * @author hhellgre
 */
public class SearchEJBTest extends EJBTestBase {

    private SearchEJB ejb;

    @Override
    @Before
    public void before() {
        super.before();

        ejb = new SearchEJB();
        ejb.em = em;
        ejb.context = context;
    }
}
