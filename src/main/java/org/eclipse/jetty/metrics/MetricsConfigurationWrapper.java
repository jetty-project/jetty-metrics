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

public class MetricsConfigurationWrapper extends Configuration.Wrapper
{
    private final WebAppMetricsListener metricsListener;

    public MetricsConfigurationWrapper(Configuration configuration, WebAppMetricsListener metricsListener)
    {
        super(configuration);
        this.metricsListener = metricsListener;
    }

    @Override
    public void preConfigure(WebAppContext context) throws Exception
    {
        try
        {
            metricsListener.onWebAppConfigureStart(context, getWrapped(), WebAppMetricsListener.ConfigurationStep.PRE);
            super.preConfigure(context);
        }
        finally
        {
            metricsListener.onWebAppConfigureFinished(context, getWrapped(), WebAppMetricsListener.ConfigurationStep.PRE);
        }
    }

    @Override
    public void configure(WebAppContext context) throws Exception
    {
        try
        {
            metricsListener.onWebAppConfigureStart(context, getWrapped(), WebAppMetricsListener.ConfigurationStep.MAIN);
            super.configure(context);
        }
        finally
        {
            metricsListener.onWebAppConfigureFinished(context, getWrapped(), WebAppMetricsListener.ConfigurationStep.MAIN);
        }
    }

    @Override
    public void postConfigure(WebAppContext context) throws Exception
    {
        try
        {
            metricsListener.onWebAppConfigureStart(context, getWrapped(), WebAppMetricsListener.ConfigurationStep.POST);
            super.postConfigure(context);
        }
        finally
        {
            metricsListener.onWebAppConfigureFinished(context, getWrapped(), WebAppMetricsListener.ConfigurationStep.POST);
        }
    }
}
