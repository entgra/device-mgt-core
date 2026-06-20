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

package io.entgra.device.mgt.core.device.mgt.core.config.mqtt;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MQTTConfigurations")
public class MQTTConfiguration {

    private int compressionLevel;
    private int compressionThresholdKb;

    @XmlElement(name = "CompressionLevel", required = true)
    public int getCompressionLevel() {
        return compressionLevel;
    }

    public void setCompressionLevel(int compressionLevel) {
        this.compressionLevel = compressionLevel;
    }

    @XmlElement(name = "CompressionThresholdKb", required = true)
    public int getCompressionThresholdKb() {
        return compressionThresholdKb;
    }

    public void setCompressionThresholdKb(int compressionThresholdKb) {
        this.compressionThresholdKb = compressionThresholdKb;
    }
}


