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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao;

import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.OidcSessionMapping;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcSessionManagementDAOException;

import java.util.List;

/**
 * Persistence operations for OIDC session mappings. Callers own transaction and connection boundaries.
 */
public interface OidcSessionManagementDAO {
    void addSessionState(OidcSessionMapping mapping) throws OidcSessionManagementDAOException;
    int updateActiveSessionState(OidcSessionMapping mapping) throws OidcSessionManagementDAOException;
    OidcSessionMapping getSessionStateForUpdate(String issuer, String clientId, String sid, String applicationName)
            throws OidcSessionManagementDAOException;
    void addSessionMapping(OidcSessionMapping mapping) throws OidcSessionManagementDAOException;
    int updateSessionMapping(OidcSessionMapping mapping) throws OidcSessionManagementDAOException;
    List<String> getHttpSessionIds(String issuer, String clientId, String sid, String applicationName)
            throws OidcSessionManagementDAOException;
    boolean isRevoked(String applicationName, String httpSessionId, long now, long touchBefore)
            throws OidcSessionManagementDAOException;
    void revoke(String issuer, String clientId, String sid, String applicationName, String jti, long revokedAt,
                long expiresAt)
            throws OidcSessionManagementDAOException;
    void deleteActiveBySession(String applicationName, String httpSessionId)
            throws OidcSessionManagementDAOException;
    boolean claimCleanup(String taskName, long lastRunAt, long runBefore)
            throws OidcSessionManagementDAOException;
    void deleteExpired(long timestamp) throws OidcSessionManagementDAOException;
}
