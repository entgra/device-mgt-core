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
package io.entgra.device.mgt.core.apimgt.application.extension.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ApiApplicationValidatorConfigManager {

    private static final Log log = LogFactory.getLog(ApiApplicationValidatorConfigManager.class);
    
    private static ApiApplicationValidatorConfigManager manager;
    private ApiApplicationValidatorConfig currentConfig;
    private boolean isInitialized = false;

    private static final String CONFIG_FILE_NAME = "api-application-validator-config.xml";

    private ApiApplicationValidatorConfigManager() {
    }

    public static ApiApplicationValidatorConfigManager getInstance() {
        if (manager == null) {
            synchronized (ApiApplicationValidatorConfigManager.class) {
                if (manager == null) {
                    manager = new ApiApplicationValidatorConfigManager();
                }
            }
        }
        return manager;
    }

    public synchronized void initConfig() {
        try {
            String configFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + CONFIG_FILE_NAME;
            File configFile = new File(configFilePath);
            
            if (!configFile.exists()) {
                if (log.isDebugEnabled()) {
                    log.debug("API Application Validator configuration file not found at: " + configFilePath);
                }
                isInitialized = true;
                return;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(configFile);

            JAXBContext jaxbContext = JAXBContext.newInstance(ApiApplicationValidatorConfig.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            
            this.currentConfig = (ApiApplicationValidatorConfig) unmarshaller.unmarshal(doc);
            
            if (log.isDebugEnabled()) {
                log.debug("Successfully initialized API Application Validator configuration.");
            }
        } catch (NoClassDefFoundError e) {
            log.error("Unable to resolve configuration dependencies. This might occur during early server startup.", e);
        } catch (Exception e) {
            log.error("Error occurred while initializing API Application Validator configuration", e);
        } finally {
            isInitialized = true;
        }
    }

    public ApiApplicationValidatorConfig getApiApplicationValidatorConfig() {
        if (!isInitialized) {
            initConfig();
        }
        return currentConfig;
    }
}
