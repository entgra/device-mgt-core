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

package io.entgra.device.mgt.core.notification.mgt.core.config;

import io.entgra.device.mgt.core.notification.mgt.core.config.datasource.NotificationDeviceMgtConfiguration;
import io.entgra.device.mgt.core.notification.mgt.core.config.datasource.NotificationManagementRepository;
import io.entgra.device.mgt.core.notification.mgt.core.exception.NotificationConfigurationException;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class NotificationConfigurationManager {
    private static final Log log = LogFactory.getLog(NotificationConfigurationManager.class);

    private static final String CDM_CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.CDM_CONFIG_FILE_NAME;
    private NotificationManagementRepository notificationManagementRepository;

    NotificationConfigurationManager() {
    }

    public static NotificationConfigurationManager getInstance() {
        return NotificationConfigurationManagerHolder.INSTANCE;
    }

    private <T> T initConfig(String docPath, Class<T> configClass) throws JAXBException {
        File doc = new File(docPath);
        JAXBContext jaxbContext = JAXBContext.newInstance(configClass);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return configClass.cast(jaxbUnmarshaller.unmarshal(doc));
    }

    private void initDatasourceConfig() throws JAXBException {
        notificationManagementRepository = initConfig(CDM_CONFIG_PATH, NotificationDeviceMgtConfiguration.class)
                .getNotificationManagementRepository();
    }

    public NotificationManagementRepository getNotificationManagementRepository()
            throws NotificationConfigurationException {
        try {
            if (notificationManagementRepository == null) {
                initDatasourceConfig();
            }
            return notificationManagementRepository;
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing datasource configuration";
            throw new NotificationConfigurationException(msg, e);
        }
    }

    private static class NotificationConfigurationManagerHolder {
        public static final NotificationConfigurationManager INSTANCE = new NotificationConfigurationManager();
    }
}

