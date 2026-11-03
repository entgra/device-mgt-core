/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.ui.request.interceptor.oidc;

import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcSessionManagementException;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.service.OidcSessionManagementService;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.util.OidcSessionRegistry;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Enforces SQL-backed OIDC session state for authenticated request-handler traffic.
 *
 * <p>This filter provides cluster-safe logout: the receiving node invalidates a local session immediately, while a
 * session on another node is invalidated when that node handles its next request.</p>
 */
public class OidcSessionStateFilter implements Filter {
    private static final Log log = LogFactory.getLog(OidcSessionStateFilter.class);
    private static final Set<String> EXCLUDED_PATHS = new HashSet<>(Arrays.asList(
            "/ssoLogin", HandlerConstants.SSO_LOGIN_CALLBACK, HandlerConstants.BACK_CHANNEL_LOGOUT,
            "/login", "/logout", "/ssoLogout"));

    @Override public void init(FilterConfig filterConfig) { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if (EXCLUDED_PATHS.contains(req.getServletPath())) { chain.doFilter(request, response); return; }
        HttpSession session = req.getSession(false);
        if (session == null || !Boolean.TRUE.equals(session.getAttribute(HandlerConstants.OIDC_MAPPED_SESSION))) {
            chain.doFilter(request, response);
            return;
        }
        OidcSessionManagementService service = OidcSessionRegistry.service();
        if (service == null) {
            log.error("Unable to verify OIDC session state because session management is unavailable");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OIDC session service is unavailable");
            return;
        }
        String applicationName = OidcSessionRegistry.applicationName(req.getContextPath());
        try {
            if (service.isSessionRevoked(applicationName, session.getId())) {
                OidcSessionRegistry.remove(applicationName, session.getId());
                try {
                    session.invalidate();
                } catch (IllegalStateException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Revoked OIDC session was already invalidated for application: " +
                                applicationName, e);
                    }
                }
                log.info("Rejected request for a revoked OIDC session in application: " + applicationName);
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session has been logged out");
                return;
            }
            chain.doFilter(request, response);
        } catch (OidcSessionManagementException e) {
            log.error("Unable to verify OIDC session state", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to verify session state");
        }
    }

    @Override public void destroy() { }
}
