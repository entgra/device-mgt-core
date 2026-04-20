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

package io.entgra.device.mgt.core.device.mgt.core.report.mgt.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceLogEntryBean;
import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceLogPayloadBean;
import io.entgra.device.mgt.core.device.mgt.common.device.details.LogsDetailsWrapper;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import java.util.ArrayList;
import java.util.List;

public class DeviceLogReportUtil {

    public static LogsDetailsWrapper createLogsWrapper(Device device, JsonArray payloadArray) {
        List<DeviceLogPayloadBean> payload = new ArrayList<>();

        for (JsonElement element : payloadArray) {
            JsonObject payloadObj = element.getAsJsonObject();
            DeviceLogPayloadBean logPayload = new DeviceLogPayloadBean();
            logPayload.setLogType(payloadObj.get("LOG_TYPE").getAsString());

            JsonArray logDataArray = payloadObj.getAsJsonArray("LOG_DATA");
            List<DeviceLogEntryBean> logData = new ArrayList<>();

            for (JsonElement logDataElement : logDataArray) {
                JsonObject logDataObj = logDataElement.getAsJsonObject();
                DeviceLogEntryBean entry = new DeviceLogEntryBean();
                entry.setTimestamp(logDataObj.get("TIMESTAMP").getAsLong());
                entry.setData(logDataObj.get("DATA").getAsString());
                logData.add(entry);
            }

            logPayload.setLogData(logData);
            payload.add(logPayload);
        }

        LogsDetailsWrapper logsWrapper = new LogsDetailsWrapper();
        logsWrapper.setDeviceId(device.getDeviceIdentifier());
        logsWrapper.setTenantId(DeviceManagerUtil.getTenantId());
        logsWrapper.setDeviceType(device.getType());
        logsWrapper.setPayload(payload);

        return logsWrapper;
    }
}