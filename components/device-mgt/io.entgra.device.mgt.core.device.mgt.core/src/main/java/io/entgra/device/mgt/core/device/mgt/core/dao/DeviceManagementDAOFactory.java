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

package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.IllegalTransactionStateException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.UnsupportedDatabaseEngineException;
import io.entgra.device.mgt.core.device.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.device.mgt.core.config.datasource.JNDILookupDefinition;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.*;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.device.GenericDeviceDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.device.OracleDeviceDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.device.PostgreSQLDeviceDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.device.SQLServerDeviceDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.enrolment.GenericEnrollmentDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.enrolment.SQLServerEnrollmentDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.impl.tracker.TrackerDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.dao.util.DeviceManagementDAOUtil;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.dao.DeviceDetailsDAO;
import io.entgra.device.mgt.core.device.mgt.core.device.details.mgt.dao.impl.DeviceDetailsDAOImpl;
import io.entgra.device.mgt.core.device.mgt.core.privacy.dao.PrivacyComplianceDAO;
import io.entgra.device.mgt.core.device.mgt.core.privacy.dao.impl.PrivacyComplianceDAOImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

/**
 * This class intends to act as the primary entity that hides all DAO instantiation related complexities and logic so
 * that the business objection handling layer doesn't need to be aware of the same providing seamless plug-ability of
 * different data sources, connection acquisition mechanisms as well as different forms of DAO implementations to the
 * high-level implementations that require device management related metadata persistence.
 * <p/>
 * In addition, this also provides means to handle transactions across multiple device management related DAO objects.
 * Any high-level business logic that requires transaction handling to be done via utility methods provided in
 * DeviceManagementDAOFactory should adhere the following guidelines to avoid any unexpected behaviour that can cause
 * as a result of improper use of the aforementioned utility method.
 * <p/>
 * Any transaction that commits data into the underlying data persistence mechanism MUST follow the sequence of
 * operations mentioned below.
 * <p/>
 * <pre>
 * {@code
 * try {
 *      DeviceManagementDAOFactory.beginTransaction();
 *      .....
 *      DeviceManagementDAOFactory.commitTransaction();
 *      return success;
 * } catch (Exception e) {
 *      DeviceManagementDAOFactory.rollbackTransaction();
 *      throw new DeviceManagementException("Error occurred while ...", e);
 * } finally {
 *      DeviceManagementDAOFactory.closeConnection();
 * }
 * }
 * </pre>
 * <p/>
 * Any transaction that retrieves data from the underlying data persistence mechanism MUST follow the sequence of
 * operations mentioned below.
 * <p/>
 * <pre>
 * {@code
 * try {
 *      DeviceManagementDAOFactory.openConnection();
 *      .....
 * } catch (Exception e) {
 *      throw new DeviceManagementException("Error occurred while ..., e);
 * } finally {
 *      DeviceManagementDAOFactory.closeConnection();
 * }
 * }
 * </pre>
 */
public class DeviceManagementDAOFactory {

    private static DataSource dataSource;
    private static String databaseEngine;
    private static final Log log = LogFactory.getLog(DeviceManagementDAOFactory.class);
    private static ThreadLocal<Connection> currentConnection = new ThreadLocal<>();

    public static DeviceDAO getDeviceDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                    return new OracleDeviceDAOImpl();
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                    return new SQLServerDeviceDAOImpl();
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                    return new PostgreSQLDeviceDAOImpl();
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_H2:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericDeviceDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static DeviceTypeDAO getDeviceTypeDAO() {
        return new DeviceTypeDAOImpl();
    }

    public static DeviceTypeEventDAO getDeviceTypeEventDAO() {
        return new DeviceTypeEventDAOImpl();
    }

    public static EnrollmentDAO getEnrollmentDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                    return new SQLServerEnrollmentDAOImpl();
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_H2:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new GenericEnrollmentDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static TenantDAO getTenantDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_H2:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new TenantDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static TrackerDAO getTrackerDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_H2:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new TrackerDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static TagDAO getTagDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_H2:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new TagDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static DeviceStatusDAO getDeviceStatusDAO() {
        return new DeviceStatusDAOImpl();
    }
    public static ApplicationDAO getApplicationDAO() {
        if (databaseEngine != null) {
            switch (databaseEngine) {
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_POSTGRESQL:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_ORACLE:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MSSQL:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_H2:
                case DeviceManagementConstants.DataBaseTypes.DB_TYPE_MYSQL:
                    return new ApplicationDAOImpl();
                default:
                    throw new UnsupportedDatabaseEngineException("Unsupported database engine : " + databaseEngine);
            }
        }
        throw new IllegalStateException("Database engine has not initialized properly.");
    }

    public static DeviceDetailsDAO getDeviceDetailsDAO() {
        return new DeviceDetailsDAOImpl();
    }

    public static PrivacyComplianceDAO getPrivacyComplianceDAO() {
        return new PrivacyComplianceDAOImpl();
    }

    public static void init(DataSourceConfig config) {
        dataSource = resolveDataSource(config);
        try {
            databaseEngine = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static void init(DataSource dtSource) {
        dataSource = dtSource;
        try {
            databaseEngine = dataSource.getConnection().getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            log.error("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static void beginTransaction() throws TransactionManagementException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("A transaction is already active within the context of " +
                    "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling");
        }
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            currentConnection.set(conn);
        } catch (SQLException e) {
            throw new TransactionManagementException("Error occurred while retrieving config.datasource connection", e);
        }
    }

    public static void openConnection() throws SQLException {
        Connection conn = currentConnection.get();
        if (conn != null) {
            throw new IllegalTransactionStateException("A transaction is already active within the context of " +
                    "this particular thread. Therefore, calling 'beginTransaction/openConnection' while another " +
                    "transaction is already active is a sign of improper transaction handling");
        }
        conn = dataSource.getConnection();
        currentConnection.set(conn);
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                    "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        return conn;
    }

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

    public static void closeConnection() {
        Connection conn = currentConnection.get();
        if (conn == null) {
            throw new IllegalTransactionStateException("No connection is associated with the current transaction. " +
                    "This might have ideally been caused by not properly initiating the transaction via " +
                    "'beginTransaction'/'openConnection' methods");
        }
        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("Error occurred while close the connection");
        }
        currentConnection.remove();
    }


    /**
     * Resolve data source from the data source definition
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    private static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException(
                    "Device Management Repository data source configuration " + "is null and " +
                            "thus, is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI " +
                        "Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = DeviceManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }
}
