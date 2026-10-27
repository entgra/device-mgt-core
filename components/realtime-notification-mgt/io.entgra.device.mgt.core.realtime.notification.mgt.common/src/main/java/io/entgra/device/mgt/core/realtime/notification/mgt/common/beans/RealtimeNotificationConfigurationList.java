/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.realtime.notification.mgt.common.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "RealtimeNotification Configuration List",
        description = "This contains a collection of notification configurations.")
public class RealtimeNotificationConfigurationList {

    @ApiModelProperty(value = "List of notification configurations")
    @JsonProperty("notificationConfigurations")
    private List<RealtimeNotificationConfig> notificationConfigurations = new ArrayList<>();

    @ApiModelProperty(value = "Default Archive period for notifications")
    @JsonProperty("defaultArchiveAfter")
    private RealtimeArchivePeriod defaultArchiveAfter;

    @ApiModelProperty(value = "Default Archive type for notifications")
    @JsonProperty("defaultArchiveType")
    private String defaultArchiveType;

    @ApiModelProperty(value = "Total number of matching configurations")
    @JsonProperty("totalCount")
    private int totalCount;

    public List<RealtimeNotificationConfig> getNotificationConfigurations() {
        return notificationConfigurations;
    }

    public void setNotificationConfigurations(List<RealtimeNotificationConfig> notificationConfigurations) {
        this.notificationConfigurations = notificationConfigurations;
    }

    public RealtimeArchivePeriod getDefaultArchiveAfter() {
        return defaultArchiveAfter;
    }

    public void setDefaultArchiveAfter(RealtimeArchivePeriod defaultArchiveAfter) {
        this.defaultArchiveAfter = defaultArchiveAfter;
    }
    
    public String getDefaultArchiveType() {
        return defaultArchiveType;
    }

    public void setDefaultArchiveType(String defaultArchiveType) {
        this.defaultArchiveType = defaultArchiveType;
    }

    public void add(RealtimeNotificationConfig config) {
        this.notificationConfigurations.add(config);
    }

    public RealtimeNotificationConfig get(int index) {
        return this.notificationConfigurations.get(index);
    }

    public void set(int index, RealtimeNotificationConfig config) {
        this.notificationConfigurations.set(index, config);
    }

    public int size() {
        return this.notificationConfigurations.size();
    }

    public boolean isEmpty() {
        return this.notificationConfigurations.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  count: ").append(getCount()).append(",\n");
        sb.append("]}\n");
        return sb.toString();
    }

    public int getCount() {
        return this.notificationConfigurations.size();
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
