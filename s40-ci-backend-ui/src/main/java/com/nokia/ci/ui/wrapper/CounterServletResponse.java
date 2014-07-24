/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.wrapper;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author hhellgre
 */
public class CounterServletResponse extends HttpServletResponseWrapper {

    private final long startTime;

    public CounterServletResponse(HttpServletResponse response) throws IOException {
        super(response);
        startTime = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
}