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

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Removes active SQL mappings when a mapped servlet session expires or is invalidated normally.
 * Revoked mappings are deliberately retained until their retention time expires so repeated logout tokens remain
 * idempotent.
 */
public class OidcSessionListener implements HttpSessionListener {
    private static final Log log = LogFactory.getLog(OidcSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent event) { }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        boolean mappedSession = Boolean.TRUE.equals(
                event.getSession().getAttribute(HandlerConstants.OIDC_MAPPED_SESSION));
        OidcSessionRegistry.remove(event.getSession());
        if (!mappedSession) {
            return;
        }
        String applicationName = OidcSessionRegistry.applicationName(
                event.getSession().getServletContext().getContextPath());
        OidcSessionManagementService service = OidcSessionRegistry.service();
        if (service == null || !service.isEnabled()) {
            log.warn("Unable to remove destroyed OIDC session mapping because session management is unavailable " +
                    "for application: " + applicationName);
            return;
        }
        try {
            service.removeSession(applicationName, event.getSession().getId());
            if (log.isDebugEnabled()) {
                log.debug("Removed destroyed OIDC session mapping for application: " + applicationName);
            }
        } catch (OidcSessionManagementException e) {
            log.error("Failed to remove destroyed OIDC session mapping for application: " + applicationName, e);
        }
    }
}
