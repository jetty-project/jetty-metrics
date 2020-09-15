//
//  ========================================================================
//  Copyright (c) 1995-2020 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.metrics;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.servlet.FilterHolder;

public class MetricsFilterWrapper extends FilterHolder.WrapperFilter
{
    private final ServletMetricsListener metricsListener;

    public MetricsFilterWrapper(Filter filter, ServletMetricsListener metricsListener)
    {
        super(filter);
        this.metricsListener = metricsListener;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        ServletContext servletContext = filterConfig.getServletContext();
        Filter delegate = getWrappedFilter();
        long start = System.nanoTime();
        try
        {
            metricsListener.onFilterStarting(servletContext, delegate);
            this.getWrappedFilter().init(filterConfig);
        }
        finally
        {
            long end = System.nanoTime();
            long dur = end - start;
            metricsListener.onFilterReady(servletContext, delegate, Duration.of(dur, ChronoUnit.NANOS));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        ServletContext servletContext = request.getServletContext();
        Filter delegate = getWrappedFilter();
        long start = System.nanoTime();
        try
        {
            metricsListener.onFilterEnter(servletContext, delegate, request);
            delegate.doFilter(request, response, chain);
            long end = System.nanoTime();
            long dur = end - start;
            metricsListener.onFilterExit(servletContext, delegate, request, Duration.of(dur, ChronoUnit.NANOS), null);
        }
        catch (Throwable cause)
        {
            long end = System.nanoTime();
            long dur = end - start;
            metricsListener.onFilterExit(servletContext, delegate, request, Duration.of(dur, ChronoUnit.NANOS), cause);
            throw cause;
        }
    }

    @Override
    public void destroy()
    {
    }
}
