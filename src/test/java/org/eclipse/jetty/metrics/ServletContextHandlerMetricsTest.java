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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.metrics.servlets.HelloServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ServletContextHandlerMetricsTest
{
    private Server server;
    private HttpClient client;

    @BeforeEach
    public void setUp() throws Exception
    {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(0);
        server.addConnector(connector);

        client = new HttpClient();
        client.start();
    }

    @AfterEach
    public void tearDown()
    {
        LifeCycle.stop(client);
        LifeCycle.stop(server);
    }

    @Test
    public void testSimpleServlet() throws Exception
    {
        ServletMetricsCaptureListener captureListener = new ServletMetricsCaptureListener();
        MetricsHandler metricsHandler = new MetricsHandler(captureListener);

        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        contextHandler.addBean(metricsHandler);
        contextHandler.addServlet(HelloServlet.class, "/hello");

        metricsHandler.addToAllConnectors(server);
        metricsHandler.addToContext(contextHandler);
        server.setHandler(contextHandler);
        server.start();

        ContentResponse response = client.GET(server.getURI().resolve("/hello"));
        assertThat("Response.status", response.getStatus(), is(HttpStatus.OK_200));

        List<String> expectedEvents = new ArrayList<>();
        expectedEvents.add("onServletContextStarting()");
        expectedEvents.add("onServletContextReady()");
        expectedEvents.add("onServletStarting()");
        expectedEvents.add("onServletReady()");
        expectedEvents.add("onServletEnter()");
        expectedEvents.add("onServletExit()");

        assertThat("Metrics Events Count", captureListener.getEvents().size(), is(expectedEvents.size()));
    }

    @Test
    public void testSimpleFilterAndServlet() throws Exception
    {
        ServletMetricsCaptureListener captureListener = new ServletMetricsCaptureListener();
        MetricsHandler metricsHandler = new MetricsHandler(captureListener);

        ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
        contextHandler.addServlet(HelloServlet.class, "/hello");
        contextHandler.addFilter(FooFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        metricsHandler.addToAllConnectors(server);
        metricsHandler.addToContext(contextHandler);
        server.setHandler(contextHandler);
        server.start();

        ContentResponse response = client.GET(server.getURI().resolve("/hello"));
        assertThat("Response.status", response.getStatus(), is(HttpStatus.OK_200));

        List<String> expectedEvents = new ArrayList<>();
        expectedEvents.add("onServletContextStarting()");
        expectedEvents.add("onFilterStarting()");
        expectedEvents.add("onFilterReady()");
        expectedEvents.add("onServletContextReady()");
        expectedEvents.add("onServletStarting()");
        expectedEvents.add("onServletReady()");
        expectedEvents.add("onFilterEnter()");
        expectedEvents.add("onServletEnter()");
        expectedEvents.add("onServletExit()");
        expectedEvents.add("onFilterExit()");

        assertThat("Metrics Events Count", captureListener.getEvents().size(), is(expectedEvents.size()));
    }
}
