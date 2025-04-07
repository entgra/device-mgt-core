package io.entgra.device.mgt.core.notification.mgt.common.service;

import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationConfigurationServiceException;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;
import java.util.List;
public interface NotificationConfigService {
    /**
     * Retrieve the  notification configurations for a tenant.
     *
     * @return {@link List < Notification >}
     * @throws NotificationConfigurationServiceException Throws when error occurred while retrieving notifications.
     */
        void addNotificationConfigContext(NotificationConfigurationList configurations) throws NotificationConfigurationServiceException;

        void deleteNotificationConfigContext(int configID) throws NotificationConfigurationServiceException;

        void updateNotificationConfigContext(NotificationConfig updatedConfig) throws NotificationConfigurationServiceException;
    /**
     * Delete Notification Configurations for a tenant.
     *
     * @return {@link Object < Notification Configuration>}
     * @throws NotificationConfigurationServiceException Throws when error occurred while retrieving notifications.
     */
        void deleteNotificationConfigurations() throws NotificationConfigurationServiceException;

    /**
     * Retrieve the  list of notification configurations for a tenant.
     *
     * @return {@link List < Notification Configurations>}
     * @throws NotificationConfigurationServiceException Throws when error occurred while retrieving notifications.
     */
        NotificationConfigurationList getNotificationConfigurations() throws NotificationConfigurationServiceException;
    /**
     * Retrieve a notification Configuration By Config ID for a tenant.
     *
     * @return {@link Object < Notification Configuration>}
     * @throws NotificationConfigurationServiceException Throws when error occurred while retrieving notifications.
     */
        NotificationConfig getNotificationConfigByID(int configID) throws NotificationConfigurationServiceException;


}
