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

package io.entgra.device.mgt.core.device.mgt.core.dao.impl.group;

import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroupRoleWrapper;
import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.group.mgt.DeviceGroup;
import io.entgra.device.mgt.core.device.mgt.core.dao.GroupManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.dao.GroupManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.AbstractGroupDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.GroupManagementDAOUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents implementation of GroupDAO
 */
public class PostgreSQLGroupDAOImpl extends AbstractGroupDAOImpl {

    private static final Log log = LogFactory.getLog(PostgreSQLGroupDAOImpl.class);

    @Override
    public int addGroup(DeviceGroup deviceGroup, int tenantId) throws GroupManagementDAOException {
        PreparedStatement stmt = null;
        ResultSet rs;
        int groupId = -1;
        boolean hasStatus = false;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql;
            if(StringUtils.isEmpty(deviceGroup.getStatus())) {
                sql = "INSERT INTO DM_GROUP(DESCRIPTION, GROUP_NAME, OWNER, TENANT_ID, PARENT_PATH, PARENT_GROUP_ID) " +
                      "VALUES (?, ?, ?, ?) RETURNING ID";
            } else {
                sql = "INSERT INTO DM_GROUP(DESCRIPTION, GROUP_NAME, OWNER, TENANT_ID, PARENT_PATH, STATUS) " +
                      "VALUES (?, ?, ?, ?, ?, ?) RETURNING ID";
                hasStatus = true;
            }
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, deviceGroup.getDescription());
            stmt.setString(2, deviceGroup.getName());
            stmt.setString(3, deviceGroup.getOwner());
            stmt.setInt(4, tenantId);
            stmt.setString(5, deviceGroup.getParentPath());
            if(hasStatus) {
                stmt.setString(6, deviceGroup.getStatus());
            }
            stmt.execute();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                groupId = rs.getInt(1);
            }
            return groupId;
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while adding deviceGroup '" +
                    deviceGroup.getName() + "'", e);
        } finally {
            GroupManagementDAOUtil.cleanupResources(stmt, null);
        }
    }

    @Override
    public int addGroupWithRoles(DeviceGroupRoleWrapper groups, int tenantId) throws GroupManagementDAOException {
        int groupId = -1;
        boolean hasStatus = false;
        try {
            Connection conn = GroupManagementDAOFactory.getConnection();
            String sql;
            if (StringUtils.isEmpty(groups.getStatus())) {
                sql = "INSERT INTO DM_GROUP(DESCRIPTION, GROUP_NAME, OWNER, TENANT_ID, PARENT_PATH) " +
                        "VALUES (?, ?, ?, ?) RETURNING ID";
            } else {
                sql = "INSERT INTO DM_GROUP(DESCRIPTION, GROUP_NAME, OWNER, TENANT_ID, PARENT_PATH, STATUS) " +
                        "VALUES (?, ?, ?, ?, ?) RETURNING ID";
                hasStatus = true;
            }
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, groups.getDescription());
                stmt.setString(2, groups.getName());
                stmt.setString(3, groups.getOwner());
                stmt.setInt(4, tenantId);
                stmt.setString(5, groups.getParentPath());
                if (hasStatus) {
                    stmt.setString(6, groups.getStatus());
                }
                stmt.execute();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        groupId = rs.getInt(1);
                    }
                    return groupId;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while adding deviceGroup '" +
                    groups.getName() + "'";
            log.error(msg);
            throw new GroupManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Device> getDevices(int groupId, int startIndex, int rowCount, int tenantId)
            throws GroupManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;
        try {
            conn = GroupManagementDAOFactory.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, e.DEVICE_TYPE, " +
                    "d1.DEVICE_IDENTIFICATION, d1.LAST_UPDATED_TIMESTAMP, e.OWNER, e.OWNERSHIP, e.STATUS, " +
                    "e.IS_TRANSFERRED, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                    "(SELECT gd.DEVICE_ID, gd.DESCRIPTION, gd.NAME, gd.DEVICE_IDENTIFICATION, gd.LAST_UPDATED_TIMESTAMP " +
                    "FROM " +
                    "(SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME, d.DEVICE_IDENTIFICATION, d.LAST_UPDATED_TIMESTAMP FROM" +
                    " DM_DEVICE d, (" +
                    "SELECT dgm.DEVICE_ID FROM DM_DEVICE_GROUP_MAP dgm WHERE dgm.GROUP_ID = ?) dgm1 " +
                    "WHERE d.ID = dgm1.DEVICE_ID AND d.TENANT_ID = ?) gd) d1 " +
                    "WHERE d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? LIMIT ? OFFSET ?";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            //noinspection JpaQueryApiInspection
            stmt.setInt(4, rowCount);
            //noinspection JpaQueryApiInspection
            stmt.setInt(5, startIndex);
            rs = stmt.executeQuery();
            devices = new ArrayList<>();
            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new GroupManagementDAOException("Error occurred while retrieving information of all " +
                    "registered devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }
}
