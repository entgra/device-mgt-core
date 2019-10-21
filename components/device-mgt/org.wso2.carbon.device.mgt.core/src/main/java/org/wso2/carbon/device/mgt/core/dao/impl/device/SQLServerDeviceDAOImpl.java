/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.dao.impl.device;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOException;
import org.wso2.carbon.device.mgt.core.dao.DeviceManagementDAOFactory;
import org.wso2.carbon.device.mgt.core.dao.impl.AbstractDeviceDAOImpl;
import org.wso2.carbon.device.mgt.core.dao.util.DeviceManagementDAOUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

/**
 * This class holds the generic implementation of DeviceDAO which can be used to support ANSI db syntax.
 */
public class SQLServerDeviceDAOImpl extends AbstractDeviceDAOImpl {

    private static final Log log = LogFactory.getLog(SQLServerDeviceDAOImpl.class);

    @Override
    public List<Device> getDevices(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;
        String deviceType = request.getDeviceType();
        boolean isDeviceTypeProvided = false;
        String deviceName = request.getDeviceName();
        boolean isDeviceNameProvided = false;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownerPattern = request.getOwnerPattern();
        boolean isOwnerPatternProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        String status = request.getStatus();
        boolean isStatusProvided = false;
        String excludeStatus = request.getExcludeStatus();
        boolean isExcludeStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                         "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                         "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.DESCRIPTION, " +
                         "d.NAME, d.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE " +
                         "FROM DM_DEVICE d, DM_DEVICE_TYPE t ";

            //Add the query to filter active devices on timestamp
            if (since != null) {
                sql = sql + ", DM_DEVICE_DETAIL dt";
                isSinceProvided = true;
            }

            sql = sql + " WHERE DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?";

            //Add query for last updated timestamp
            if (isSinceProvided) {
                sql = sql + " AND dt.DEVICE_ID = d.ID AND dt.UPDATE_TIMESTAMP > ?";
            }

            //Add the query for device-type
            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND t.NAME = ?";
                isDeviceTypeProvided = true;
            }
            //Add the query for device-name
            if (deviceName != null && !deviceName.isEmpty()) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }

            sql = sql + ") d1 WHERE d1.ID = e.DEVICE_ID AND TENANT_ID = ?";

            //Add the query for ownership
            if (ownership != null && !ownership.isEmpty()) {
                sql = sql + " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }
            //Add the query for owner
            if (owner != null && !owner.isEmpty()) {
                sql = sql + " AND e.OWNER = ?";
                isOwnerProvided = true;
            } else if (ownerPattern != null && !ownerPattern.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerPatternProvided = true;
            }
            //Add the query for status
            if (status != null && !status.isEmpty()) {
                sql = sql + " AND e.STATUS = ?";
                isStatusProvided = true;
            }
            //Add the query for exclude status
            if (excludeStatus != null && !excludeStatus.isEmpty()) {
                sql = sql + " AND e.STATUS != ?";
                isExcludeStatusProvided = true;
            }

            sql = sql + " ORDER BY ENROLMENT_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            int paramIdx = 2;
            if (isSinceProvided) {
                stmt.setLong(paramIdx++, since.getTime());
            }
            if (isDeviceTypeProvided) {
                stmt.setString(paramIdx++, deviceType);
            }
            if (isDeviceNameProvided) {
                stmt.setString(paramIdx++, deviceName + "%");
            }

            stmt.setInt(paramIdx++, tenantId);
            if (isOwnershipProvided) {
                stmt.setString(paramIdx++, ownership);
            }
            if (isOwnerProvided) {
                stmt.setString(paramIdx++, owner);
            } else if (isOwnerPatternProvided) {
                stmt.setString(paramIdx++, ownerPattern + "%");
            }
            if (isStatusProvided) {
                stmt.setString(paramIdx++, status);
            }
            if (isExcludeStatusProvided) {
                stmt.setString(paramIdx++, excludeStatus);
            }
            stmt.setInt(paramIdx++, request.getStartIndex());
            stmt.setInt(paramIdx, request.getRowCount());
            rs = stmt.executeQuery();
            devices = new ArrayList<>();
            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of all " +
                                                   "registered devices", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    public List<Device> searchDevicesInGroup(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Device> devices = null;

        int groupId = request.getGroupId();
        String deviceType = request.getDeviceType();
        boolean isDeviceTypeProvided = false;
        String deviceName = request.getDeviceName();
        boolean isDeviceNameProvided = false;
        String owner = request.getOwner();
        boolean isOwnerProvided = false;
        String ownerPattern = request.getOwnerPattern();
        boolean isOwnerPatternProvided = false;
        String ownership = request.getOwnership();
        boolean isOwnershipProvided = false;
        String status = request.getStatus();
        boolean isStatusProvided = false;
        String excludeStatus = request.getExcludeStatus();
        boolean isExcludeStatusProvided = false;
        Date since = request.getSince();
        boolean isSinceProvided = false;

        try {
            conn = this.getConnection();
            String sql = "SELECT d1.DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                         "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                         "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, " +
                         "(SELECT gd.DEVICE_ID, gd.DESCRIPTION, gd.NAME, gd.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE " +
                         "FROM (SELECT d.ID AS DEVICE_ID, d.DESCRIPTION,  d.NAME, d.DEVICE_IDENTIFICATION, d.DEVICE_TYPE_ID " +
                         "FROM DM_DEVICE d, (SELECT dgm.DEVICE_ID FROM DM_DEVICE_GROUP_MAP dgm WHERE  dgm.GROUP_ID = ?) dgm1 WHERE" +
                         " d.ID = dgm1.DEVICE_ID AND d.TENANT_ID = ?";


            //Add the query for device-name
            if (deviceName != null && !deviceName.isEmpty()) {
                sql = sql + " AND d.NAME LIKE ?";
                isDeviceNameProvided = true;
            }

            sql = sql + ") gd, DM_DEVICE_TYPE t";

            if (since != null) {
                sql = sql + ", DM_DEVICE_DETAIL dt";
                isSinceProvided = true;
            }

            sql = sql + " WHERE gd.DEVICE_TYPE_ID = t.ID";

            //Add query for last updated timestamp
            if (isSinceProvided) {
                sql = sql + " AND dt.DEVICE_ID = gd.DEVICE_ID AND dt.UPDATE_TIMESTAMP > ?";
            }

            //Add the query for device-type
            if (deviceType != null && !deviceType.isEmpty()) {
                sql = sql + " AND t.NAME = ?";
                isDeviceTypeProvided = true;
            }

            sql = sql + " ) d1 WHERE  d1.DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? ";

            //Add the query for ownership
            if (ownership != null && !ownership.isEmpty()) {
                sql = sql + " AND e.OWNERSHIP = ?";
                isOwnershipProvided = true;
            }
            //Add the query for owner
            if (owner != null && !owner.isEmpty()) {
                sql = sql + " AND e.OWNER = ?";
                isOwnerProvided = true;
            } else if (ownerPattern != null && !ownerPattern.isEmpty()) {
                sql = sql + " AND e.OWNER LIKE ?";
                isOwnerPatternProvided = true;
            }
            //Add the query for status
            if (status != null && !status.isEmpty()) {
                sql = sql + " AND e.STATUS = ?";
                isStatusProvided = true;
            }
            //Add the query for exclude status
            if (excludeStatus != null && !excludeStatus.isEmpty()) {
                sql = sql + " AND e.STATUS != ?";
                isExcludeStatusProvided = true;
            }

            sql = sql + " ORDER BY ENROLMENT_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, groupId);
            stmt.setInt(2, tenantId);

            int paramIdx = 3;
            if (isDeviceNameProvided) {
                stmt.setString(paramIdx++, deviceName + "%");
            }
            if (isSinceProvided) {
                stmt.setLong(paramIdx++, since.getTime());
            }
            if (isDeviceTypeProvided) {
                stmt.setString(paramIdx++, deviceType);
            }

            stmt.setInt(paramIdx++, tenantId);
            if (isOwnershipProvided) {
                stmt.setString(paramIdx++, ownership);
            }
            if (isOwnerProvided) {
                stmt.setString(paramIdx++, owner);
            } else if (isOwnerPatternProvided) {
                stmt.setString(paramIdx++, ownerPattern + "%");
            }
            if (isStatusProvided) {
                stmt.setString(paramIdx++, status);
            }
            if (isExcludeStatusProvided) {
                stmt.setString(paramIdx++, excludeStatus);
            }
            stmt.setInt(paramIdx++, request.getStartIndex());
            stmt.setInt(paramIdx, request.getRowCount());

            rs = stmt.executeQuery();
            devices = new ArrayList<>();
            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while retrieving information of" +
                                                   " devices belonging to group : " + groupId, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesOfUser(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT e1.OWNER, e1.OWNERSHIP, e1.ENROLMENT_ID, e1.DEVICE_ID, e1.STATUS, e1.DATE_OF_LAST_UPDATE," +
                         " e1.DATE_OF_ENROLMENT, d.DESCRIPTION, d.NAME AS DEVICE_NAME, d.DEVICE_IDENTIFICATION, t.NAME " +
                         "AS DEVICE_TYPE FROM DM_DEVICE d, (SELECT e.OWNER, e.OWNERSHIP, e.ID AS ENROLMENT_ID, " +
                         "e.DEVICE_ID, e.STATUS, e.DATE_OF_LAST_UPDATE, e.DATE_OF_ENROLMENT FROM DM_ENROLMENT e WHERE " +
                         "e.TENANT_ID = ? AND e.OWNER = ?) e1, DM_DEVICE_TYPE t WHERE d.ID = e1.DEVICE_ID " +
                         "AND t.ID = d.DEVICE_TYPE_ID ORDER BY ENROLMENT_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, request.getOwner());
            stmt.setInt(3, request.getStartIndex());
            stmt.setInt(4, request.getRowCount());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices belongs to '" +
                                                   request.getOwner() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesByName(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                         "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                         "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.NAME, " +
                         "d.DESCRIPTION, t.NAME AS DEVICE_TYPE, d.DEVICE_IDENTIFICATION FROM DM_DEVICE d, " +
                         "DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.NAME LIKE ? AND d.TENANT_ID = ?) d1 " +
                         "WHERE DEVICE_ID = e.DEVICE_ID AND TENANT_ID = ? ORDER BY ENROLMENT_ID " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, request.getDeviceName() + "%");
            stmt.setInt(2, tenantId);
            stmt.setInt(3, tenantId);
            stmt.setInt(4, request.getStartIndex());
            stmt.setInt(5, request.getRowCount());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches " +
                                                   "'" + request.getDeviceName() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesByOwnership(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, t.NAME AS DEVICE_TYPE, " +
                         "d.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                         "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM (SELECT e.ID, e.DEVICE_ID, e.OWNER, e.OWNERSHIP, e.STATUS, " +
                         "e.DATE_OF_ENROLMENT, e.DATE_OF_LAST_UPDATE, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e " +
                         "WHERE TENANT_ID = ? AND OWNERSHIP = ?) e, DM_DEVICE d, DM_DEVICE_TYPE t " +
                         "WHERE DEVICE_ID = e.DEVICE_ID AND d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ? ORDER BY ENROLMENT_ID " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, request.getOwnership());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, request.getStartIndex());
            stmt.setInt(5, request.getRowCount());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            throw new DeviceManagementDAOException("Error occurred while fetching the list of devices that matches to ownership " +
                                                   "'" + request.getOwnership() + "'", e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    @Override
    public List<Device> getDevicesByStatus(PaginationRequest request, int tenantId)
            throws DeviceManagementDAOException {
        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        try {
            conn = this.getConnection();
            String sql = "SELECT d.ID AS DEVICE_ID, d.DESCRIPTION, d.NAME AS DEVICE_NAME, t.NAME AS DEVICE_TYPE, " +
                         "d.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                         "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM (SELECT e.ID, e.DEVICE_ID, e.OWNER, e.OWNERSHIP, e.STATUS, " +
                         "e.DATE_OF_ENROLMENT, e.DATE_OF_LAST_UPDATE, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e " +
                         "WHERE TENANT_ID = ? AND STATUS = ?) e, DM_DEVICE d, DM_DEVICE_TYPE t " +
                         "WHERE DEVICE_ID = e.DEVICE_ID AND d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ? ORDER BY ENROLMENT_ID " +
                         "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);
            stmt.setString(2, request.getStatus());
            stmt.setInt(3, tenantId);
            stmt.setInt(4, request.getStartIndex());
            stmt.setInt(5, request.getRowCount());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching the list of devices that matches to status " +
                         "'" + request.getStatus() + "'";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, null);
        }
        return devices;
    }

    /**
     * Get the list of devices that matches with the given device name and (or) device type.
     *
     * @param deviceName Name of the device.
     * @param tenantId   Id of the current tenant
     * @return device list
     * @throws DeviceManagementDAOException
     */
    @Override
    public List<Device> getDevicesByNameAndType(String deviceName, String type, int tenantId, int offset, int limit)
            throws DeviceManagementDAOException {

        String filteringString = "";
        if (deviceName != null && !deviceName.isEmpty()) {
            filteringString = filteringString + " AND d.NAME LIKE ?";
        }

        if (type != null && !type.isEmpty()) {
            filteringString = filteringString + " AND t.NAME = ?";
        }

        Connection conn;
        PreparedStatement stmt = null;
        List<Device> devices = new ArrayList<>();
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            String sql = "SELECT d1.ID AS DEVICE_ID, d1.DESCRIPTION, d1.NAME AS DEVICE_NAME, d1.DEVICE_TYPE, " +
                "d1.DEVICE_IDENTIFICATION, e.OWNER, e.OWNERSHIP, e.STATUS, e.DATE_OF_LAST_UPDATE, " +
                    "e.DATE_OF_ENROLMENT, e.ID AS ENROLMENT_ID FROM DM_ENROLMENT e, (SELECT d.ID, d.NAME, " +
                        "d.DESCRIPTION, d.DEVICE_IDENTIFICATION, t.NAME AS DEVICE_TYPE FROM DM_DEVICE d, " +
                            "DM_DEVICE_TYPE t WHERE d.DEVICE_TYPE_ID = t.ID AND d.TENANT_ID = ?" + filteringString +
                                ") d1 WHERE d1.ID = e.DEVICE_ID ORDER BY ENROLMENT_ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, tenantId);

            int i = 1;

            if (deviceName != null && !deviceName.isEmpty()) {
                stmt.setString(++i, deviceName + "%");
            }

            if (type != null && !type.isEmpty()) {
                stmt.setString(++i, type);
            }

            stmt.setInt(++i, offset);
            stmt.setInt(++i, limit);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Device device = DeviceManagementDAOUtil.loadDevice(rs);
                devices.add(device);
            }
        } catch (SQLException e) {
            String msg = "Error occurred while fetching the list of devices corresponding" +
                         "to the mentioned filtering criteria";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        } finally {
            DeviceManagementDAOUtil.cleanupResources(stmt, rs);
        }
        return devices;
    }

    @Override
    public List<Device> getSubscribedDevices(int offsetValue, int limitValue,
                                             List<Integer> deviceIds, int tenantId, String status)
            throws DeviceManagementDAOException {
        Connection conn;

        try {
            conn = this.getConnection();
            int index = 1;

            boolean isStatusProvided = false;
            StringJoiner joiner = new StringJoiner(",",
                       "SELECT " +
                        "f.ID AS DEVICE_ID, f.NAME AS DEVICE_NAME, f.DESCRIPTION AS DESCRIPTION, " +
                        "f.DEVICE_TYPE_ID, f.DEVICE_IDENTIFICATION AS DEVICE_IDENTIFICATION, " +
                        "e.ID AS ENROLMENT_ID, e.OWNER, e.OWNERSHIP, e.DATE_OF_ENROLMENT, " +
                        "e.DATE_OF_LAST_UPDATE, e.STATUS, t.NAME AS DEVICE_TYPE " +
                        "FROM DM_ENROLMENT AS e,DM_DEVICE AS f, DM_DEVICE_TYPE t "+
                        "WHERE " +
                        "e.DEVICE_ID=f.ID AND " +
                        "e.DEVICE_ID IN (", ") AND e.TENANT_ID=?");

            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();

            if (status != null && !status.isEmpty()) {
                query = query + " AND e.STATUS=?";
                isStatusProvided = true;
            }

            query = query + " ORDER BY f.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

            try (PreparedStatement ps = conn.prepareStatement(query)) {

                for (Integer deviceId : deviceIds) {
                    ps.setObject(index++, deviceId);
                }

                ps.setInt(index++, tenantId);
                if (isStatusProvided) {
                    ps.setString(index++, status);
                }
                ps.setInt(index++, offsetValue);
                ps.setInt(index, limitValue);

                try (ResultSet rs = ps.executeQuery()) {
                    List<Device> devices = new ArrayList<>();
                    while (rs.next()) {
                        devices.add(DeviceManagementDAOUtil.loadDevice(rs));
                    }
                    return devices;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all registered devices " +
                         "according to device ids and the limit area.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DeviceManagementDAOFactory.getConnection();
    }

    @Override
    public List<Device> getDevicesByDuration(PaginationRequest request, int tenantId,
                                             String fromDate, String toDate)
            throws DeviceManagementDAOException {
        List<Device> devices;
        String deviceStatus = request.getStatus();
        String ownership = request.getOwnership();

        String sql = "SELECT " +
                     "d.ID AS DEVICE_ID, " +
                     "d.DESCRIPTION,d.NAME AS DEVICE_NAME, " +
                     "t.NAME AS DEVICE_TYPE, " +
                     "d.DEVICE_IDENTIFICATION, " +
                     "e.OWNER, " +
                     "e.OWNERSHIP, " +
                     "e.STATUS, " +
                     "e.DATE_OF_LAST_UPDATE," +
                     "e.DATE_OF_ENROLMENT, " +
                     "e.ID AS ENROLMENT_ID " +
                     "FROM DM_DEVICE AS d , DM_ENROLMENT AS e , DM_DEVICE_TYPE AS t " +
                     "WHERE d.ID = e.DEVICE_ID AND " +
                     "d.DEVICE_TYPE_ID = t.ID AND " +
                     "e.TENANT_ID = ? AND " +
                     "e.DATE_OF_ENROLMENT BETWEEN ? AND ?";

        if (deviceStatus != null) {
            sql = sql + " AND e.STATUS = ?";
        }
        if (ownership != null) {
            sql = sql + " AND e.OWNERSHIP = ?";
        }

        sql = sql + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIdx = 1;
            stmt.setInt(paramIdx++, tenantId);
            stmt.setString(paramIdx++, fromDate);
            stmt.setString(paramIdx++, toDate);
            if (deviceStatus != null) {
                stmt.setString(paramIdx++, deviceStatus);
            }
            if (ownership != null) {
                stmt.setString(paramIdx++, ownership);
            }
            stmt.setInt(paramIdx++, request.getStartIndex());
            stmt.setInt(paramIdx, request.getRowCount());
            try (ResultSet rs = stmt.executeQuery()) {
                devices = new ArrayList<>();
                while (rs.next()) {
                    Device device = DeviceManagementDAOUtil.loadDevice(rs);
                    devices.add(device);
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all " +
                         "registered devices under tenant id " + tenantId;
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
        return devices;
    }

    @Override
    public int getSubscribedDeviceCount(List<Integer> deviceIds, int tenantId, String status)
            throws DeviceManagementDAOException {
        try {
            Connection conn = this.getConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT " +
                            "COUNT(e.DEVICE_ID) AS DEVICE_ID "+
                            "FROM DM_ENROLMENT AS e, DM_DEVICE AS f "+
                            "WHERE " +
                            "e.DEVICE_ID=f.ID AND " +
                            "e.DEVICE_ID IN (", ") AND e.TENANT_ID = ?");

            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();

            if (!StringUtils.isBlank(status)) {
                query = query + " AND e.STATUS = ?";
            }

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Integer deviceId : deviceIds) {
                    ps.setObject(index++, deviceId);
                }

                ps.setInt(index++, tenantId);
                if (!StringUtils.isBlank(status)) {
                    ps.setString(index, status);
                }

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("DEVICE_ID");
                    }
                    return 0;
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving information of all registered devices " +
                    "according to device ids and the limit area.";
            log.error(msg, e);
            throw new DeviceManagementDAOException(msg, e);
        }
    }
}