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

package io.entgra.device.mgt.core.notification.mgt.core.impl;

import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.service.NotificationManagementService;
import io.entgra.device.mgt.core.notification.mgt.core.dao.NotificationManagementDAO;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.exception.NotificationManagementDAOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

public class NotificationManagementServiceImpl implements NotificationManagementService {
    private static final Log log = LogFactory.getLog(NotificationManagementServiceImpl.class);
    private final NotificationManagementDAO notificationDAO;

    public NotificationManagementServiceImpl() {
        notificationDAO = NotificationManagementDAOFactory.getNotificationManagementDAO();
    }

    @Override
    public List<Notification> getLatestNotifications(int offset, int limit) throws NotificationManagementException {
        try {
            NotificationManagementDAOFactory.openConnection();
            return notificationDAO.getLatestNotifications(offset, limit);
        } catch (NotificationManagementDAOException e) {
            String msg = "Error occurred while initiating transaction";
            log.error(msg, e);
            throw new NotificationManagementException(msg, e);
        } finally {
            NotificationManagementDAOFactory.closeConnection();
        }
    }
}
