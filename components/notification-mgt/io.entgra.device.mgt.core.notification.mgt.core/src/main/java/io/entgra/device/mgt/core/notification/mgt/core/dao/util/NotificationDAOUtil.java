/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.notification.mgt.core.dao.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

/**
 * This class includes the utility methods required by NotificationMgmt functionalities.
 */
public class NotificationDAOUtil {

	private static final Log log = LogFactory.getLog(NotificationDAOUtil.class);

	public static void cleanupResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing result set", e);
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing prepared statement", e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing database connection", e);
			}
		}
	}

	public static void cleanupResources(PreparedStatement stmt, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing result set", e);
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing prepared statement", e);
			}
		}
	}

	public static void cleanupResources(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.warn("Error occurred while closing the statement", e);
			}
		}
	}

	public static DataSource lookupDataSource(String dataSourceName,
	                                          final Hashtable<Object, Object> jndiProperties) {
		try {
			if (jndiProperties == null || jndiProperties.isEmpty()) {
				return (DataSource) InitialContext.doLookup(dataSourceName);
			}
			final InitialContext context = new InitialContext(jndiProperties);
			return (DataSource) context.lookup(dataSourceName);
		} catch (Exception e) {
			throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
		}
	}
}
