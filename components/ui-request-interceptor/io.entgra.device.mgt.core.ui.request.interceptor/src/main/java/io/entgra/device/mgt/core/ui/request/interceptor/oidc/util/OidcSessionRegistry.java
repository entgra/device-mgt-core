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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.util;

import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcSessionManagementException;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.service.OidcSessionManagementService;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.service.impl.OidcSessionManagementServiceImpl;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Maintains node-local servlet-session references for immediate invalidation.
 *
 * <p>This registry is only an optimization. The SQL session mapping remains authoritative in clustered deployments.</p>
 */
public final class OidcSessionRegistry {
    private static final Log log = LogFactory.getLog(OidcSessionRegistry.class);
    private static final ConcurrentMap<String, HttpSession> SESSIONS = new ConcurrentHashMap<>();
    private static volatile OidcSessionManagementService sessionManagementService;

    private OidcSessionRegistry() { }

    public static void add(HttpSession session) {
        String applicationName = applicationName(session);
        SESSIONS.put(key(applicationName, session.getId()), session);
        if (log.isDebugEnabled()) {
            log.debug("Registered node-local OIDC session for application: " + applicationName);
        }
    }

    public static HttpSession remove(String applicationName, String sessionId) {
        HttpSession session = SESSIONS.remove(key(applicationName, sessionId));
        if (session != null && log.isDebugEnabled()) {
            log.debug("Removed node-local OIDC session for application: " + applicationName);
        }
        return session;
    }

    public static void remove(HttpSession session) {
        remove(applicationName(session), session.getId());
    }

    /**
     * Derives the registered application name from a request-handler servlet context.
     */
    public static String applicationName(String contextPath) {
        String name = contextPath == null ? "" : contextPath;
        if (name.startsWith("/")) { name = name.substring(1); }
        if (name.endsWith(HandlerConstants.UI_REQUEST_HANDLER_SUFFIX)) {
            name = name.substring(0, name.length() - HandlerConstants.UI_REQUEST_HANDLER_SUFFIX.length());
        }
        return name;
    }

    public static OidcSessionManagementService service() {
        if (sessionManagementService == null) {
            synchronized (OidcSessionRegistry.class) {
                if (sessionManagementService == null) {
                    try {
                        sessionManagementService = new OidcSessionManagementServiceImpl();
                        if (log.isDebugEnabled()) {
                            log.debug("Initialized OIDC session management service");
                        }
                    } catch (OidcSessionManagementException e) {
                        log.error("Unable to initialize OIDC session management", e);
                        return null;
                    }
                }
            }
        }
        return sessionManagementService;
    }

    private static String applicationName(HttpSession session) {
        return applicationName(session.getServletContext().getContextPath());
    }
    private static String key(String applicationName, String sessionId) { return applicationName + ':' + sessionId; }
}
