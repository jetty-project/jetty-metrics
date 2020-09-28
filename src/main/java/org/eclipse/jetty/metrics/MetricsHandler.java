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

import java.util.EventListener;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ListenerHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

public class MetricsHandler extends ContainerLifeCycle
    implements ServletHolder.WrapFunction,
    FilterHolder.WrapFunction,
    ListenerHolder.WrapFunction,
    Configuration.WrapperFunction,
    HttpChannel.Listener,
    LifeCycle.Listener
{
    private static final Logger LOG = Log.getLogger(MetricsHandler.class);
    public static final String ATTR_REQUEST_ID = MetricsHandler.class.getName() + ".requestId";
    private final ServletMetricsListener metricsListener;

    public MetricsHandler(ServletMetricsListener metricsListener)
    {
        this.metricsListener = metricsListener;
    }

    public void addToAllConnectors(Server server)
    {
        for (Connector connector : server.getConnectors())
        {
            if (connector instanceof ServerConnector)
            {
                connector.addBean(this);
            }
        }
    }

    public void addToContext(ServletContextHandler context)
    {
        context.addBean(this);
        context.addLifeCycleListener(this);
    }

    @Override
    public void lifeCycleStarting(LifeCycle event)
    {
        if (event instanceof WebAppContext)
        {
            WebAppContext webAppContext = (WebAppContext)event;
            if (metricsListener instanceof WebAppMetricsListener)
            {
                ((WebAppMetricsListener)metricsListener).onWebAppStarting(webAppContext);
            }
        }
        if (event instanceof ServletContextHandler)
        {
            ServletContextHandler contextHandler = (ServletContextHandler)event;
            metricsListener.onServletContextStarting(contextHandler.getServletContext());
        }
    }

    @Override
    public void lifeCycleStarted(LifeCycle event)
    {
        if (event instanceof WebAppContext)
        {
            WebAppContext webAppContext = (WebAppContext)event;
            if (metricsListener instanceof WebAppMetricsListener)
            {
                ((WebAppMetricsListener)metricsListener).onWebAppReady(webAppContext);
            }
        }
        if (event instanceof ServletContextHandler)
        {
            ServletContextHandler contextHandler = (ServletContextHandler)event;
            metricsListener.onServletContextReady(contextHandler.getServletContext());
        }
    }

    @Override
    public void lifeCycleFailure(LifeCycle event, Throwable cause)
    {
    }

    @Override
    public void lifeCycleStopping(LifeCycle event)
    {
    }

    @Override
    public void lifeCycleStopped(LifeCycle event)
    {
    }

    @Override
    public void onRequestBegin(Request request)
    {
        String uniqId = UUID.randomUUID().toString();
        request.setAttribute(ATTR_REQUEST_ID, uniqId);
    }

    @Override
    public Configuration wrapConfiguration(Configuration configuration)
    {
        LOG.info("wrapConfiguration({})", configuration);
        if (!(metricsListener instanceof WebAppMetricsListener))
        {
            return configuration;
        }

        Configuration unwrapped = configuration;
        while (unwrapped instanceof Configuration.Wrapper)
        {
            // Are we already wrapped somewhere along the line?
            if (unwrapped instanceof MetricsConfigurationWrapper)
            {
                // If so, we are done. no need to wrap again.
                return configuration;
            }
            // Unwrap
            unwrapped = ((Configuration.Wrapper)unwrapped).getWrapped();
        }

        return new MetricsConfigurationWrapper(configuration, (WebAppMetricsListener)metricsListener);
    }

    @Override
    public EventListener wrapEventListener(EventListener listener)
    {
        LOG.info("wrapEventListener({})", listener);
        return listener;
    }

    @Override
    public Filter wrapFilter(Filter filter)
    {
        LOG.info("wrapFilter({})", filter);
        Filter unwrapped = filter;
        while (unwrapped instanceof FilterHolder.Wrapper)
        {
            // Are we already wrapped somewhere along the line?
            if (unwrapped instanceof MetricsFilterWrapper)
            {
                // If so, we are done. no need to wrap again.
                return filter;
            }
            // Unwrap
            unwrapped = ((FilterHolder.Wrapper)unwrapped).getWrapped();
        }

        return new MetricsFilterWrapper(filter, metricsListener);
    }

    @Override
    public Servlet wrapServlet(Servlet servlet)
    {
        LOG.info("wrapServlet({})", servlet);
        Servlet unwrapped = servlet;
        while (unwrapped instanceof ServletHolder.Wrapper)
        {
            // Are we already wrapped somewhere along the line?
            if (unwrapped instanceof MetricsServletWrapper)
            {
                // If so, we are done. no need to wrap again.
                return servlet;
            }
            // Unwrap
            unwrapped = ((ServletHolder.Wrapper)unwrapped).getWrapped();
        }

        return new MetricsServletWrapper(servlet, metricsListener);
    }
}
