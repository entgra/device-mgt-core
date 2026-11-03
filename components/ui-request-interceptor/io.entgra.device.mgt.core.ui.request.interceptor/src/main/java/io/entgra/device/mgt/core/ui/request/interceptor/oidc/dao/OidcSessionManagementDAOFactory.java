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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Owns connections and thread-bound transactions for the dedicated OIDC session datasource.
 * A connection opened by this factory must always be closed on the same request thread.
 */
public final class OidcSessionManagementDAOFactory {

    private static final Log log = LogFactory.getLog(OidcSessionManagementDAOFactory.class);
    public static final String DATA_SOURCE_NAME = "jdbc/SESSION_DS";

    private static final ThreadLocal<Connection> CURRENT_CONNECTION = new ThreadLocal<>();
    private static volatile DataSource dataSource;

    private OidcSessionManagementDAOFactory() {
    }

    public static void init(DataSource configuredDataSource) {
        if (configuredDataSource == null) {
            throw new IllegalArgumentException("OIDC session management data source cannot be null");
        }
        dataSource = configuredDataSource;
        if (log.isDebugEnabled()) {
            log.debug("Initialized OIDC session datasource from an explicitly supplied DataSource");
        }
    }

    public static void beginTransaction() throws SQLException {
        ensureNoConnection();
        Connection connection = getDataSource().getConnection();
        try {
            // A pooled connection can retain transaction state from a previous borrower. Roll back any such
            // transaction before using the connection for a new request.
            if (!connection.getAutoCommit()) {
                connection.rollback();
            }
            connection.setAutoCommit(false);
            CURRENT_CONNECTION.set(connection);
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    public static void openConnection() throws SQLException {
        ensureNoConnection();
        Connection connection = getDataSource().getConnection();
        try {
            // Non-transactional DAO operations rely on auto-commit. Normalize the state because the datasource may
            // return a pooled connection previously used by beginTransaction().
            if (!connection.getAutoCommit()) {
                connection.rollback();
                connection.setAutoCommit(true);
            }
            CURRENT_CONNECTION.set(connection);
        } catch (SQLException e) {
            connection.close();
            throw e;
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection == null) {
            throw new SQLException("No UI request interceptor database connection is associated with this thread");
        }
        return connection;
    }

    public static void commitTransaction() throws SQLException {
        getConnection().commit();
    }

    public static void rollbackTransaction() throws SQLException {
        getConnection().rollback();
    }

    public static void closeConnection() throws SQLException {
        Connection connection = CURRENT_CONNECTION.get();
        if (connection == null) {
            return;
        }
        SQLException failure = null;
        try {
            if (!connection.getAutoCommit()) {
                try {
                    connection.rollback();
                } catch (SQLException e) {
                    failure = e;
                }
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    if (failure == null) {
                        failure = e;
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            } finally {
                CURRENT_CONNECTION.remove();
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private static DataSource getDataSource() throws SQLException {
        DataSource currentDataSource = dataSource;
        if (currentDataSource == null) {
            synchronized (OidcSessionManagementDAOFactory.class) {
                currentDataSource = dataSource;
                if (currentDataSource == null) {
                    try {
                        currentDataSource = InitialContext.doLookup(DATA_SOURCE_NAME);
                        dataSource = currentDataSource;
                        log.info("Initialized OIDC session datasource using JNDI name: " + DATA_SOURCE_NAME);
                    } catch (NamingException e) {
                        throw new SQLException("Unable to resolve OIDC session datasource " + DATA_SOURCE_NAME, e);
                    }
                }
            }
        }
        return currentDataSource;
    }

    private static void ensureNoConnection() throws SQLException {
        if (CURRENT_CONNECTION.get() != null) {
            throw new SQLException("A UI request interceptor database connection is already associated with this thread");
        }
    }
}
