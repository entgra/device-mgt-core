/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.device.application.mgt.core.dao.impl.subscription;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.SubAction;
import org.wso2.carbon.device.application.mgt.common.SubsciptionType;
import org.wso2.carbon.device.application.mgt.common.dto.DeviceSubscriptionDTO;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.core.dao.SubscriptionDAO;
import org.wso2.carbon.device.application.mgt.core.util.DAOUtil;
import org.wso2.carbon.device.application.mgt.core.dao.impl.AbstractDAOImpl;
import org.wso2.carbon.device.application.mgt.core.exception.ApplicationManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class GenericSubscriptionDAOImpl extends AbstractDAOImpl implements SubscriptionDAO {
    private static Log log = LogFactory.getLog(GenericSubscriptionDAOImpl.class);

    @Override
    public List<Integer> addDeviceSubscription(String subscribedBy, List<Integer> deviceIds,
            String subscribedFrom, String installStatus, int releaseId, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO "
                + "AP_DEVICE_SUBSCRIPTION("
                + "SUBSCRIBED_BY, "
                + "SUBSCRIBED_TIMESTAMP, "
                + "ACTION_TRIGGERED_FROM, "
                + "STATUS, "
                + "DM_DEVICE_ID, "
                + "AP_APP_RELEASE_ID,"
                + "TENANT_ID) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (Integer deviceId : deviceIds) {
                    stmt.setString(1, subscribedBy);
                    stmt.setTimestamp(2, timestamp);
                    stmt.setString(3, subscribedFrom);
                    stmt.setString(4, installStatus);
                    stmt.setInt(5, deviceId);
                    stmt.setInt(6, releaseId);
                    stmt.setInt(7, tenantId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding a device subscription for device id " + deviceId + " and application "
                                + "release which has release id" + releaseId);
                    }
                }
                stmt.executeBatch();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    List<Integer> deviceSubIds = new ArrayList<>();
                    while (rs.next()) {
                        deviceSubIds.add(rs.getInt(1));
                    }
                    return deviceSubIds;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occured while obtaining database connection to add device subscription for application "
                    + "release which has release Id" + releaseId;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occured when processing SQL to add device subscription for application release which"
                    + " has release Id " + releaseId;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> updateDeviceSubscription(String updateBy, List<Integer> deviceIds,
            boolean isUnsubscribed, String actionTriggeredFrom, String installStatus, int releaseId, int tenantId)
            throws ApplicationManagementDAOException {
        try {
            String sql = "UPDATE AP_DEVICE_SUBSCRIPTION SET ";

            if (isUnsubscribed) {
                sql += "UNSUBSCRIBED = true, UNSUBSCRIBED_BY = ?, UNSUBSCRIBED_TIMESTAMP = ?, ";
            } else {
                sql += "SUBSCRIBED_BY = ?, SUBSCRIBED_TIMESTAMP = ?, ";
            }
            sql += "ACTION_TRIGGERED_FROM = ?, " +
                    "STATUS = ? " +
                    "WHERE " +
                    "DM_DEVICE_ID = ? AND " +
                    "AP_APP_RELEASE_ID = ? AND " +
                    "TENANT_ID = ?";

            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (Integer deviceId : deviceIds) {
                    stmt.setString(1, updateBy);
                    stmt.setTimestamp(2, timestamp);
                    stmt.setString(3, actionTriggeredFrom);
                    stmt.setString(4, installStatus);
                    stmt.setInt(5, deviceId);
                    stmt.setInt(6, releaseId);
                    stmt.setInt(7, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                try (ResultSet rs = stmt.getGeneratedKeys()){
                    List<Integer> updatedDeviceSubIds = new ArrayList<>();
                    while (rs.next()) {
                        updatedDeviceSubIds.add(rs.getInt(1));
                    }
                    return updatedDeviceSubIds;
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update device subscriptions of "
                    + "application. Updated by: " + updateBy + " and updating action triggered from "
                    + actionTriggeredFrom;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when obtaining database connection for updating the device subscriptions of "
                    + "application. Updated by: " + updateBy + " and updating action triggered from "
                    + actionTriggeredFrom;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addOperationMapping(int operationId, List<Integer> deviceSubscriptionIds, int tenantId)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO "
                + "AP_APP_SUB_OP_MAPPING("
                + "OPERATION_ID, "
                + "AP_DEVICE_SUBSCRIPTION_ID, "
                + "TENANT_ID) "
                + "VALUES (?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                for (Integer subId : deviceSubscriptionIds) {
                    stmt.setInt(1, operationId);
                    stmt.setInt(2, subId);
                    stmt.setInt(3, tenantId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding a operation mapping for subscription id " + subId);
                    }
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to add operation subscription mapping to DB";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to add operation subscription mapping to DB. Executed "
                    + "query: " + sql;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addUserSubscriptions(int tenantId, String subscribedBy, List<String> users, int releaseId)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO "
                + "AP_USER_SUBSCRIPTION("
                + "TENANT_ID, "
                + "SUBSCRIBED_BY, "
                + "SUBSCRIBED_TIMESTAMP, "
                + "USER_NAME, "
                + "AP_APP_RELEASE_ID) "
                + "VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (String user : users) {
                    stmt.setInt(1, tenantId);
                    stmt.setString(2, subscribedBy);
                    stmt.setTimestamp(3, timestamp);
                    stmt.setString(4, user);
                    stmt.setInt(5, releaseId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding an user subscription for user " + user + " and application release which "
                                + "has Id " + releaseId + " to the database.");
                    }
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to add user subscription. Subscribing user "
                    + "is " + subscribedBy;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to add user subscription. Subscribing user is "
                    + subscribedBy + " and executed query: " + sql;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addRoleSubscriptions(int tenantId, String subscribedBy, List<String> roles, int releaseId)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO "
                + "AP_ROLE_SUBSCRIPTION("
                + "TENANT_ID, "
                + "SUBSCRIBED_BY, "
                + "SUBSCRIBED_TIMESTAMP, "
                + "ROLE_NAME, "
                + "AP_APP_RELEASE_ID) "
                + "VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (String role : roles) {
                    stmt.setInt(1, tenantId);
                    stmt.setString(2, subscribedBy);
                    stmt.setTimestamp(3, timestamp);
                    stmt.setString(4, role);
                    stmt.setInt(5, releaseId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding a role subscription for role " + role + " and application release which "
                                + "has Id " + releaseId + " to the database.");
                    }
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to add role subscription. Subscribing role "
                    + "is " + subscribedBy;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to add role subscription. Subscribing role is "
                    + subscribedBy + " and executed query: " + sql;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void addGroupSubscriptions(int tenantId, String subscribedBy, List<String> groups, int releaseId)
            throws ApplicationManagementDAOException {
        String sql = "INSERT INTO "
                + "AP_GROUP_SUBSCRIPTION("
                + "TENANT_ID, "
                + "SUBSCRIBED_BY, "
                + "SUBSCRIBED_TIMESTAMP, "
                + "GROUP_NAME, "
                + "AP_APP_RELEASE_ID) "
                + "VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (String group : groups) {
                    stmt.setInt(1, tenantId);
                    stmt.setString(2, subscribedBy);
                    stmt.setTimestamp(3, timestamp);
                    stmt.setString(4, group);
                    stmt.setInt(5, releaseId);
                    stmt.addBatch();
                    if (log.isDebugEnabled()) {
                        log.debug("Adding a group subscription for role " + group + " and application release which "
                                + "has Id " + releaseId + " to the database.");
                    }
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while getting database connection to add group subscription. Subscribing "
                    + "group is " + subscribedBy;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while executing query to add group subscription. Subscribing group is "
                    + subscribedBy + " and executed query: " + sql;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<DeviceSubscriptionDTO> getDeviceSubscriptions(int appReleaseId, int tenantId) throws
            ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Getting device subscriptions for the application release id " + appReleaseId
                    + " from the database");
        }
        String sql = "SELECT "
                + "DS.ID AS ID, "
                + "DS.SUBSCRIBED_BY AS SUBSCRIBED_BY, "
                + "DS.SUBSCRIBED_TIMESTAMP AS SUBSCRIBED_AT, "
                + "DS.UNSUBSCRIBED AS IS_UNSUBSCRIBED, "
                + "DS.UNSUBSCRIBED_BY AS UNSUBSCRIBED_BY, "
                + "DS.UNSUBSCRIBED_TIMESTAMP AS UNSUBSCRIBED_AT, "
                + "DS.ACTION_TRIGGERED_FROM AS ACTION_TRIGGERED_FROM, "
                + "DS.DM_DEVICE_ID AS DEVICE_ID "
                + "FROM AP_DEVICE_SUBSCRIPTION DS "
                + "WHERE DS.AP_APP_RELEASE_ID = ? AND DS.TENANT_ID=?";
        try {
            Connection conn = this.getDBConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, appReleaseId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully retrieved device subscriptions for application release id "
                                + appReleaseId);
                    }
                    return DAOUtil.loadDeviceSubscriptions(rs);
                }
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection for getting device subscription for "
                    + "application Id: " + appReleaseId + ".";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while getting device subscription data for application ID: " + appReleaseId;
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public Map<Integer, DeviceSubscriptionDTO> getDeviceSubscriptions(List<Integer> deviceIds, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get device subscriptions for given device ids.");
        }
        try {
            Connection conn = this.getDBConnection();
            int index = 1;
            Map<Integer, DeviceSubscriptionDTO> deviceSubscriptionDTOHashMap = new HashMap<>();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT "
                            + "DS.ID AS ID, "
                            + "DS.SUBSCRIBED_BY AS SUBSCRIBED_BY, "
                            + "DS.SUBSCRIBED_TIMESTAMP AS SUBSCRIBED_AT, "
                            + "DS.UNSUBSCRIBED AS IS_UNSUBSCRIBED, "
                            + "DS.UNSUBSCRIBED_BY AS UNSUBSCRIBED_BY, "
                            + "DS.UNSUBSCRIBED_TIMESTAMP AS UNSUBSCRIBED_AT, "
                            + "DS.ACTION_TRIGGERED_FROM AS ACTION_TRIGGERED_FROM, "
                            + "DS.DM_DEVICE_ID AS DEVICE_ID, "
                            + "DS.STATUS AS STATUS "
                            + "FROM AP_DEVICE_SUBSCRIPTION DS "
                            + "WHERE DS.DM_DEVICE_ID IN (", ") AND TENANT_ID = ?");
            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Integer deviceId : deviceIds) {
                    ps.setObject(index++, deviceId);
                }
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        DeviceSubscriptionDTO deviceSubscriptionDTO = DAOUtil.constructDeviceSubscriptionDTO(rs);
                        deviceSubscriptionDTOHashMap.put(deviceSubscriptionDTO.getId(), deviceSubscriptionDTO);
                    }
                }
            }
            return deviceSubscriptionDTOHashMap;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get device subscriptions for given device"
                    + " Ids.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting device subscriptions for given device Ids.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getSubscribedUserNames(List<String> users, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed users for given list of user names.");
        }
        try {
            Connection conn = this.getDBConnection();
            int index = 1;
            List<String> subscribedUsers = new ArrayList<>();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT US.USER_NAME AS USER "
                            + "FROM AP_USER_SUBSCRIPTION US "
                            + "WHERE US.USER_NAME IN (", ") AND TENANT_ID = ?");
            users.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String username : users) {
                    ps.setObject(index++, username);
                }
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedUsers.add(rs.getString("USER"));
                    }
                }
            }
            return subscribedUsers;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already subscribed users for given "
                    + "user names.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting subscribed users for given user names.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getSubscribedRoleNames(List<String> roles, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed role names for given list of roles.");
        }
        try {
            Connection conn = this.getDBConnection();
            int index = 1;
            List<String> subscribedUsers = new ArrayList<>();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT RS.ROLE_NAME AS ROLE "
                            + "FROM AP_ROLE_SUBSCRIPTION RS "
                            + "WHERE RS.ROLE_NAME IN (", ") AND TENANT_ID = ?");
            roles.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String roleName : roles) {
                    ps.setObject(index++, roleName);
                }
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedUsers.add(rs.getString("ROLE"));
                    }
                }
            }
            return subscribedUsers;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to getg subscribed roles for given role "
                    + "names.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SWL Error occurred while getting subscribes roles for given role names.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<String> getSubscribedGroupNames(List<String> groups, int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received in DAO Layer to get already subscribed groups for given list of groups.");
        }
        try {
            Connection conn = this.getDBConnection();
            int index = 1;
            List<String> subscribedUsers = new ArrayList<>();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT GS.GROUP_NAME AS GROUP "
                            + "FROM AP_GROUP_SUBSCRIPTION GS "
                            + "WHERE GS.GROUP_NAME IN (", ") AND TENANT_ID = ?");
            groups.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (String groupName : groups) {
                    ps.setObject(index++, groupName);
                }
                ps.setInt(index, tenantId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedUsers.add(rs.getString("GROUP"));
                    }
                }
            }
            return subscribedUsers;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get already subscribed groups for given "
                    + "group names.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting already subscribed groups for given group names.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getSubscribedDeviceIds(List<Integer> deviceIds, int applicationReleaseId,
            int tenantId)
            throws ApplicationManagementDAOException {
        if (log.isDebugEnabled()) {
            log.debug("Request received to DAO Layer to get already subscribed dvice Ids for given list of device Ids.");
        }
        try {
            Connection conn = this.getDBConnection();
            int index = 1;
            List<Integer> subscribedDevices = new ArrayList<>();
            StringJoiner joiner = new StringJoiner(",",
                    "SELECT DS.DM_DEVICE_ID "
                            + "FROM AP_DEVICE_SUBSCRIPTION DS "
                            + "WHERE DS.DM_DEVICE_ID IN (", ") AND AP_APP_RELEASE_ID = ? AND TENANT_ID = ?");
            deviceIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                for (Integer deviceId : deviceIds) {
                    ps.setObject(index++, deviceId);
                }
                ps.setInt(index++, tenantId);
                ps.setInt(index, applicationReleaseId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        subscribedDevices.add(rs.getInt("DM_DEVICE_ID"));
                    }
                }
            }
            return subscribedDevices;
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get subscribed device Ids for given "
                    + "device Id list.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "SQL Error occurred while getting already subscribed device ids for given device Id list.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public void updateSubscriptions(int tenantId, String updateBy, List<String> paramList, int releaseId,
            String subType, String action) throws ApplicationManagementDAOException {
        try {
            Connection conn = this.getDBConnection();
            String sql = "UPDATE ";
            if (SubsciptionType.USER.toString().equalsIgnoreCase(subType)) {
                sql += "AP_USER_SUBSCRIPTION SET ";
            } else if (SubsciptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                sql += "AP_ROLE_SUBSCRIPTION SET ";
            } else if (SubsciptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                sql += "AP_GROUP_SUBSCRIPTION SET ";
            }

            if (SubAction.UNINSTALL.toString().equalsIgnoreCase(action)) {
                sql += "UNSUBSCRIBED = true, UNSUBSCRIBED_BY = ?, UNSUBSCRIBED_TIMESTAMP = ? ";
            } else {
                sql += "SUBSCRIBED_BY = ?, SUBSCRIBED_TIMESTAMP = ? ";
            }

            if (SubsciptionType.USER.toString().equalsIgnoreCase(subType)) {
                sql += "WHERE USER_NAME = ? ";
            } else if (SubsciptionType.ROLE.toString().equalsIgnoreCase(subType)) {
                sql += "WHERE ROLE_NAME = ? ";
            } else if (SubsciptionType.GROUP.toString().equalsIgnoreCase(subType)) {
                sql += "WHERE GROUP_NAME = ? ";
            }

            sql += "AND AP_APP_RELEASE_ID = ? AND TENANT_ID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                Calendar calendar = Calendar.getInstance();
                Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
                for (String username : paramList) {
                    stmt.setString(1, updateBy);
                    stmt.setTimestamp(2, timestamp);
                    stmt.setString(3, username);
                    stmt.setInt(4, releaseId);
                    stmt.setInt(5, tenantId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update the user subscriptions of "
                    + "application.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when obtaining database connection for updating the user subscriptions of "
                    + "application.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public List<Integer> getDeviceSubIdsForOperation(int operationId, int tenantId)
            throws ApplicationManagementDAOException {
        try {
            Connection conn = this.getDBConnection();
            List<Integer> deviceSubIds = new ArrayList<>();
            String sql = "SELECT "
                    + "ID "
                    + "FROM AP_APP_SUB_OP_MAPPING "
                    + "WHERE "
                    + "OPERATION_ID = ? AND "
                    + "TENANT_ID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, operationId);
                stmt.setInt(2, tenantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        deviceSubIds.add(rs.getInt("ID"));
                    }
                }
                return deviceSubIds;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to get app device subscription ids for given "
                    + "operation.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when processing SQL to get app device subscription ids for given operation.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }

    @Override
    public boolean updateDeviceSubStatus(int deviceId, List<Integer> deviceSubIds, String status, int tenantId)
            throws ApplicationManagementDAOException {
        try {
            Connection conn = this.getDBConnection();
            int index = 1;
            StringJoiner joiner = new StringJoiner(",",
                    "UPDATE AP_DEVICE_SUBSCRIPTION SET STATUS = ? "
                            + "WHERE ID IN (",
                    ") AND DM_DEVICE_ID = ? AND TENANT_ID = ?");
            deviceSubIds.stream().map(ignored -> "?").forEach(joiner::add);
            String query = joiner.toString();
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(index++, status);
                for (Integer deviceSubId : deviceSubIds) {
                    ps.setObject(index++, deviceSubId);
                }
                ps.setInt(index++, deviceId);
                ps.setInt(index, tenantId);
                return ps.executeUpdate() != 0;
            }
        } catch (DBConnectionException e) {
            String msg = "Error occurred while obtaining the DB connection to update the subscription status of the "
                    + "device subscription.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred when obtaining database connection for updating the subscription status of the "
                    + "device subscription.";
            log.error(msg);
            throw new ApplicationManagementDAOException(msg, e);
        }
    }
}