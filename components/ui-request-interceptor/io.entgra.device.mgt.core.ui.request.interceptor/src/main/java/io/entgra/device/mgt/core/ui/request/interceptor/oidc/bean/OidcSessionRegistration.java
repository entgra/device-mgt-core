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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean;

/**
 * Carries the validated SSO callback context needed to create a persistent OIDC session mapping.
 * The ID token is used only during validation and is never persisted.
 */
public class OidcSessionRegistration {

    private final String idToken;
    private final String clientId;
    private final String applicationName;
    private final String httpSessionId;
    private final long sessionTimeoutMillis;

    public OidcSessionRegistration(String idToken, String clientId, String applicationName,
                                   String httpSessionId, long sessionTimeoutMillis) {
        this.idToken = idToken;
        this.clientId = clientId;
        this.applicationName = applicationName;
        this.httpSessionId = httpSessionId;
        this.sessionTimeoutMillis = sessionTimeoutMillis;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getClientId() {
        return clientId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getHttpSessionId() {
        return httpSessionId;
    }

    public long getSessionTimeoutMillis() {
        return sessionTimeoutMillis;
    }
}
