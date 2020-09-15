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
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Listener bean for obtaining events related to servlet initialization / timing, and
 * calls (to Filters and Servlets) within the defined `ServletContextHandler`.
 * <p>
 * This supplements the {@link org.eclipse.jetty.server.HttpChannel.Listener}
 * with events that represent behaviors within a specific `ServletContextHandler`.
 * </p>
 * <p>
 * If there is a Bean present on the `ServletContextHandler` implementing this
 * Listener it is used, otherwise the Server level Bean is used if present.
 * If no bean is discovered, no listener is notified.
 * </p>
 */
public interface ServletMetricsListener
{
    /**
     * Event that the ServletContext has started to be initialized
     *
     * @param servletContext the specific context that the was initialized.
     */
    void onServletContextStarting(ServletContext servletContext);

    /**
     * Event that the ServletContext has completed startup / initialization is now ready.
     *
     * @param servletContext the specific context that is now ready.
     */
    void onServletContextReady(ServletContext servletContext);

    /**
     * A specific Servlet is being started / initialized
     *
     * @param servletContext the specific context that the servlet belongs to.
     * @param servlet the Servlet that is being initialized.
     */
    void onServletStarting(ServletContext servletContext, Servlet servlet);

    /**
     * A specific Servlet is now ready to process requests.
     *
     * @param servletContext the specific context that the servlet belongs to.
     * @param servlet the Servlet that was initialized.
     * @param duration the duration for this specific servlet startup/initialization.
     */
    void onServletReady(ServletContext servletContext, Servlet servlet, Duration duration);

    /**
     * A specific Filter is being started / initialized.
     *
     * @param servletContext the specific context that the filter belongs to.
     * @param filter the Filter that is being initialized.
     */
    void onFilterStarting(ServletContext servletContext, Filter filter);

    /**
     * A specific Filter has completed startup and is now ready to handle requests.
     *
     * @param servletContext the specific context that the filter belongs to.
     * @param filter the Filter that was initialized.
     * @param duration the duration for this initialization.
     */
    void onFilterReady(ServletContext servletContext, Filter filter, Duration duration);

    /**
     * Event indicating a specific {@link javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)} was entered
     * from the Filter chain.
     *
     * @param servletContext the specific context that the filter belongs to.
     * @param filter the filter {@link Filter#doFilter(ServletRequest, ServletResponse, FilterChain)} that was entered
     * @param request the request that caused this filter to be entered.
     */
    void onFilterEnter(ServletContext servletContext, Filter filter, ServletRequest request);

    /**
     * Event indicating a specific {@link javax.servlet.Filter#doFilter(ServletRequest, ServletResponse, FilterChain)} was exited
     * from the Filter chain.
     *
     * @param servletContext the specific context that the filter belongs to.
     * @param filter the filter {@link Filter#doFilter(ServletRequest, ServletResponse, FilterChain)} that was exited
     * @param request the request that caused this filter to be exited.
     * @param duration the duration for this filter servicing.
     * @param cause if exit condition was a result of a throwable, this will be populate, it will be null if exit was normal
     */
    void onFilterExit(ServletContext servletContext, Filter filter, ServletRequest request, Duration duration, Throwable cause);

    /**
     * Event indicating a specific {@link javax.servlet.Servlet#service(ServletRequest, ServletResponse)} ` was entered.
     *
     * @param servletContext the specific context that the servlet belongs to.
     * @param servlet the servlet {@link Servlet#service(ServletRequest, ServletResponse)} that was entered.
     * @param request the request that entered this servlet.
     */
    void onServletEnter(ServletContext servletContext, Servlet servlet, ServletRequest request);

    /**
     * Event indicating a specific {@link javax.servlet.Servlet#service(ServletRequest, ServletResponse)} was exited.
     *
     * @param servletContext the specific context that the servlet belongs to.
     * @param servlet the servlet {@link Servlet#service(ServletRequest, ServletResponse)} that was exited.
     * @param request the request that exited this servlet.
     * @param duration the duration for this servlet servicing.
     * @param cause if exit condition was a result of a throwable, this will be populate, it will be null if exit was normal
     */
    void onServletExit(ServletContext servletContext, Servlet servlet, ServletRequest request, Duration duration, Throwable cause);
}
