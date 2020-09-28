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
import java.util.Objects;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.jetty.servlet.ServletHolder;

public class MetricsServletWrapper extends ServletHolder.Wrapper
{
    private final ServletMetricsListener metricsListener;

    public MetricsServletWrapper(Servlet servlet, ServletMetricsListener metricsListener)
    {
        super(servlet);
        this.metricsListener = metricsListener;
    }

    /**
     * Entry point for {@link ServletHolder#initialize()}
     */
    @Override
    public void init(ServletConfig config) throws ServletException
    {
        ServletContext servletContext = config.getServletContext();
        Servlet delegate = getWrapped();
        long start = System.nanoTime();
        try
        {
            metricsListener.onServletStarting(servletContext, delegate);
            delegate.init(config);
        }
        finally
        {
            long end = System.nanoTime();
            long dur = end - start;
            metricsListener.onServletReady(servletContext, delegate, Duration.of(dur, ChronoUnit.NANOS));
        }
    }

    /**
     * Entry point for all Servlet requests
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        ServletContext servletContext = req.getServletContext();
        Servlet delegate = Objects.requireNonNull(getWrapped());
        long start = System.nanoTime();
        try
        {
            metricsListener.onServletEnter(servletContext, delegate, req);
            delegate.service(req, res);
            long end = System.nanoTime();
            long dur = end - start;
            metricsListener.onServletExit(servletContext, delegate, req, Duration.of(dur, ChronoUnit.NANOS), null);
        }
        catch (Throwable cause)
        {
            long end = System.nanoTime();
            long dur = end - start;
            metricsListener.onServletExit(servletContext, delegate, req, Duration.of(dur, ChronoUnit.NANOS), cause);
            throw cause;
        }
    }
}
