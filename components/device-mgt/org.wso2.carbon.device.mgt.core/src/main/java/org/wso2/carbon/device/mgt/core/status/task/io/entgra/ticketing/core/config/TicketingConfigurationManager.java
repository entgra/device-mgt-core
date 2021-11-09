/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.TicketingHandlerConstants;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.config.TicketingConfiguration;
import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;


public class TicketingConfigurationManager {
    private static final Log log = LogFactory.getLog(TicketingConfigurationManager.class);
    private static TicketingConfigurationManager ticketingConfigurationManager;
    private TicketingConfiguration ticketingConfiguration;
    private static final String CarbonUtilsFile = CarbonUtils.getCarbonConfigDirPath() + File.separator;
    private static final String TICKETING_CONFIG_PATH = CarbonUtilsFile + TicketingHandlerConstants.TICKETING_CONFIG_XML_NAME;

    /**
     * Retrieve an instance of {@link TicketingConfigurationManager}
     * @return an instance of {@link TicketingConfigurationManager}
     */
    public static TicketingConfigurationManager getInstance() {
        if (ticketingConfigurationManager == null) {
            synchronized (TicketingConfigurationManager.class) {
                if (ticketingConfigurationManager == null) {
                    ticketingConfigurationManager = new TicketingConfigurationManager();
                }
            }
        }
        return ticketingConfigurationManager;
    }

    /**
     * Initialize the Ticketing Configuration through the provided configuration location
     * @param configLocation has the path of the Ticketing configuration file
     * @throws DeviceManagementException throws when there are any errors during the initialization of
     * Ticketing configuration
     */
    public synchronized void initConfig(String configLocation) throws DeviceManagementException {
        try {
            File ticketingConfig = new File(configLocation);
            Document doc = DeviceManagerUtil.convertToDocument(ticketingConfig);

            /* Un-marshaling Ticketing configuration */
            JAXBContext ticketingContext = JAXBContext.newInstance(TicketingConfiguration.class);
            Unmarshaller unmarshaller = ticketingContext.createUnmarshaller();
            this.ticketingConfiguration = (TicketingConfiguration) unmarshaller.unmarshal(doc);
        } catch (JAXBException e) {
            String msg = "Error occurred while initializing Ticketing config '" + configLocation + "'";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    /**
     * Initialize the Ticketing Configuration through the ticketing-config.xml file in the TICKETING_CONFIG_PATH
     * @throws DeviceManagementException throws when there are any errors during the initialization of
     * Ticketing configuration
     */
    public void initConfig() throws DeviceManagementException {
        this.initConfig(TICKETING_CONFIG_PATH);
    }

    /**
     * Retrieves the initialized {@link TicketingConfiguration}
     * @return the initialized {@link TicketingConfiguration}
     */
    public TicketingConfiguration getTicketingConfig() {
        try{
            initConfig();
        }catch (Exception e){
            log.error("TicketingConfiguration:", e);
        }
        return ticketingConfiguration;
    }
}
