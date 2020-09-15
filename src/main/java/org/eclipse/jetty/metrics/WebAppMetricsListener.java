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

import javax.servlet.ServletContext;

import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

public interface WebAppMetricsListener extends ServletMetricsListener
{
    enum ConfigurationStep
    {
        PRE, MAIN, POST
    }

    /**
     * Event that a specific {@link Configuration} being applied to the {@link WebAppContext}
     *
     * @param context the specific context that was the configuration was applied to
     * @param configuration the configuration that was applied
     * @param configurationStep the configuration step
     */
    void onWebAppConfigureStart(WebAppContext context, Configuration configuration, ConfigurationStep configurationStep);

    /**
     * Event that a specific {@link Configuration} being applied to the {@link WebAppContext} has completed
     *
     * @param context the specific context that was the configuration was applied to
     * @param configuration the configuration that was applied
     * @param configurationStep the configuration step
     */
    void onWebAppConfigureFinished(WebAppContext context, Configuration configuration, ConfigurationStep configurationStep);

    /**
     * Event that the WebAppContext has started to be initialized
     *
     * <p>
     * This is similar to {@link #onServletContextStarting(ServletContext)}
     * and occurs at a point in time before the ServletContext starts to be initialized.
     * The difference in time between this event and {@link #onServletContextStarting(ServletContext)}
     * event is due to preconfigure timings for the WebApp itself.
     * This often includes things like the Bytecode / Annotation scanning in its overall timing.
     * </p>
     *
     * @param context the specific context that has started to be initialized
     * @see #onServletContextStarting(ServletContext)
     */
    void onWebAppStarting(WebAppContext context);

    /**
     * Event that the WebAppContext has completed initialization and is ready to serve requests
     *
     * <p>
     * This is similar to {@link ServletMetricsListener#onServletContextReady(ServletContext)}
     * but also includes the postconfigure timings for the WebApp itself.
     * </p>
     *
     * @param context the specific context that was started / initialized
     * @see #onServletContextReady(ServletContext)
     */
    void onWebAppReady(WebAppContext context);
}
