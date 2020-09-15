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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.metrics.servlets.CrossContextIncludeServlet;
import org.eclipse.jetty.metrics.servlets.HelloServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.toolchain.test.FS;
import org.eclipse.jetty.toolchain.test.jupiter.WorkDir;
import org.eclipse.jetty.toolchain.test.jupiter.WorkDirExtension;
import org.eclipse.jetty.util.IO;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith(WorkDirExtension.class)
public class WebAppContextMetricsTest
{
    public WorkDir workDir;
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

    private void enableByteCodeScanning()
    {
        Configuration.ClassList classlist = Configuration.ClassList
            .setServerDefault(server);

        classlist.addBefore(
            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
            "org.eclipse.jetty.annotations.AnnotationConfiguration");
    }

    private Path createWebApp(Path baseDir, Class<?>... classesToCopy) throws IOException
    {
        FS.ensureDirExists(baseDir);
        Path webinfDir = baseDir.resolve("WEB-INF");
        Path classesDir = webinfDir.resolve("classes");
        FS.ensureDirExists(classesDir);

        for (Class<?> clazz : classesToCopy)
        {
            copyClassToWebInfClasses(clazz, classesDir);
        }

        return baseDir;
    }

    @Test
    public void testBasicBytecodeScan() throws Exception
    {
        Path webappDir = createWebApp(workDir.getEmptyPathDir(), HelloServlet.class);

        enableByteCodeScanning();

        WebAppMetricsCaptureListener captureListener = new WebAppMetricsCaptureListener();
        MetricsHandler metricsHandler = new MetricsHandler(captureListener);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.addBean(metricsHandler);
        webapp.setWarResource(new PathResource(webappDir));

        metricsHandler.addToAllConnectors(server);
        metricsHandler.addToContext(webapp);
        server.setHandler(webapp);
        server.start();

        ContentResponse response = client.GET(server.getURI().resolve("/hello"));
        assertThat("Response.status", response.getStatus(), is(HttpStatus.OK_200));

        Class<?>[] configurationClasses = {
            WebInfConfiguration.class,
            WebXmlConfiguration.class,
            MetaInfConfiguration.class,
            FragmentConfiguration.class,
            AnnotationConfiguration.class,
            JettyWebXmlConfiguration.class
        };

        List<String> expectedEvents = new ArrayList<>();
        expectedEvents.add("onWebAppStarting()");
        expectedEvents.add("onServletContextStarting()");
        addExpectedConfigurations(expectedEvents, WebAppMetricsListener.ConfigurationStep.PRE, configurationClasses);
        addExpectedConfigurations(expectedEvents, WebAppMetricsListener.ConfigurationStep.MAIN, configurationClasses);
        expectedEvents.add("onServletStarting() - DefaultServlet");
        expectedEvents.add("onServletReady() - DefaultServlet");
        expectedEvents.add("onServletStarting() - NoJspServlet");
        expectedEvents.add("onServletReady() - NoJspServlet");
        addExpectedConfigurations(expectedEvents, WebAppMetricsListener.ConfigurationStep.POST, configurationClasses);
        expectedEvents.add("onWebAppReady()");
        expectedEvents.add("onServletContextReady()");
        expectedEvents.add("onServletStarting() - HelloServlet");
        expectedEvents.add("onServletReady() - HelloServlet");
        expectedEvents.add("onServletEnter()");
        expectedEvents.add("onServletExit()");

        assertThat("Metrics Events Count", captureListener.getEvents().size(), is(expectedEvents.size()));
    }

    @Test
    @Disabled
    public void testCrossContextInclude() throws Exception
    {
        Path testDir = workDir.getEmptyPathDir();

        Path webappADir = createWebApp(
            testDir.resolve("webapp-a"),
            HelloServlet.class);
        Path webappBDir = createWebApp(
            testDir.resolve("webapp-b"),
            CrossContextIncludeServlet.class);

        enableByteCodeScanning();

        WebAppMetricsCaptureListener captureListener = new WebAppMetricsCaptureListener();
        MetricsHandler metricsHandler = new MetricsHandler(captureListener);

        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/a");
        webapp.addBean(metricsHandler);
        webapp.setWarResource(new PathResource(webappADir));

        metricsHandler.addToAllConnectors(server);
        metricsHandler.addToContext(webapp);
        server.setHandler(webapp);
        server.start();

        ContentResponse response = client.GET(server.getURI().resolve("/hello"));
        assertThat("Response.status", response.getStatus(), is(HttpStatus.OK_200));

        Class<?>[] configurationClasses = {
            WebInfConfiguration.class,
            WebXmlConfiguration.class,
            MetaInfConfiguration.class,
            FragmentConfiguration.class,
            AnnotationConfiguration.class,
            JettyWebXmlConfiguration.class
        };

        List<String> expectedEvents = new ArrayList<>();
        expectedEvents.add("onWebAppStarting()");
        expectedEvents.add("onServletContextStarting()");
        addExpectedConfigurations(expectedEvents, WebAppMetricsListener.ConfigurationStep.PRE, configurationClasses);
        addExpectedConfigurations(expectedEvents, WebAppMetricsListener.ConfigurationStep.MAIN, configurationClasses);
        expectedEvents.add("onServletStarting() - DefaultServlet");
        expectedEvents.add("onServletReady() - DefaultServlet");
        expectedEvents.add("onServletStarting() - NoJspServlet");
        expectedEvents.add("onServletReady() - NoJspServlet");
        addExpectedConfigurations(expectedEvents, WebAppMetricsListener.ConfigurationStep.POST, configurationClasses);
        expectedEvents.add("onWebAppReady()");
        expectedEvents.add("onServletContextReady()");
        expectedEvents.add("onServletStarting() - HelloServlet");
        expectedEvents.add("onServletReady() - HelloServlet");
        expectedEvents.add("onServletEnter()");
        expectedEvents.add("onServletExit()");

        assertThat("Metrics Events Count", captureListener.getEvents().size(), is(expectedEvents.size()));
    }

    private void addExpectedConfigurations(List<String> expectedEvents, WebAppMetricsListener.ConfigurationStep configStep, Class<?>[] configurationClasses)
    {
        for (Class<?> configClass : configurationClasses)
        {
            expectedEvents.add(String.format("onWebAppConfigureStart() - %s:%s", configStep, configClass.getName()));
            expectedEvents.add(String.format("onWebAppConfigureFinished() - %s:%s", configStep, configClass.getName()));
        }
    }

    private void copyClassToWebInfClasses(Class<?> clazz, Path classesDir) throws IOException
    {
        String classRef = clazz.getName().replace('.', '/') + ".class";
        URL url = this.getClass().getResource('/' + classRef);
        MatcherAssert.assertThat("URL to class " + classRef, url, is(not(nullValue())));
        Path destPath = classesDir.resolve(classRef);
        FS.ensureDirExists(destPath.getParent());
        try (InputStream in = url.openStream();
             OutputStream out = Files.newOutputStream(destPath))
        {
            IO.copy(in, out);
        }
    }
}

