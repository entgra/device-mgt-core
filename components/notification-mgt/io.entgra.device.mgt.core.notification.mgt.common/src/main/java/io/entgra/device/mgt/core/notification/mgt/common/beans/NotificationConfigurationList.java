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
package io.entgra.device.mgt.core.notification.mgt.common.beans;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "Notification Configuration List", description = "This contains basic details of a set of users that matches " +
        "a given criteria as a collection")
public class NotificationConfigurationList {

    private List<NotificationConfig> notificationConfigurations = new ArrayList<>();

    @ApiModelProperty(value = "List of devices returned")
    @JsonProperty("notificationConfigurations")
    public List<NotificationConfig> getList() {
        return notificationConfigurations;
    }

    public void setList(List<NotificationConfig> notificationConfigurations) {
        this.notificationConfigurations = notificationConfigurations;
    }
    public void add(NotificationConfig config) {
        this.notificationConfigurations.add(config);
    }

    public NotificationConfig get(int index) {
        return this.notificationConfigurations.get(index);
    }

    public void set(int index, NotificationConfig config) {
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

}
