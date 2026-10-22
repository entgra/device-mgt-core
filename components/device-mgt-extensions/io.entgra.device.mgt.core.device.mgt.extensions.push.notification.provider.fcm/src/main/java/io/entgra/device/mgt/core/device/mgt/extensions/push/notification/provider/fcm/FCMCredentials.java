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

package io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm;

import com.google.auth.oauth2.GoogleCredentials;

public class FCMCredentials {
    private final GoogleCredentials oauthCredentials;
    private final String projectId;
    private final String defaultFCMUrl;
    private final int tenantId;

    public FCMCredentials(GoogleCredentials oauthCredentials, String projectId, String defaultFCMUrl, int tenantId) {
        this.oauthCredentials = oauthCredentials;
        this.projectId = projectId;
        this.defaultFCMUrl = defaultFCMUrl;
        this.tenantId = tenantId;
    }

    public GoogleCredentials getOauthCredentials() {
        if (oauthCredentials == null) {
            throw new IllegalStateException("OAuth Credentials are not set in FCM Credentials");
        }
        return oauthCredentials;
    }

    public String getFcmUrl() {
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalStateException("Project ID is not set in FCM Credentials");
        }
        return defaultFCMUrl.replace("project_id", projectId);
    }

    public String getProjectId() {
        return projectId;
    }

    public String getDefaultFCMUrl() {
        return defaultFCMUrl;
    }

    public int getTenantId() {
        return tenantId;
    }
}
