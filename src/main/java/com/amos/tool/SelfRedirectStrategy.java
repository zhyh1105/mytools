package com.amos.tool;
/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */


import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.annotation.Immutable;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.Asserts;
import org.apache.http.util.TextUtils;

/**
 *  //自定义重定向策略
 */
public class SelfRedirectStrategy implements RedirectStrategy {

    private final Log log = LogFactory.getLog(getClass());

    /**
     * @deprecated (4.3) use {@link org.apache.http.client.protocol.HttpClientContext#REDIRECT_LOCATIONS}.
     */
    @Deprecated
    public static final String REDIRECT_LOCATIONS = "http.protocol.redirect-locations";

    public static final SelfRedirectStrategy INSTANCE = new SelfRedirectStrategy();

    /**
     * Redirectable methods.
     */
    private static final String[] REDIRECT_METHODS = new String[] {
            HttpGet.METHOD_NAME,
            HttpHead.METHOD_NAME,
            HttpPost.METHOD_NAME
    };

    public SelfRedirectStrategy() {
        super();
    }

    public boolean isRedirected(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws ProtocolException {
        Args.notNull(request, "HTTP request");
        Args.notNull(response, "HTTP response");

        final int statusCode = response.getStatusLine().getStatusCode();
        final String method = request.getRequestLine().getMethod();
        final Header locationHeader = response.getFirstHeader("location");
        switch (statusCode) {
            case HttpStatus.SC_MOVED_TEMPORARILY:
                return isRedirectable(method) && locationHeader != null;
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_TEMPORARY_REDIRECT:
                return isRedirectable(method);
            case HttpStatus.SC_SEE_OTHER:
                return true;
            default:
                return false;
        } //end of switch
    }

    public URI getLocationURI(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws ProtocolException {
        Args.notNull(request, "HTTP request");
        Args.notNull(response, "HTTP response");
        Args.notNull(context, "HTTP context");

        final HttpClientContext clientContext = HttpClientContext.adapt(context);

        //get the location header to find out where to redirect to
        final Header locationHeader = response.getFirstHeader("location");
        if (locationHeader == null) {
            // got a redirect response, but no location header
            throw new ProtocolException(
                    "Received redirect response " + response.getStatusLine()
                            + " but no location header");
        }
        final String location = locationHeader.getValue();
        if (this.log.isDebugEnabled()) {
            this.log.debug("Redirect requested to location '" + location + "'");
        }

        final RequestConfig config = clientContext.getRequestConfig();

        URI uri = createLocationURI(location.replaceAll(" ", "%20"));


        // rfc2616 demands the location value be a complete URI
        // Location       = "Location" ":" absoluteURI
        try {
            if (!uri.isAbsolute()) {
                if (!config.isRelativeRedirectsAllowed()) {
                    throw new ProtocolException("Relative redirect location '"
                            + uri + "' not allowed");
                }
                // Adjust location URI
                final HttpHost target = clientContext.getTargetHost();
                Asserts.notNull(target, "Target host");
                final URI requestURI = new URI(request.getRequestLine().getUri());
                final URI absoluteRequestURI = URIUtils.rewriteURI(requestURI, target, false);
                uri = URIUtils.resolve(absoluteRequestURI, uri);
            }
        } catch (final URISyntaxException ex) {
            throw new ProtocolException(ex.getMessage(), ex);
        }

        RedirectLocations redirectLocations = (RedirectLocations) clientContext.getAttribute(
                HttpClientContext.REDIRECT_LOCATIONS);
        if (redirectLocations == null) {
            redirectLocations = new RedirectLocations();
            context.setAttribute(HttpClientContext.REDIRECT_LOCATIONS, redirectLocations);
        }
        if (!config.isCircularRedirectsAllowed()) {
            if (redirectLocations.contains(uri)) {
                throw new CircularRedirectException("Circular redirect to '" + uri + "'");
            }
        }
        redirectLocations.add(uri);
        return uri;
    }

    /**
     * @since 4.1
     */
    protected URI createLocationURI(final String location) throws ProtocolException {

        System.out.println("redirect_location:"+location);

        try {
            final URIBuilder b = new URIBuilder(new URI(location).normalize());
            final String host = b.getHost();
            if (host != null) {
                b.setHost(host.toLowerCase(Locale.ENGLISH));
            }
            final String path = b.getPath();
            if (TextUtils.isEmpty(path)) {
                b.setPath("/");
            }
            return b.build();
        } catch (final URISyntaxException ex) {
            throw new ProtocolException("Invalid redirect URI: " + location, ex);
        }
    }

    /**
     * @since 4.2
     */
    protected boolean isRedirectable(final String method) {
        for (final String m: REDIRECT_METHODS) {
            if (m.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    public HttpUriRequest getRedirect(
            final HttpRequest request,
            final HttpResponse response,
            final HttpContext context) throws ProtocolException {
        final URI uri = getLocationURI(request, response, context);
        final String method = request.getRequestLine().getMethod();
        if (method.equalsIgnoreCase(HttpHead.METHOD_NAME)) {
            return new HttpHead(uri);
        } else if (method.equalsIgnoreCase(HttpGet.METHOD_NAME)) {
            return new HttpGet(uri);
        } else if (method.equalsIgnoreCase(HttpPost.METHOD_NAME)) {
            return new HttpPost(uri);
        } else {
            final int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_TEMPORARY_REDIRECT) {
                return RequestBuilder.copy(request).setUri(uri).build();
            } else {
                return new HttpGet(uri);
            }
        }
    }

}
