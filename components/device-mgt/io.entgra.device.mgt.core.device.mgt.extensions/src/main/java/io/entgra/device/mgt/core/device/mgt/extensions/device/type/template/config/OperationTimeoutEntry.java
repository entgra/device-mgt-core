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
package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "OperationTimeout")
public class OperationTimeoutEntry {

    @XmlElement(name = "OperationCode", required = true)
    private String operationCode;

    @XmlElement(name = "Timeout", required = true)
    private long timeout;

    @XmlElementWrapper(name = "DeviceTypes", required = true)
    @XmlElement(name = "DeviceType", required = true)
    private List<String> deviceTypes;

    @XmlElement(name = "InitialStatus", required = true)
    private String initialStatus;

    @XmlElement(name = "NextStatus", required = true)
    private String nextStatus;

    /**
     * Gets the operation code for which this timeout configuration applies.
     * @return the operation code
     */
    public String getOperationCode() {
        return operationCode;
    }

    /**
     * Sets the operation code for which this timeout configuration applies.
     * @param operationCode the operation code to set
     */
    public void setOperationCode(String operationCode) {
        this.operationCode = operationCode;
    }

    /**
     * Gets the timeout value in milliseconds.
     * @return the timeout in milliseconds
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout value in milliseconds.
     * @param timeout the timeout to set in milliseconds
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Gets the list of device types for which this timeout configuration applies.
     * @return the list of device types
     */
    public List<String> getDeviceTypes() {
        if (deviceTypes == null) {
            deviceTypes = new ArrayList<>();
        }
        return deviceTypes;
    }

    /**
     * Sets the list of device types for which this timeout configuration applies.
     * @param deviceTypes the list of device types to set
     */
    public void setDeviceTypes(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    /**
     * Gets the initial status of the operation before timeout.
     * @return the initial status
     */
    public String getInitialStatus() {
        return initialStatus;
    }

    /**
     * Sets the initial status of the operation before timeout.
     * @param initialStatus the initial status to set
     */
    public void setInitialStatus(String initialStatus) {
        this.initialStatus = initialStatus;
    }

    /**
     * Gets the status to be set when the operation times out.
     * @return the next status after timeout
     */
    public String getNextStatus() {
        return nextStatus;
    }

    /**
     * Sets the status to be set when the operation times out.
     * @param nextStatus the next status to set after timeout
     */
    public void setNextStatus(String nextStatus) {
        this.nextStatus = nextStatus;
    }
}