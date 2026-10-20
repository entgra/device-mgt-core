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
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "SupportedDeviceTypeInfo", description = "A device subtype supported by a communication module.")
public class SupportedDeviceTypeInfo {

    @ApiModelProperty(name = "id", value = "The device subtype ID.", required = true)
    private String id;

    @ApiModelProperty(name = "deviceType", value = "The device type.", required = true)
    private String deviceType;

    @ApiModelProperty(name = "subTypeName", value = "The device subtype name.", required = true)
    private String subTypeName;

    public SupportedDeviceTypeInfo(String id, String deviceType, String subTypeName) {
        this.id = id;
        this.deviceType = deviceType;
        this.subTypeName = subTypeName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getSubTypeName() {
        return subTypeName;
    }

    public void setSubTypeName(String subTypeName) {
        this.subTypeName = subTypeName;
    }
}
