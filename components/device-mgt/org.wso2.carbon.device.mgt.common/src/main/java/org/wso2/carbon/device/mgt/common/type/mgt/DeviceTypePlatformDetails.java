/*
 *  Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.device.mgt.common.type.mgt;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeviceTypePlatformDetails", propOrder = {
        "deviceTypePlatformVersion"
})
public class DeviceTypePlatformDetails {

    @XmlElement(name = "DeviceTypePlatformVersion")
    private List<DeviceTypePlatformVersion> deviceTypePlatformVersion;

    public List<DeviceTypePlatformVersion> getDeviceTypePlatformVersion() {
        return deviceTypePlatformVersion;
    }

    public void setDeviceTypePlatformVersion(List<DeviceTypePlatformVersion> deviceTypePlatformVersion) {
        this.deviceTypePlatformVersion = deviceTypePlatformVersion;
    }
}


