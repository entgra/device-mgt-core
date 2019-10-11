/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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

package org.wso2.carbon.device.mgt.extensions.device.type.template.dao;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.extensions.device.type.template.exception.DeviceTypeMgtPluginException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.util.DeviceTypeUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements CRUD for Devices. This holds the generic implementation. An instance of this will be created for
 * each device type.
 */
public class DeviceTypePluginDAOImpl implements PluginDAO {

    private static final Log log = LogFactory.getLog(DeviceTypePluginDAOImpl.class);
    private DeviceTypeDAOHandler deviceTypeDAOHandler;
    private DeviceDAODefinition deviceDAODefinition;
    private String selectDBQueryForGetDevice;
    private String createDBqueryForAddDevice;
    private String updateDBQueryForUpdateDevice;
    private String selectDBQueryToGetAllDevice;
    private String deleteDBQueryForDeleteDevice;

    public DeviceTypePluginDAOImpl(DeviceDAODefinition deviceDAODefinition,
                                   DeviceTypeDAOHandler deviceTypeDAOHandler) {
        this.deviceTypeDAOHandler = deviceTypeDAOHandler;
        this.deviceDAODefinition = deviceDAODefinition;
        initializeDbQueries();
    }

    public Device getDevice(String deviceId) throws DeviceTypeMgtPluginException {
        Connection conn = null;
        PreparedStatement stmt = null;
        Device device = null;
        ResultSet resultSet = null;
        try {
            conn = deviceTypeDAOHandler.getConnection();
            stmt = conn.prepareStatement(selectDBQueryForGetDevice);
            stmt.setString(1, deviceId);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                device = new Device();
                device.setDeviceIdentifier(deviceId);
                if (log.isDebugEnabled()) {
                    log.debug(deviceId + " data has been fetched from " + deviceDAODefinition.getDeviceTableName() +
                            " database.");
                }
                List<Device.Property> properties = new ArrayList<>();
                for (String columnName : deviceDAODefinition.getColumnNames()) {
                    Device.Property property = new Device.Property();
                    property.setName(columnName);
                    property.setValue(resultSet.getString(columnName));
                    properties.add(property);
                }
                device.setProperties(properties);
            }

        } catch (SQLException e) {
            String msg = "Error occurred while fetching device : '" + deviceId + "' from " + deviceDAODefinition
                    .getDeviceTableName();
            log.error(msg, e);
            throw new DeviceTypeMgtPluginException(msg, e);
        } finally {
            DeviceTypeUtils.cleanupResources(stmt, resultSet);
            deviceTypeDAOHandler.closeConnection();
        }

