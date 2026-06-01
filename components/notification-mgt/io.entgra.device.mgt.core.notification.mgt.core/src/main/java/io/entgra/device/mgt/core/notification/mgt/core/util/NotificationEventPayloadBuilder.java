/*
 *  Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.notification.mgt.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Builds JSON payloads for {@link NotificationEventBroker} push events (message and unread count).
 */
public final class NotificationEventPayloadBuilder {

    private static final Gson GSON = new Gson();

    private NotificationEventPayloadBuilder() {
    }

    public static String buildMessagePayload(String message, int unreadCount) {
        JsonObject json = new JsonObject();
        json.addProperty("message", message);
        json.addProperty("unreadCount", unreadCount);
        return GSON.toJson(json);
    }

    public static String buildUnreadCountPayload(int unreadCount) {
        JsonObject json = new JsonObject();
        json.addProperty("unreadCount", unreadCount);
        return GSON.toJson(json);
    }
}
