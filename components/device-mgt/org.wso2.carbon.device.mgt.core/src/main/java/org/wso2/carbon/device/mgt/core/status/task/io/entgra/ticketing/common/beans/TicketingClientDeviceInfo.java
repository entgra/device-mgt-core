/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.beans;

public class TicketingClientDeviceInfo {
    private String subject;
    private String message;
    private String deviceType;
    private String deviceIdentifier;
    private int deviceId;
    private String deviceName;

    public TicketingClientDeviceInfo(String subject, String message, String deviceType, String deviceIdentifier, int deviceId,
                                     String deviceName){
        this.subject=subject;
        this.message =message;
        this.deviceType=deviceType;
        this.deviceIdentifier =deviceIdentifier;
        this.deviceId =deviceId;
        this.deviceName =deviceName;
    }

    public TicketingClientDeviceInfo(){ }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }

    public String getDeviceType() { return deviceType; }

    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

}
