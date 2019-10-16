/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package org.wso2.carbon.device.application.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.application.mgt.common.exception.DBConnectionException;
import org.wso2.carbon.device.application.mgt.common.exception.IllegalTransactionStateException;
import org.wso2.carbon.device.application.mgt.common.exception.TransactionManagementException;

import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * ConnectionManagerUtil is responsible for handling all the datasource connections utilities.
 */
public class ConnectionManagerUtil {

    private static final Log log = LogFactory.getLog(ConnectionManagerUtil.class);

    private enum TxState {
        CONNECTION_NOT_BORROWED, CONNECTION_BORROWED, CONNECTION_CLOSED
    }

    private static final ThreadLocal<Connection> currentConnection = new ThreadLocal<>();
    private static ThreadLocal<TxState> currentTxState = new ThreadLocal<>();
    private static DataSource dataSource;

    public static void openDBConnection() throws DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("Database connection has already been obtained.");
        }
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            throw new DBConnectionException("Failed to get a database connection.", e);
        }
        currentConnection.set(conn);
    }

    public static Connection getDBConnection() throws DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn == null) {
            try {
                conn = dataSource.getConnection();
                currentConnection.set(conn);
            } catch (SQLException e) {
                throw new DBConnectionException("Failed to get database connection.", e);
            }
        }
        return conn;
    }

    public static void beginDBTransaction() throws TransactionManagementException, DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn == null) {
            conn = getDBConnection();
        } else if (inTransaction(conn)) {
            throw new IllegalTransactionStateException("Transaction has already been started.");
        }

        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new TransactionManagementException("Error occurred while starting a database transaction.", e);
        }
    }

    public static void endDBTransaction() throws TransactionManagementException, DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("Database connection is not active.");
        }

        if (!inTransaction(conn)) {
            throw new IllegalTransactionStateException("Transaction has not been started.");
        }

        try {
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new TransactionManagementException("Error occurred while ending database transaction.", e);
        }
    }

    public static void commitDBTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("Database connection is not active.");
        }

        if (!inTransaction(conn)) {
            throw new IllegalTransactionStateException("Transaction has not been started.");
        }

        try {
            conn.commit();
        } catch (SQLException e) {
            log.error("Error occurred while committing the transaction", e);
        }
    }

    public static void rollbackDBTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("Database connection is not active.");
        }

        if (!inTransaction(conn)) {
            throw new IllegalTransactionStateException("Transaction has not been started.");
        }

        try {
            conn.rollback();
        } catch (SQLException e) {
            log.warn("Error occurred while roll-backing the transaction", e);
        }
    }

    public static void closeDBConnection() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("Database connection is not active.");
        }
        try {
            conn.close();
        } catch (SQLException e) {
            log.error("Error occurred while closing the connection", e);
        }
        currentConnection.remove();
    }

    private static boolean inTransaction(Connection conn) {
        boolean inTransaction = true;
        try {
            if (conn.getAutoCommit()) {
                inTransaction = false;
            }
        } catch (SQLException e) {
            throw new IllegalTransactionStateException("Failed to get transaction state.");
        }
        return inTransaction;
    }

    public static boolean isTransactionStarted() throws DBConnectionException {
        Connection connection = getDBConnection();
        return inTransaction(connection);
    }

    @Deprecated
    public static ThreadLocal<Connection> getCurrentConnection() {
        return currentConnection;
    }

    @Deprecated
    public static Connection openConnection() throws DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("A transaction is already active within the context of " +
                    "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling");
        }
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            currentTxState.set(TxState.CONNECTION_NOT_BORROWED);
            throw new DBConnectionException(e.getMessage(), e);
        }
        currentConnection.set(conn);
        currentTxState.set(TxState.CONNECTION_BORROWED);
        return conn;
    }

    @Deprecated
    public static Connection getConnection() throws DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new DBConnectionException("No connection is associated with the current thread. " +
                    "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        return conn;
    }

    @Deprecated
    public static void beginTransaction() throws TransactionManagementException, DBConnectionException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("A transaction is already active within the context of " +
                    "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling");
        }
        try {
            conn = dataSource.getConnection();
        } catch (SQLException e) {
            throw new DBConnectionException("Error occurred while retrieving a data source connection", e);
        }

        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            try {
                conn.close();
            } catch (SQLException e1) {
                log.warn("Error occurred while closing the borrowed connection. " +
                        "Transaction has ended pre-maturely", e1);
            }
            currentTxState.set(TxState.CONNECTION_CLOSED);
            throw new TransactionManagementException("Error occurred while setting auto-commit to false", e);
        }
        currentConnection.set(conn);
        currentTxState.set(TxState.CONNECTION_BORROWED);
    }

    @Deprecated
    public static void commitTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                    "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        try {
            conn.commit();
        } catch (SQLException e) {
            log.error("Error occurred while committing the transaction", e);
        }
    }

    @Deprecated
    public static void rollbackTransaction() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                    "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        try {
            conn.rollback();
        } catch (SQLException e) {
            log.warn("Error occurred while roll-backing the transaction", e);
        }
    }

    @Deprecated
    public static void closeConnection() {
        if (currentTxState != null) {
            TxState txState = currentTxState.get();

            if (TxState.CONNECTION_NOT_BORROWED == txState) {
                if (log.isDebugEnabled()) {
                    log.debug("No successful connection appears to have been borrowed to perform the underlying " +
                            "transaction even though the 'openConnection' method has been called. Therefore, " +
                            "'closeConnection' method is returning silently");
                }
                currentTxState.remove();
                return;
            }

            Connection conn = currentConnection.get();
            if (conn == null) {
                throw new IllegalTransactionStateException("No connection is associated with the current transaction. "
                        + "This might have ideally been caused by not properly initiating the transaction via "
                        + "'beginTransaction'/'openConnection' methods");
            }
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Error occurred while close the connection", e);
            }
            currentConnection.remove();
            currentTxState.remove();
        }
    }

    /**
     * Resolve the datasource from the datasource definition.
     *
     * @param dataSourceName Name of the datasource
     * @return DataSource resolved by the datasource name
     */
    public static DataSource resolveDataSource(String dataSourceName) {
        try {
            dataSource = InitialContext.doLookup(dataSourceName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
        }
        return dataSource;
    }


    public static String getDatabaseType() {
        try {
            return dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
        return null;
    }

    /**
     * To check whether particular database that is used for application management supports batch query execution.
     *
     * @return true if batch query is supported, otherwise false.
     */
    public static boolean isBatchQuerySupported() {
        try {
            return dataSource.getConnection().getMetaData().supportsBatchUpdates();
        } catch (SQLException e) {
            log.error("Error occurred while checking whether database supports batch updates", e);
        }
        return false;
    }


    public static void init(DataSource dtSource) {
        dataSource = dtSource;
    }

}
