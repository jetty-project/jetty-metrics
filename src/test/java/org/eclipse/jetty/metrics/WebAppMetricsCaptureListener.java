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

import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

public class WebAppMetricsCaptureListener extends ServletMetricsCaptureListener implements WebAppMetricsListener
{
    @Override
    public void onWebAppConfigureStart(WebAppContext context, Configuration configuration, ConfigurationStep configurationStep)
    {
        addEvent("onWebAppConfigureStart(), configuration=%s, configurationStep=%s, context=%s", configuration, configurationStep, context);
    }

    @Override
    public void onWebAppConfigureFinished(WebAppContext context, Configuration configuration, ConfigurationStep configurationStep)
    {
        addEvent("onWebAppConfigureFinished(), configuration=%s, configurationStep=%s, context=%s", configuration, configurationStep, context);
    }

    @Override
    public void onWebAppStarting(WebAppContext context)
    {
        addEvent("onWebAppStarting(), context=%s", context);
    }

    @Override
    public void onWebAppReady(WebAppContext context)
    {
        addEvent("onWebAppReady(), context=%s", context);
    }
}
