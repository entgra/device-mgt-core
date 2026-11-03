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
package io.entgra.device.mgt.core.ui.request.interceptor;

import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.BackChannelLogoutResult;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcSessionManagementException;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcTokenValidationException;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.service.OidcSessionManagementService;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.util.OidcSessionRegistry;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Receives OIDC back-channel logout notifications from the identity provider.
 *
 * <p>The persistent mapping is revoked before this handler attempts node-local servlet-session invalidation.
 * Sessions owned by another node are rejected by {@code OidcSessionStateFilter} on their next request.</p>
 */
@WebServlet(HandlerConstants.BACK_CHANNEL_LOGOUT)
public class OidcBackChannelLogoutHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(OidcBackChannelLogoutHandler.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String contentType = req.getContentType();
        String[] tokens = req.getParameterValues(HandlerConstants.LOGOUT_TOKEN);
        if (contentType == null || !contentType.toLowerCase().startsWith("application/x-www-form-urlencoded") ||
                tokens == null || tokens.length != 1 || tokens[0] == null || tokens[0].trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "A single form-encoded logout_token is required");
            return;
        }
        OidcSessionManagementService service = OidcSessionRegistry.service();
        if (service == null || !service.isEnabled()) {
            log.error("OIDC back-channel logout request cannot be processed because session management is unavailable");
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "OIDC back-channel logout service is unavailable");
            return;
        }
        String applicationName = OidcSessionRegistry.applicationName(req.getContextPath());
        try {
            BackChannelLogoutResult result = service.processLogoutToken(tokens[0], null, applicationName);
            int locallyInvalidatedCount = 0;
            for (String httpSessionId : result.getHttpSessionIds()) {
                HttpSession session = OidcSessionRegistry.remove(applicationName, httpSessionId);
                if (session != null) {
                    try {
                        session.invalidate();
                        locallyInvalidatedCount++;
                    } catch (IllegalStateException e) {
                        if (log.isDebugEnabled()) {
                            log.debug("OIDC session was already invalidated locally for application: " +
                                    applicationName, e);
                        }
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Processed OIDC back-channel logout for application: " + applicationName +
                        ", mapping found: " + result.isMappingFound() +
                        ", locally invalidated sessions: " + locallyInvalidatedCount);
            }
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (OidcTokenValidationException e) {
            log.warn("Rejected invalid OIDC back-channel logout token: " + e.getMessage());
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid logout token");
        } catch (OidcSessionManagementException e) {
            log.error("Unable to process OIDC back-channel logout", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to process back-channel logout");
        }
    }
}
