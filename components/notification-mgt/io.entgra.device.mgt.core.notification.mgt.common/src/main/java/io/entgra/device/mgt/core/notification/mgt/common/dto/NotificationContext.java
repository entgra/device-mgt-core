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

package io.entgra.device.mgt.core.notification.mgt.common.dto;

import java.util.List;

/**
 * Structured notification payload persisted as JSON in {@code DM_NOTIFICATION.NOTIFICATION_CONTEXT}.
 */
public class NotificationContext {

    private String operationCode;
    private String configDescription;
    private String operationStatus;
    private String deviceType;
    private Boolean includeDeviceList;
    /** Plain-text body for task/system notifications. */
    private String message;
    /** User-facing labels: {@code name (identifier)}. Set at publish time for operations. */
    private List<String> deviceLabels;

    public String getOperationCode() {
        return operationCode;
    }

    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    public String getConfigDescription() {
        return configDescription;
    }

    public void setConfigDescription(String configDescription) {
        this.configDescription = configDescription;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Boolean getIncludeDeviceList() {
        return includeDeviceList;
    }

    public void setIncludeDeviceList(Boolean includeDeviceList) {
        this.includeDeviceList = includeDeviceList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getDeviceLabels() {
        return deviceLabels;
    }

    public void setDeviceLabels(List<String> deviceLabels) {
        this.deviceLabels = deviceLabels;
    }
}
