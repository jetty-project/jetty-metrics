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

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class ServletMetricsCaptureListener implements ServletMetricsListener
{
    private static final Logger LOG = Log.getLogger(ServletMetricsCaptureListener.class);
    private LinkedBlockingQueue<String> events = new LinkedBlockingQueue<>();

    protected void addEvent(String format, Object... args)
    {
        String eventText = String.format(format, args);
        events.offer(eventText);
        Throwable cause = null;
        Object lastArg = args[args.length - 1];
        if (lastArg instanceof Throwable)
        {
            cause = (Throwable)lastArg;
        }
        LOG.info("[EVENT] {}", eventText, cause);
    }

    public LinkedBlockingQueue<String> getEvents()
    {
        return events;
    }

    @Override
    public void onServletContextStarting(ServletContext servletContext)
    {
        addEvent("onServletContextStarting(), servletContext=%s", servletContext);
    }

    @Override
    public void onServletContextReady(ServletContext servletContext)
    {
        addEvent("onServletContextReady(), servletContext=%s", servletContext);
    }

    @Override
    public void onServletStarting(ServletContext servletContext, Servlet servlet)
    {
        addEvent("onServletStarting(), servletContext=%s, servlet=%s", servletContext, servlet);
    }

    @Override
    public void onServletReady(ServletContext servletContext, Servlet servlet, Duration duration)
    {
        addEvent("onServletReady(), servletContext=%s, servlet=%s, duration=%s", servletContext, servlet, duration);
    }

    @Override
    public void onFilterStarting(ServletContext servletContext, Filter filter)
    {
        addEvent("onFilterStarting(), servletContext=%s, filter=%s", servletContext, filter);
    }

    @Override
    public void onFilterReady(ServletContext servletContext, Filter filter, Duration duration)
    {
        addEvent("onFilterReady(), servletContext=%s, filter=%s, duration=%s", servletContext, filter, duration);
    }

    @Override
    public void onFilterEnter(ServletContext servletContext, Filter filter, ServletRequest request)
    {
        addEvent("onFilterEnter(), servletContext=%s, filter=%s, request=%s", servletContext, filter, request);
    }

    @Override
    public void onFilterExit(ServletContext servletContext, Filter filter, ServletRequest request, Duration duration, Throwable cause)
    {
        addEvent("onFilterExit(), servletContext=%s, filter=%s, request=%s, duration=%s", servletContext, filter, request, duration, cause);
    }

    @Override
    public void onServletEnter(ServletContext servletContext, Servlet servlet, ServletRequest request)
    {
        addEvent("onServletEnter(), servletContext=%s, servlet=%s, request=%s", servletContext, servlet, request);
    }

    @Override
    public void onServletExit(ServletContext servletContext, Servlet servlet, ServletRequest request, Duration duration, Throwable cause)
    {
        addEvent("onServletExit(), servletContext=%s, servlet=%s, request=%s, duration=%s", servletContext, servlet, request, duration, cause);
    }
}
