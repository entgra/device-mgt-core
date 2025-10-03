/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.dto.FCMCredentials;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.util.FCMUtil;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationContext;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.NotificationStrategy;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationConfig;
import io.entgra.device.mgt.core.device.mgt.common.push.notification.PushNotificationExecutionFailedException;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.internal.FCMDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.util.FCMUtil;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;

public class FCMNotificationStrategy implements NotificationStrategy {

    private static final Log log = LogFactory.getLog(FCMNotificationStrategy.class);
    private static final String NOTIFIER_TYPE_FCM = "FCM";
    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String FCM_API_KEY = "fcmAPIKey";
    private static final int TIME_TO_LIVE = 2419199; // 1 second less than 28 days
    private static final int HTTP_STATUS_CODE_OK = 200;
    private static final int HTTP_STATUS_CODE_NOT_FOUND = 404;
    private final PushNotificationConfig config;
    private static final String FCM_ENDPOINT_KEY = "FCM_SERVER_ENDPOINT";

    public FCMNotificationStrategy(PushNotificationConfig config) {
        this.config = config;
    }

    @Override
    public void init() {

    }

    @Override
    public void execute(NotificationContext ctx) throws PushNotificationExecutionFailedException {
        try {
            if (NOTIFIER_TYPE_FCM.equals(config.getType())) {
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
                Device device = FCMDataHolder.getInstance().getDeviceManagementProviderService()
                        .getDeviceWithTypeProperties(ctx.getDeviceId());
                if(device.getProperties() != null && getFCMToken(device.getProperties()) != null) {
                    sendWakeUpCall(FCMUtil.getInstance().getFCMCredentials(tenantId),
                            getFCMToken(device.getProperties()), device.getDeviceIdentifier(), tenantId);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Not using FCM notifier as notifier type is set to " + config.getType() +
                            " in Platform Configurations.");
                }
            }
        } catch (DeviceManagementException e) {
            throw new PushNotificationExecutionFailedException("Error occurred while retrieving device information", e);
        } catch (IOException e) {
            throw new PushNotificationExecutionFailedException("Error occurred while sending push notification", e);
        }
    }


    /**
     * Send FCM message to the FCM server to initiate the push notification
     * @param fcmCredentials FCM Credentials of the tenant containing the OAuth token and the FCM URL
     * @param registrationId Registration ID of the device
     * @param deviceIdentifier identifier of the device
     * @param tenantId ID of the tenant
     * @throws IOException If an error occurs while sending the request to the FCM server
     * @throws PushNotificationExecutionFailedException If an error occurs while sending the push notification
     */
    private void sendWakeUpCall(FCMCredentials fcmCredentials, String registrationId,
                                String deviceIdentifier, int tenantId) throws IOException,
            PushNotificationExecutionFailedException {

        if (null == fcmCredentials) {
            String msg = "FCM credentials not found. Push notification will not be sent to the device "
                    + deviceIdentifier + " in tenant " + tenantId;
            log.error(msg);
            throw new PushNotificationExecutionFailedException(msg);
        }

        String accessToken;
        String fcmServerEndpoint;
        try {
            fcmCredentials.getOauthCredentials().refreshIfExpired();
            accessToken = fcmCredentials.getOauthCredentials().getAccessToken().getTokenValue();
            fcmServerEndpoint = fcmCredentials.getFcmUrl();
        } catch (IllegalStateException e) {
            String msg = "FCM configuration error occurred while sending push notification to the device "
                    + deviceIdentifier + " in tenant " + fcmCredentials.getTenantId();
            log.error(msg, e);
            throw new PushNotificationExecutionFailedException(msg, e);
        }

        RequestBody fcmRequest = getFCMRequest(registrationId);
        Request request = new Request.Builder()
                .url(fcmServerEndpoint)
                .post(fcmRequest)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();
        try (Response response = FCMUtil.getInstance().getHttpClient().newCall(request).execute()) {
            if (log.isDebugEnabled()) {
                log.debug("FCM message sent to the FCM server. Response code: " + response.code()
                        + " Response message : " + response.message());
            }
            if(!response.isSuccessful()) {
                if (response.code() == HTTP_STATUS_CODE_NOT_FOUND) {
                    if (log.isDebugEnabled()) {
                        log.debug("The device " + deviceIdentifier + "with FCM registration ID: "
                                + registrationId + " was not registered in Google FCM servers, Or " +
                                "the registration ID is expired");
                    }
                    return;
                }
                String msg = "Received FCM Response [Status: " + response.code() + ", Response Message: "
                        + response.message() + "] for the device " + deviceIdentifier;
                log.error(msg);
                throw new IOException(msg);
            }
        }
    }

    /**
     * Get the FCM request as a JSON string
     * @param registrationId Registration ID of the device
     * @return FCM request as a JSON string
     */
    private static RequestBody getFCMRequest(String registrationId) {
        JsonObject messageObject = new JsonObject();
        messageObject.addProperty("token", registrationId);

        JsonObject fcmRequest = new JsonObject();
        fcmRequest.add("message", messageObject);

        return RequestBody.create(fcmRequest.toString(), okhttp3.MediaType.parse("application/json"));
    }

    @Override
    public NotificationContext buildContext() {
        return null;
    }

    @Override
    public void undeploy() {

    }

    private static String getFCMToken(List<Device.Property> properties) {
        String fcmToken = null;
        for (Device.Property property : properties) {
            if (FCM_TOKEN.equals(property.getName())) {
                fcmToken = property.getValue();
                break;
            }
        }
        return fcmToken;
    }

    @Override
    public PushNotificationConfig getConfig() {
        return config;
    }
}
