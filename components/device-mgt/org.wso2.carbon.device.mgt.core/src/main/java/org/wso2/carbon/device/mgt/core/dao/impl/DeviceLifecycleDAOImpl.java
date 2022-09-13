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

package org.wso2.carbon.device.mgt.core.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.device.mgt.common.LifecycleStateDevice;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.core.dao.DeviceLifecycleDAO;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeviceLifecycleDAOImpl implements DeviceLifecycleDAO {

    private static final Log log = LogFactory.getLog(DeviceLifecycleDAOImpl.class);

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    public int getDeviceId(int enrolmentId) throws DeviceManagementDAOException {
        int deviceId;
        Connection conn;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT DEVICE_ID FROM DM_ENROLMENT WHERE ID = ? AND TENANT_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, enrolmentId);
            stmt.setInt(2, tenantId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                deviceId = rs.getInt("DEVICE_ID");
            } else {
                // if there were no records corresponding to the enrolment id this is a problem. i.e. enrolment
                // id is invalid
                throw new DeviceManagementDAOException("Error occurred while getting the status of device enrolment: " +
                        "no record for enrolment id " + enrolmentId);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getiing the device if which has " + enrolmentId + " enrolmentId";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return deviceId;
    }

    @Override
    public List<LifecycleStateDevice> getDeviceLifecycle(PaginationRequest request, int id) throws DeviceManagementDAOException {
        List<LifecycleStateDevice> result = new ArrayList<>();
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT DEVICE_ID, STATUS, UPDATE_TIME, CHANGED_BY, PREVIOUS_STATUS FROM " +
                    "DM_DEVICE_STATUS WHERE DEVICE_ID = ? ORDER BY ID DESC LIMIT ?,?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setInt(2, request.getStartIndex());
            stmt.setInt(3, request.getRowCount());
            rs = stmt.executeQuery();

            while (rs.next()) {
                LifecycleStateDevice lifecycleStateDevice = new LifecycleStateDevice(
                        rs.getInt("DEVICE_ID"),
                        rs.getString("STATUS"),
                        rs.getString("PREVIOUS_STATUS"),
                        rs.getString("CHANGED_BY"),
                        new Date(rs.getTimestamp("UPDATE_TIME").getTime())
                );
                result.add(lifecycleStateDevice);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getiing the device lifecycle which has " + id + " deviceId";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return result;
    }

    @Override
    public int getLifecycleCountByDevice(int id) throws DeviceManagementDAOException {

        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int lifecycleCount = 0;
        try {
            conn = this.getConnection();
            String sql = "SELECT COUNT(ID) AS STATES_COUNT FROM DM_DEVICE_STATUS WHERE DEVICE_ID = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                lifecycleCount = rs.getInt("STATES_COUNT");
            }
        } catch (SQLException e) {
            String msg = "Error occurred while getiing the device lifecycle which has " + id + " deviceId";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }

        return lifecycleCount;
    }
}
