/*
 *   Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.device.mgt.core.dao;

import org.wso2.carbon.device.mgt.common.EnrolmentInfo;
import org.wso2.carbon.device.mgt.common.LifecycleStateDevice;
import org.wso2.carbon.device.mgt.common.PaginationRequest;

import java.util.List;

/**
 * Device status relevent DAO activity
 */
public interface DeviceLifecycleDAO {

    /**
     * Get Device ID
     *
     * @param enrolmentId Enrolment ID
     * @return Device id
     * @throws DeviceManagementDAOException when device not found
     */
    int getDeviceId(int enrolmentId) throws DeviceManagementDAOException;

    /**
     * Get the lifecycle history of the device
     *
     * @param request  PaginationRequest object holding the data for pagination and search data.
     * @param id id of the device
     * @return List of LifecycleStateDevice
     */
    List<LifecycleStateDevice> getDeviceLifecycle(PaginationRequest request, int id) throws DeviceManagementDAOException;

    int getLifecycleCountByDevice(int id) throws DeviceManagementDAOException;
}
