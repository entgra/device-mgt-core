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

package io.entgra.device.mgt.core.device.mgt.core.util;

import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

public class HttpReportingUtil {

    private static final Log log = LogFactory.getLog(HttpReportingUtil.class);
    private static final String IS_EVENT_PUBLISHING_ENABLED = "isEventPublishingEnabled";
    private static final String IS_TRACKER_ENABLED = "isTrackerEnabled";
    private static final String IS_LOCATION_PUBLISHING_ENABLED = "isLocationPublishingEnabled";
    private static final String TRACKER_SERVER_URI = "trackerServer";
    private static final String TRACKER_PASSWORD = "trackerPassword";
    private static final String TRACKER_USER = "trackerUsername";
    private static final String TRACKER_CONFIG = "locationPublishing";

    public static String getReportingHost() {
        return System.getProperty(DeviceManagementConstants.Report.REPORTING_EVENT_HOST);
    }


    public static boolean isPublishingEnabledForTenant() {
        Object configuration = DeviceManagerUtil.getConfiguration(IS_EVENT_PUBLISHING_ENABLED);
        if (configuration != null) {
            return Boolean.parseBoolean(configuration.toString());
        }
        return false;
    }

    public static boolean isLocationPublishing() {
        return getTrackerBooleanValues(IS_LOCATION_PUBLISHING_ENABLED);
    }

    public static boolean isTrackerEnabled() {
        return getTrackerBooleanValues(IS_TRACKER_ENABLED);
    }

    public static String trackerServer() {
        return getTrackerStringValues(TRACKER_SERVER_URI);
    }

    public static String trackerPassword() {
        return getTrackerStringValues(TRACKER_PASSWORD);
    }

    public static String trackerUser() {
        return getTrackerStringValues(TRACKER_USER);
    }

    public static boolean getTrackerBooleanValues(String trackerConfigKey) {
        Object configuration = DeviceManagerUtil.getConfiguration(TRACKER_CONFIG);
        if (configuration != null) {
            JSONObject locationConfig = new JSONObject(configuration.toString());
            if (locationConfig.has(trackerConfigKey)) {
                return Boolean.parseBoolean(locationConfig.get(trackerConfigKey).toString());
            }
        }
        return false;
    }

    public static String getTrackerStringValues(String trackerConfigKey) {
        Object configuration = DeviceManagerUtil.getConfiguration(TRACKER_CONFIG);
        if (configuration != null) {
            JSONObject locationConfig = new JSONObject(configuration.toString());
            if (locationConfig.has(trackerConfigKey)) {
                return locationConfig.get(trackerConfigKey).toString();
            }
        }
        return null;
    }
}
