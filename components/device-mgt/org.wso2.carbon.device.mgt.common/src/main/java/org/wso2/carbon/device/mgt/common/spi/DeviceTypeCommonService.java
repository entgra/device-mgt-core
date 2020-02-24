/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.common.spi;

import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;

import java.util.Map;

/**
 * This implementation populates device type plugin management service.
 */
public interface DeviceTypeCommonService {

    /**
     * To get Enrollment QR code against Ownership type
     *
     * @return QR code Map which contains key value pairs to have for the QR String.
     * @throws DeviceManagementException if error occurred while generating the QR String for Ownership
     */
    Map<String, Object> getEnrollmentQRCode(String ownershipType) throws DeviceManagementException;
}