        return device;
    }

    public boolean addDevice(Device device) throws DeviceTypeMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = deviceTypeDAOHandler.getConnection();
            stmt = conn.prepareStatement(createDBqueryForAddDevice);
            stmt.setString(1, device.getDeviceIdentifier());
            int columnIndex = 2;
            for (String columnName : deviceDAODefinition.getColumnNames()) {
                stmt.setString(columnIndex, getPropertString(device.getProperties(), columnName));
                columnIndex++;
            }
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("device " + device.getDeviceIdentifier() + " data has been" +
                            " added to the " + deviceDAODefinition.getDeviceTableName() + " database.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding the device '" +
                    device.getDeviceIdentifier() + "' to the " + deviceDAODefinition.getDeviceTableName() + " db.";
            log.error(msg, e);
            throw new DeviceTypeMgtPluginException(msg, e);
        } finally {
            DeviceTypeUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public boolean updateDevice(Device device) throws DeviceTypeMgtPluginException {
        boolean status = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = deviceTypeDAOHandler.getConnection();
            stmt = conn.prepareStatement(updateDBQueryForUpdateDevice);
            int columnIndex = 1;
            for (String columnName : deviceDAODefinition.getColumnNames()) {
                stmt.setString(columnIndex, getPropertString(device.getProperties(), columnName));
                columnIndex++;
            }
            stmt.setString(columnIndex, device.getDeviceIdentifier());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                status = true;
                if (log.isDebugEnabled()) {
                    log.debug("device " + device.getDeviceIdentifier() + " data has been modified.");
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while modifying the device '" +
                    device.getDeviceIdentifier() + "' data. " + deviceDAODefinition.getDeviceTableName();
            log.error(msg, e);
            throw new DeviceTypeMgtPluginException(msg, e);
        } finally {
            DeviceTypeUtils.cleanupResources(stmt, null);
        }
        return status;
    }

    public List<Device> getAllDevices() throws DeviceTypeMgtPluginException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        Device device;
        List<Device> devices = new ArrayList<>();
        try {
            conn = deviceTypeDAOHandler.getConnection();
            stmt = conn.prepareStatement(selectDBQueryToGetAllDevice);
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                device = new Device();
                device.setDeviceIdentifier(resultSet.getString(deviceDAODefinition.getPrimaryKey()));
                List<Device.Property> properties = new ArrayList<>();
                for (String columnName : deviceDAODefinition.getColumnNames()) {
                    Device.Property property = new Device.Property();
                    property.setName(columnName);
                    property.setValue(resultSet.getString(columnName));
                    properties.add(property);
                }
                device.setProperties(properties);
                devices.add(device);
            }
            if (log.isDebugEnabled()) {
                log.debug(
                        "All device details have fetched from " + deviceDAODefinition.getDeviceTableName() + " table.");
            }
            return devices;
        } catch (SQLException e) {
            String msg =
                    "Error occurred while fetching all " + deviceDAODefinition.getDeviceTableName() + " device data'";
            log.error(msg, e);
            throw new DeviceTypeMgtPluginException(msg, e);
        } finally {
            DeviceTypeUtils.cleanupResources(stmt, resultSet);
            deviceTypeDAOHandler.closeConnection();
        }
    }

    @Override
    public boolean deleteDevices(List<String> deviceIdentifiers) throws DeviceTypeMgtPluginException {
        try {
            Connection conn = deviceTypeDAOHandler.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(deleteDBQueryForDeleteDevice)) {
                if (conn.getMetaData().supportsBatchUpdates()) {
                    for (String deviceId : deviceIdentifiers) {
                        ps.setString(1, deviceId);
                        ps.addBatch();
                    }
                    for (int i : ps.executeBatch()) {
                        if (i == Statement.SUCCESS_NO_INFO || i == Statement.EXECUTE_FAILED) {
                            return false;
                        }
                    }
                } else {
                    for (String deviceId : deviceIdentifiers) {
                        ps.setString(1, deviceId);
                        ps.executeUpdate();
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            String msg = "Error occurred while deleting the data in "
                    + deviceDAODefinition.getDeviceTableName();
            log.error(msg, e);
            throw new DeviceTypeMgtPluginException(msg, e);
        }
    }

    private String getDeviceTableColumnNames() {
        return StringUtils.join(deviceDAODefinition.getColumnNames(), ", ");
    }

    private String getDeviceTableColumnNamesForUpdateQuery() {
        return StringUtils.join(deviceDAODefinition.getColumnNames(), "= ?,") + "= ?";
    }

    private String getPreparedInputString(int length) {
        String preparedInput = "?";
        for (int i = 1; i < length; i++) {
            preparedInput += ", ?";
        }
        return preparedInput;
    }

    private String getPropertString(List<Device.Property> properties, String propertyName) {
        if (properties != null) {
            for (Device.Property property : properties) {
                if (property.getName() != null && property.getName().equals(propertyName)) {
                    return property.getValue();
                }
            }
        }
        return null;
    }

    private void initializeDbQueries() {
        selectDBQueryForGetDevice =
                "SELECT " + getDeviceTableColumnNames() + " FROM " + deviceDAODefinition.getDeviceTableName()
                        + " WHERE " + deviceDAODefinition.getPrimaryKey() + " = ?";

        createDBqueryForAddDevice =
                "INSERT INTO " + deviceDAODefinition.getDeviceTableName() + "(" + deviceDAODefinition.getPrimaryKey()
                        + " , " + getDeviceTableColumnNames() + ") VALUES (" + getPreparedInputString(
                        deviceDAODefinition.getColumnNames().size() + 1) + ")";

        updateDBQueryForUpdateDevice = "UPDATE " + deviceDAODefinition.getDeviceTableName() + " SET "
                + getDeviceTableColumnNamesForUpdateQuery() + " WHERE " + deviceDAODefinition.getPrimaryKey() + " = ?";

        selectDBQueryToGetAllDevice =
                "SELECT " + getDeviceTableColumnNames() + "," + deviceDAODefinition.getPrimaryKey() + " FROM "
                        + deviceDAODefinition.getDeviceTableName();

        deleteDBQueryForDeleteDevice = "DELETE FROM " + deviceDAODefinition.getDeviceTableName() + " WHERE "
                                       + deviceDAODefinition.getPrimaryKey() + " = ?";
    }
}
