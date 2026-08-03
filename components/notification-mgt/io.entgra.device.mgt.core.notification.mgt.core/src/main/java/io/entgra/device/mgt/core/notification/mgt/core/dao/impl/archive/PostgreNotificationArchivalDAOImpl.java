/*
 *  Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.notification.mgt.core.dao.impl.archive;

import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationArchivalDAOException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.AbstractNotificationArchivalDAOImpl;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.archive.NotificationArchivalSourceDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PostgreNotificationArchivalDAOImpl extends AbstractNotificationArchivalDAOImpl {
    private static final Log log = LogFactory.getLog(PostgreNotificationArchivalDAOImpl.class);

    private static final String SOURCE_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getSourceDB();

    private static final String DESTINATION_DB =
            DeviceConfigurationManager.getInstance().getDeviceManagementConfig().getArchivalConfiguration()
                    .getArchivalTaskConfiguration().getDbConfig().getDestinationDB();

    @Override
    public int deleteOldNotifications(Timestamp cutoff, int tenantId)
            throws NotificationArchivalDAOException {
        String deleteSQL =
                "DELETE FROM " + SOURCE_DB + ".DM_NOTIFICATION "
                        + "WHERE CREATED_TIMESTAMP < ? " +
                        "AND TENANT_ID = ?";
        try {
            Connection src = NotificationArchivalSourceDAOFactory.getConnection();
            try (PreparedStatement stmt = src.prepareStatement(deleteSQL)) {
                stmt.setTimestamp(1, cutoff);
                stmt.setInt(2, tenantId);
                return stmt.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Failed to delete old notifications for tenant " + tenantId;
            log.error(msg, e);
            throw new NotificationArchivalDAOException(msg, e);
        }
    }
}
