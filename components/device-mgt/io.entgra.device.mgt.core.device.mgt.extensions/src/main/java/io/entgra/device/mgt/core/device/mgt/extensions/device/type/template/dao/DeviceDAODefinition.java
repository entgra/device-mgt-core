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
package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.dao;

import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config.Table;
import io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.exception.DeviceTypeDeployerPayloadException;

import java.util.ArrayList;
import java.util.List;

/**
 * This holds the meta data of device definition table.
 * This is optional.
 */
public class DeviceDAODefinition {

    private String deviceTableName;
    private String primarykey;

    public List<String> getColumnNames() {
        return columnNames;
    }

    private List<String> columnNames = new ArrayList<>();


    public DeviceDAODefinition(Table table) {
        if (table == null) {
            throw new DeviceTypeDeployerPayloadException("Table is null. Cannot create DeviceDAODefinition");
        }
        deviceTableName = table.getName();
        primarykey = table.getPrimaryKey();

        if (deviceTableName == null || deviceTableName.isEmpty()) {
            throw new DeviceTypeDeployerPayloadException("Missing deviceTableName");
        }

        if (primarykey == null || primarykey.isEmpty()) {
            throw new DeviceTypeDeployerPayloadException("Missing primaryKey for the table " + deviceTableName);
        }

        if (table.getAttributes() == null) {
            throw new DeviceTypeDeployerPayloadException("Table " + deviceTableName + " attributes are not specified. "
                    + "Cannot created DeviceDAODefinition");
        }

        List<String> attributes = table.getAttributes().getAttribute();
        if (attributes == null || attributes.size() == 0) {
            throw new DeviceTypeDeployerPayloadException("Missing Attributes ");
        }
        for (String attribute : attributes) {
            if (attribute.isEmpty()) {
                throw new DeviceTypeDeployerPayloadException("Unsupported attribute format for device definition");
            }
            columnNames.add(attribute);
        }
    }

    public String getDeviceTableName() {
        return deviceTableName;
    }

    public String getPrimaryKey() {
        return primarykey;
    }


}
