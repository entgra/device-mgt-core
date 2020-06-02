/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

function onRequest(context) {
    var log = new Log("configuration.js");
    var utility = require("/app/modules/utility.js").utility;
    var deviceModule = require("/app/modules/business-controllers/device.js")["deviceModule"];
    var devicemgtProps = require("/app/modules/conf-reader/main.js")["conf"];

    //get all device types
    var isAuthorized = false;
    if (userModule.isAuthorized("/permission/admin/device-mgt/notifications/view")) {
        isAuthorized = true;
    }
    var deviceTypesArray = [];
    var typesListResponse = deviceModule.getDeviceTypes();
    if (typesListResponse["status"] == "success") {
        var data = typesListResponse["content"];
        if (data) {
            for (var i = 0; i < data.length; i++) {
                var deviceTypeName = data[i].name;
                var deviceTypeLabel = deviceTypeName.charAt(0).toUpperCase() + deviceTypeName.slice(1);
                var configUnitName = utility.getTenantedDeviceUnitName(deviceTypeName, "platform.configuration");
                if (configUnitName) {
                    var deviceTypeConfig = utility.getDeviceTypeConfig(deviceTypeName);
                    if (deviceTypeConfig) {
                        deviceTypeLabel = deviceTypeConfig.deviceType.label;
                    }
                }
                deviceTypesArray.push({
                    name: deviceTypeName,
                    label: deviceTypeLabel,
                    unitName: configUnitName
                });
            }
        }
    }
    var geoServicesEnabled = devicemgtProps.serverConfig.geoLocationConfiguration.enabled;
    return {
        "geoServicesEnabled": geoServicesEnabled,
        "deviceTypes": deviceTypesArray,
        "isAuthorized": isAuthorized
    };
}
