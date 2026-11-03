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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.service;

import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.BackChannelLogoutResult;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.OidcSessionRegistration;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcSessionManagementException;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcTokenValidationException;

/**
 * Validates OIDC session tokens and coordinates registration, revocation, and lifecycle cleanup.
 */
public interface OidcSessionManagementService {

    boolean isEnabled();

    void registerSession(OidcSessionRegistration registration)
            throws OidcSessionManagementException, OidcTokenValidationException;

    BackChannelLogoutResult processLogoutToken(String logoutToken, String expectedClientId,
                                                String applicationName)
            throws OidcSessionManagementException, OidcTokenValidationException;

    boolean isSessionRevoked(String applicationName, String httpSessionId)
            throws OidcSessionManagementException;

    void removeSession(String applicationName, String httpSessionId)
            throws OidcSessionManagementException;
}
