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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.config;

/**
 * Configuration used by the UI request interceptor to validate OIDC ID and back-channel logout tokens.
 */
public class BackChannelLogoutConfiguration {
    private boolean enabled;
    private String issuer;
    private String jwksUri;
    private int allowedClockSkewSeconds = 60;
    private long staleMappingRetentionSeconds = 86400;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public String getJwksUri() { return jwksUri; }
    public void setJwksUri(String jwksUri) { this.jwksUri = jwksUri; }
    public int getAllowedClockSkewSeconds() { return allowedClockSkewSeconds; }
    public void setAllowedClockSkewSeconds(int allowedClockSkewSeconds) {
        this.allowedClockSkewSeconds = allowedClockSkewSeconds;
    }
    public long getStaleMappingRetentionSeconds() { return staleMappingRetentionSeconds; }
    public void setStaleMappingRetentionSeconds(long staleMappingRetentionSeconds) {
        this.staleMappingRetentionSeconds = staleMappingRetentionSeconds;
    }
}
