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

package io.entgra.device.mgt.core.subtype.mgt.dao;

import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.exception.SubTypeMgtDAOException;

import java.util.List;

public interface DeviceSubTypeDAO {
    boolean addDeviceSubType(DeviceSubType deviceSubType) throws SubTypeMgtDAOException;

    boolean updateDeviceSubType(String subTypeId, int tenantId, String deviceType, String subTypeName,
                                String typeDefinition) throws SubTypeMgtDAOException;

    DeviceSubType getDeviceSubType(String subTypeId, int tenantId, String deviceType)
            throws SubTypeMgtDAOException;

    List<DeviceSubType> getAllDeviceSubTypes(int tenantId, String deviceType)
            throws SubTypeMgtDAOException;

    int getDeviceSubTypeCount(String deviceType) throws SubTypeMgtDAOException;

    boolean checkDeviceSubTypeExist(String subTypeId, int tenantId, String deviceType)
            throws SubTypeMgtDAOException;

    DeviceSubType getDeviceSubTypeByProvider(String subTypeName, int tenantId, String deviceType)
            throws SubTypeMgtDAOException;

    /**
     * Retrieves the subtype name associated with a given IMEI number.
     *
     * <p>This method joins the <b>COMMUNICATION_MODULE</b> and <b>DM_DEVICE_SUB_TYPE</b> tables to find
     * the corresponding subtype name of the communication module based on the IMEI.
     *
     * <p>The SQL query performs the following:
     * <ul>
     *     <li>Joins <b>COMMUNICATION_MODULE</b> (<code>c</code>) and <b>DM_DEVICE_SUB_TYPE</b> (<code>d</code>)
     *         on <code>c.SUB_TYPE_ID = d.SUB_TYPE_ID</code>, with the latter cast to UNSIGNED for type compatibility.</li>
     *     <li>Filters by <code>c.IMEI = ?</code> to find the specific communication module.</li>
     *     <li>Retrieves the corresponding <code>SUB_TYPE_NAME</code> from <code>d</code>.</li>
     * </ul>
     *
     * @param imeiNumber the IMEI number of the communication module
     * @return the subtype name if found; otherwise, null
     * @throws SubTypeMgtDAOException if a database access error occurs
     */
    String getSubTypeNames(String imeiNumber) throws SubTypeMgtDAOException;
}
