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
 * Database representation of the relationship between an OIDC provider session and a servlet session.
 */
public class OidcSessionMapping {
    private String issuer;
    private String clientId;
    private String sid;
    private String subject;
    private String applicationName;
    private String httpSessionId;
    private String status;
    private String logoutJti;
    private long createdAt;
    private long lastSeenAt;
    private long revokedAt;
    private long expiresAt;
    private long sessionTimeoutMillis;

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getSid() { return sid; }
    public void setSid(String sid) { this.sid = sid; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
    public String getHttpSessionId() { return httpSessionId; }
    public void setHttpSessionId(String httpSessionId) { this.httpSessionId = httpSessionId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLogoutJti() { return logoutJti; }
    public void setLogoutJti(String logoutJti) { this.logoutJti = logoutJti; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(long lastSeenAt) { this.lastSeenAt = lastSeenAt; }
    public long getRevokedAt() { return revokedAt; }
    public void setRevokedAt(long revokedAt) { this.revokedAt = revokedAt; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public long getSessionTimeoutMillis() { return sessionTimeoutMillis; }
    public void setSessionTimeoutMillis(long sessionTimeoutMillis) {
        this.sessionTimeoutMillis = sessionTimeoutMillis;
    }
}
