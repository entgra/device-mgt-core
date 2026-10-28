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

package io.entgra.device.mgt.core.realtime.notification.mgt.core.util;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.dto.RealtimeNotificationContext;
import java.util.Collections;
import java.util.List;

/**
 * Builds user-visible notification descriptions from persisted context JSON.
 */
public final class RealtimeNotificationMessageRenderer {

    private static final Gson GSON = new Gson();

    private RealtimeNotificationMessageRenderer() {
    }

    public static RealtimeNotificationContext parseContext(String notificationContextJson) {
        if (notificationContextJson == null || notificationContextJson.trim().isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(notificationContextJson, RealtimeNotificationContext.class);
        } catch (JsonSyntaxException e) {
            RealtimeNotificationContext fallback = new RealtimeNotificationContext();
            fallback.setMessage(notificationContextJson);
            return fallback;
        }
    }

    public static String toJson(RealtimeNotificationContext context) {
        if (context == null) {
            return null;
        }
        return GSON.toJson(context);
    }

    /**
     * @param context          persisted notification context
     * @param notificationType notification type (e.g. operation, task)
     */
    public static String render(RealtimeNotificationContext context, String notificationType) {
        if (context == null) {
            return "";
        }
        if (RealtimeConstants.TASK.equalsIgnoreCase(notificationType) && isNotBlank(context.getMessage())) {
            return context.getMessage();
        }
        if (isNotBlank(context.getMessage()) && !isNotBlank(context.getOperationCode())) {
            return context.getMessage();
        }
        return renderOperationNotification(context);
    }

    /**
     * Renders an operation notification from context fields and {@code deviceLabels}.
     * Device names/identifiers must already be present on the context when the list
     * should be shown; raw device primary keys are never used in the message.
     */
    public static String renderOperationNotification(RealtimeNotificationContext context) {
        if (context == null) {
            return "";
        }
        String code = context.getOperationCode() != null ? context.getOperationCode() : "";
        String configDescription = context.getConfigDescription() != null ? context.getConfigDescription() : "";
        String deviceType = context.getDeviceType() != null ? context.getDeviceType() : "";
        String status = context.getOperationStatus() != null ? context.getOperationStatus() : "";
        boolean includeDeviceList = context.getIncludeDeviceList() == null || context.getIncludeDeviceList();
        List<String> labels = context.getDeviceLabels() != null ? context.getDeviceLabels() : Collections.emptyList();
        if (!includeDeviceList || labels.isEmpty()) {
            return String.format("The operation %s (%s) for devices of type %s is %s.",
                    code, configDescription, deviceType, status);
        }
        if (labels.size() == 1) {
            return String.format("The operation %s (%s) for device %s of type %s is %s.",
                    code, configDescription, labels.get(0), deviceType, status);
        }
        return String.format("The operation %s (%s) for devices %s of type %s is %s.",
                code, configDescription, labels, deviceType, status);
    }

    private static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
