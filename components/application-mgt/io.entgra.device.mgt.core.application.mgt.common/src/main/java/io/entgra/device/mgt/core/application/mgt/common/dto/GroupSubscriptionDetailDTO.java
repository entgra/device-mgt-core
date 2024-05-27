/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.application.mgt.common.dto;

import io.entgra.device.mgt.core.application.mgt.common.CategorizedSubscriptionResult;

import java.sql.Timestamp;
import java.util.Map;

public class GroupSubscriptionDetailDTO {
    private int groupId;
    private String groupName;
    private String groupOwner;
    private String subscribedBy;
    private Timestamp subscribedTimestamp;
    private boolean isUnsubscribed;
    private String unsubscribedBy;
    private Timestamp unsubscribedTimestamp;
    private int appReleaseId;
    private int deviceCount;
    private CategorizedSubscriptionResult devices;
    private Map<String, Double> statusPercentages;

    // Getters and Setters
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupOwner() {
        return groupOwner;
    }

    public void setGroupOwner(String groupOwner) {
        this.groupOwner = groupOwner;
    }

    public String getSubscribedBy() {
        return subscribedBy;
    }

    public void setSubscribedBy(String subscribedBy) {
        this.subscribedBy = subscribedBy;
    }

    public Timestamp getSubscribedTimestamp() {
        return subscribedTimestamp;
    }

    public void setSubscribedTimestamp(Timestamp subscribedTimestamp) {
        this.subscribedTimestamp = subscribedTimestamp;
    }

    public boolean isUnsubscribed() {
        return isUnsubscribed;
    }

    public void setUnsubscribed(boolean unsubscribed) {
        isUnsubscribed = unsubscribed;
    }

    public String getUnsubscribedBy() {
        return unsubscribedBy;
    }

    public void setUnsubscribedBy(String unsubscribedBy) {
        this.unsubscribedBy = unsubscribedBy;
    }

    public Timestamp getUnsubscribedTimestamp() {
        return unsubscribedTimestamp;
    }

    public void setUnsubscribedTimestamp(Timestamp unsubscribedTimestamp) {
        this.unsubscribedTimestamp = unsubscribedTimestamp;
    }

    public int getAppReleaseId() {
        return appReleaseId;
    }

    public void setAppReleaseId(int appReleaseId) {
        this.appReleaseId = appReleaseId;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public CategorizedSubscriptionResult getDevices() {
        return devices;
    }

    public void setDevices(CategorizedSubscriptionResult devices) {
        this.devices = devices;
    }

    public Map<String, Double> getStatusPercentages() {
        return statusPercentages;
    }

    public void setStatusPercentages(Map<String, Double> statusPercentages) {
        this.statusPercentages = statusPercentages;
    }
}
