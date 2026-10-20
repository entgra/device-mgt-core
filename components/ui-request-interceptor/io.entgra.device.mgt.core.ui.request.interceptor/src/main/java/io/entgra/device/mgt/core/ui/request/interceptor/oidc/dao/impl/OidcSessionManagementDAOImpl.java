/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao.impl;

import io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean.OidcSessionMapping;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao.OidcSessionManagementDAO;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao.OidcSessionManagementDAOFactory;
import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcSessionManagementDAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation for the cluster-wide OIDC session state and its servlet-session mappings.
 */
public class OidcSessionManagementDAOImpl implements OidcSessionManagementDAO {

    @Override
    public void addSessionState(OidcSessionMapping mapping) throws OidcSessionManagementDAOException {
        String sql = "INSERT INTO DM_OIDC_SESSION_STATE (ISSUER, CLIENT_ID, SID, SUBJECT, APPLICATION_NAME, " +
                "SESSION_STATUS, LOGOUT_JTI, CREATED_AT, LAST_SEEN_AT, REVOKED_AT, EXPIRES_AT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            stmt.setString(1, mapping.getIssuer());
            stmt.setString(2, mapping.getClientId());
            stmt.setString(3, mapping.getSid());
            stmt.setString(4, mapping.getSubject());
            stmt.setString(5, mapping.getApplicationName());
            stmt.setString(6, mapping.getStatus());
            stmt.setString(7, mapping.getLogoutJti());
            stmt.setLong(8, mapping.getCreatedAt());
            stmt.setLong(9, mapping.getLastSeenAt());
            if (mapping.getRevokedAt() == 0) {
                stmt.setNull(10, Types.BIGINT);
            } else {
                stmt.setLong(10, mapping.getRevokedAt());
            }
            stmt.setLong(11, mapping.getExpiresAt());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error adding OIDC session state", e);
        }
    }

    @Override
    public int updateActiveSessionState(OidcSessionMapping mapping) throws OidcSessionManagementDAOException {
        String sql = "UPDATE DM_OIDC_SESSION_STATE SET SUBJECT = ?, LAST_SEEN_AT = ?, " +
                "EXPIRES_AT = CASE WHEN EXPIRES_AT < ? THEN ? ELSE EXPIRES_AT END " +
                "WHERE ISSUER = ? AND CLIENT_ID = ? AND SID = ? AND APPLICATION_NAME = ? " +
                "AND SESSION_STATUS = 'ACTIVE'";
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            stmt.setString(1, mapping.getSubject());
            stmt.setLong(2, mapping.getLastSeenAt());
            stmt.setLong(3, mapping.getExpiresAt());
            stmt.setLong(4, mapping.getExpiresAt());
            stmt.setString(5, mapping.getIssuer());
            stmt.setString(6, mapping.getClientId());
            stmt.setString(7, mapping.getSid());
            stmt.setString(8, mapping.getApplicationName());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error updating active OIDC session state", e);
        }
    }

    @Override
    public OidcSessionMapping getSessionStateForUpdate(String issuer, String clientId, String sid,
                                                       String applicationName)
            throws OidcSessionManagementDAOException {
        // A no-op update obtains a row lock using syntax supported by every SESSION_DS database. SQL Server does not
        // support SELECT ... FOR UPDATE, so using a write lock here keeps registration and logout ordering portable.
        String lockSql = "UPDATE DM_OIDC_SESSION_STATE SET LAST_SEEN_AT = LAST_SEEN_AT WHERE ISSUER = ? " +
                "AND CLIENT_ID = ? AND SID = ? AND APPLICATION_NAME = ?";
        String sql = "SELECT SUBJECT, SESSION_STATUS, LOGOUT_JTI, CREATED_AT, LAST_SEEN_AT, REVOKED_AT, " +
                "EXPIRES_AT FROM DM_OIDC_SESSION_STATE WHERE ISSUER = ? AND CLIENT_ID = ? AND SID = ? " +
                "AND APPLICATION_NAME = ?";
        try (PreparedStatement lock = connection().prepareStatement(lockSql);
             PreparedStatement stmt = connection().prepareStatement(sql)) {
            lock.setString(1, issuer);
            lock.setString(2, clientId);
            lock.setString(3, sid);
            lock.setString(4, applicationName);
            lock.executeUpdate();
            stmt.setString(1, issuer);
            stmt.setString(2, clientId);
            stmt.setString(3, sid);
            stmt.setString(4, applicationName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                OidcSessionMapping mapping = new OidcSessionMapping();
                mapping.setIssuer(issuer);
                mapping.setClientId(clientId);
                mapping.setSid(sid);
                mapping.setApplicationName(applicationName);
                mapping.setSubject(rs.getString("SUBJECT"));
                mapping.setStatus(rs.getString("SESSION_STATUS"));
                mapping.setLogoutJti(rs.getString("LOGOUT_JTI"));
                mapping.setCreatedAt(rs.getLong("CREATED_AT"));
                mapping.setLastSeenAt(rs.getLong("LAST_SEEN_AT"));
                mapping.setRevokedAt(rs.getLong("REVOKED_AT"));
                mapping.setExpiresAt(rs.getLong("EXPIRES_AT"));
                return mapping;
            }
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error locking OIDC session state", e);
        }
    }

    @Override
    public void addSessionMapping(OidcSessionMapping mapping) throws OidcSessionManagementDAOException {
        String sql = "INSERT INTO DM_OIDC_SESSION_MAPPING (ISSUER, CLIENT_ID, SID, APPLICATION_NAME, " +
                "HTTP_SESSION_ID, CREATED_AT, LAST_SEEN_AT, SESSION_TIMEOUT_MS, EXPIRES_AT) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            bindMapping(stmt, mapping);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error adding OIDC servlet-session mapping", e);
        }
    }

    @Override
    public int updateSessionMapping(OidcSessionMapping mapping) throws OidcSessionManagementDAOException {
        String sql = "UPDATE DM_OIDC_SESSION_MAPPING SET ISSUER = ?, CLIENT_ID = ?, SID = ?, CREATED_AT = ?, " +
                "LAST_SEEN_AT = ?, SESSION_TIMEOUT_MS = ?, EXPIRES_AT = ? " +
                "WHERE APPLICATION_NAME = ? AND HTTP_SESSION_ID = ?";
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            stmt.setString(1, mapping.getIssuer());
            stmt.setString(2, mapping.getClientId());
            stmt.setString(3, mapping.getSid());
            stmt.setLong(4, mapping.getCreatedAt());
            stmt.setLong(5, mapping.getLastSeenAt());
            stmt.setLong(6, mapping.getSessionTimeoutMillis());
            stmt.setLong(7, mapping.getExpiresAt());
            stmt.setString(8, mapping.getApplicationName());
            stmt.setString(9, mapping.getHttpSessionId());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error updating OIDC servlet-session mapping", e);
        }
    }

    @Override
    public List<String> getHttpSessionIds(String issuer, String clientId, String sid, String applicationName)
            throws OidcSessionManagementDAOException {
        String sql = "SELECT HTTP_SESSION_ID FROM DM_OIDC_SESSION_MAPPING WHERE ISSUER = ? AND CLIENT_ID = ? " +
                "AND SID = ? AND APPLICATION_NAME = ?";
        List<String> sessionIds = new ArrayList<>();
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            stmt.setString(1, issuer);
            stmt.setString(2, clientId);
            stmt.setString(3, sid);
            stmt.setString(4, applicationName);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessionIds.add(rs.getString(1));
                }
            }
            return sessionIds;
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error retrieving OIDC servlet-session mappings", e);
        }
    }

    @Override
    public boolean isRevoked(String applicationName, String httpSessionId, long now, long touchBefore)
            throws OidcSessionManagementDAOException {
        String sql = "SELECT S.ISSUER, S.CLIENT_ID, S.SID, S.SESSION_STATUS, M.LAST_SEEN_AT, " +
                "M.SESSION_TIMEOUT_MS FROM DM_OIDC_SESSION_MAPPING M INNER JOIN DM_OIDC_SESSION_STATE S " +
                "ON S.ISSUER = M.ISSUER AND S.CLIENT_ID = M.CLIENT_ID AND S.SID = M.SID " +
                "AND S.APPLICATION_NAME = M.APPLICATION_NAME WHERE M.APPLICATION_NAME = ? AND M.HTTP_SESSION_ID = ?";
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            stmt.setString(1, applicationName);
            stmt.setString(2, httpSessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return true;
                }
                boolean revoked = "REVOKED".equals(rs.getString("SESSION_STATUS"));
                long lastSeenAt = rs.getLong("LAST_SEEN_AT");
                long timeoutMillis = rs.getLong("SESSION_TIMEOUT_MS");
                long maximumTouchInterval = now - touchBefore;
                long touchInterval = Math.min(maximumTouchInterval, Math.max(1L, timeoutMillis / 4));
                long sessionTouchBefore = now - touchInterval;
                if (!revoked && lastSeenAt < sessionTouchBefore) {
                    return !touchActiveSession(rs.getString("ISSUER"), rs.getString("CLIENT_ID"),
                            rs.getString("SID"), applicationName, httpSessionId, now, timeoutMillis,
                            sessionTouchBefore);
                }
                return revoked;
            }
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error checking OIDC session state", e);
        }
    }

    @Override
    public void revoke(String issuer, String clientId, String sid, String applicationName, String jti,
                       long revokedAt, long expiresAt) throws OidcSessionManagementDAOException {
        String sql = "UPDATE DM_OIDC_SESSION_STATE SET SESSION_STATUS = 'REVOKED', LOGOUT_JTI = ?, " +
                "REVOKED_AT = ?, EXPIRES_AT = ? WHERE ISSUER = ? AND CLIENT_ID = ? AND SID = ? " +
                "AND APPLICATION_NAME = ? AND SESSION_STATUS = 'ACTIVE'";
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            stmt.setString(1, jti);
            stmt.setLong(2, revokedAt);
            stmt.setLong(3, expiresAt);
            stmt.setString(4, issuer);
            stmt.setString(5, clientId);
            stmt.setString(6, sid);
            stmt.setString(7, applicationName);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error revoking OIDC session state", e);
        }
    }

    @Override
    public void deleteActiveBySession(String applicationName, String httpSessionId)
            throws OidcSessionManagementDAOException {
        String sql = "DELETE FROM DM_OIDC_SESSION_MAPPING WHERE APPLICATION_NAME = ? AND HTTP_SESSION_ID = ? " +
                "AND EXISTS (SELECT 1 FROM DM_OIDC_SESSION_STATE S WHERE S.ISSUER = DM_OIDC_SESSION_MAPPING.ISSUER " +
                "AND S.CLIENT_ID = DM_OIDC_SESSION_MAPPING.CLIENT_ID AND S.SID = DM_OIDC_SESSION_MAPPING.SID " +
                "AND S.APPLICATION_NAME = DM_OIDC_SESSION_MAPPING.APPLICATION_NAME AND S.SESSION_STATUS = 'ACTIVE')";
        executeUpdate(sql, applicationName, httpSessionId);
    }

    @Override
    public boolean claimCleanup(String taskName, long lastRunAt, long runBefore)
            throws OidcSessionManagementDAOException {
        String sql = "UPDATE DM_OIDC_SESSION_CLEANUP SET LAST_RUN_AT = ? WHERE TASK_NAME = ? AND LAST_RUN_AT < ?";
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            stmt.setLong(1, lastRunAt);
            stmt.setString(2, taskName);
            stmt.setLong(3, runBefore);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error claiming OIDC session cleanup", e);
        }
    }

    @Override
    public void deleteExpired(long timestamp) throws OidcSessionManagementDAOException {
        String sql = "DELETE FROM DM_OIDC_SESSION_STATE WHERE EXPIRES_AT < ?";
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            stmt.setLong(1, timestamp);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error deleting expired OIDC session state", e);
        }
    }

    private boolean touchActiveSession(String issuer, String clientId, String sid, String applicationName,
                                       String httpSessionId, long now, long timeoutMillis, long touchBefore)
            throws SQLException {
        long expiresAt = now + timeoutMillis;
        // Extend the authoritative SID state first. If logout or cleanup removed/revoked it after the preceding read,
        // fail closed instead of refreshing an orphaned servlet-session mapping.
        String stateSql = "UPDATE DM_OIDC_SESSION_STATE SET LAST_SEEN_AT = ?, " +
                "EXPIRES_AT = CASE WHEN EXPIRES_AT < ? THEN ? ELSE EXPIRES_AT END WHERE ISSUER = ? " +
                "AND CLIENT_ID = ? AND SID = ? AND APPLICATION_NAME = ? AND SESSION_STATUS = 'ACTIVE'";
        try (PreparedStatement stmt = connection().prepareStatement(stateSql)) {
            stmt.setLong(1, now);
            stmt.setLong(2, expiresAt);
            stmt.setLong(3, expiresAt);
            stmt.setString(4, issuer);
            stmt.setString(5, clientId);
            stmt.setString(6, sid);
            stmt.setString(7, applicationName);
            if (stmt.executeUpdate() == 0) {
                return false;
            }
        }
        String mappingSql = "UPDATE DM_OIDC_SESSION_MAPPING SET LAST_SEEN_AT = ?, EXPIRES_AT = ? WHERE " +
                "APPLICATION_NAME = ? AND HTTP_SESSION_ID = ? AND LAST_SEEN_AT < ?";
        try (PreparedStatement stmt = connection().prepareStatement(mappingSql)) {
            stmt.setLong(1, now);
            stmt.setLong(2, expiresAt);
            stmt.setString(3, applicationName);
            stmt.setString(4, httpSessionId);
            stmt.setLong(5, touchBefore);
            stmt.executeUpdate();
        }
        return true;
    }

    private void bindMapping(PreparedStatement stmt, OidcSessionMapping mapping) throws SQLException {
        stmt.setString(1, mapping.getIssuer());
        stmt.setString(2, mapping.getClientId());
        stmt.setString(3, mapping.getSid());
        stmt.setString(4, mapping.getApplicationName());
        stmt.setString(5, mapping.getHttpSessionId());
        stmt.setLong(6, mapping.getCreatedAt());
        stmt.setLong(7, mapping.getLastSeenAt());
        stmt.setLong(8, mapping.getSessionTimeoutMillis());
        stmt.setLong(9, mapping.getExpiresAt());
    }

    private void executeUpdate(String sql, String... values) throws OidcSessionManagementDAOException {
        try (PreparedStatement stmt = connection().prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                stmt.setString(i + 1, values[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new OidcSessionManagementDAOException("Error updating OIDC session mappings", e);
        }
    }

    private Connection connection() throws SQLException {
        return OidcSessionManagementDAOFactory.getConnection();
    }
}
