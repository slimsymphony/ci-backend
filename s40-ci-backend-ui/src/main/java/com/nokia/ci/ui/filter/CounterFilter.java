/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nokia.ci.ui.filter;

import com.nokia.ci.ui.wrapper.CounterServletResponse;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author hhellgre
 */
public class CounterFilter implements Filter {

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpres = (HttpServletResponse) response;
        CounterServletResponse counter = new CounterServletResponse(httpres);
        HttpServletRequest httpreq = (HttpServletRequest) request;
        httpreq.setAttribute("counter", counter);
        chain.doFilter(request, counter);
    }

    @Override
    public void destroy() {
    }
}
