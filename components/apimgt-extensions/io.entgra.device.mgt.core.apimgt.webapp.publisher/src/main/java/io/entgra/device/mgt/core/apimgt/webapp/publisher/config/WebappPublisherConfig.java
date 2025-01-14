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

package io.entgra.device.mgt.core.apimgt.webapp.publisher.config;

import io.entgra.device.mgt.core.apimgt.webapp.publisher.InvalidConfigurationStateException;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.WebappPublisherConfigurationFailedException;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.WebappPublisherUtil;
import org.w3c.dom.Document;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;

/**
 * This class represents the configuration that are needed
 * when publishing APIs to API Manager.
 */
@XmlRootElement(name = "WebappPublisherConfigs")
public class WebappPublisherConfig {

    private String host;
    private boolean isPublished;
    private boolean isEnabledUpdateApi;
    private Profiles profiles;
    private static boolean isInitialized = false;
    private Tenants tenants;

    private static WebappPublisherConfig config;

    private static final String WEBAPP_PUBLISHER_CONFIG_PATH =
            CarbonUtils.getEtcCarbonConfigDirPath() + File.separator + "webapp-publisher-config.xml";

    private WebappPublisherConfig() {
    }

    public static WebappPublisherConfig getInstance() {
        if (!isInitialized) {
            try {
                init();
            } catch (WebappPublisherConfigurationFailedException e) {
                throw new InvalidConfigurationStateException("Webapp Authenticator Configuration is not " +
                        "initialized properly");
            }
        }
        return config;
    }

    @XmlElement(name = "Host", required = true)
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @XmlElement(name = "PublishAPI", required = true)
    public boolean isPublished() {
        return isPublished;
    }

    @XmlElement(name = "Profiles", required = true)
    public Profiles getProfiles() {
        return profiles;
    }

    @XmlElement(name = "EnabledUpdateApi", required = true)
    public boolean isEnabledUpdateApi() {
        return isEnabledUpdateApi;
    }

    public void setEnabledUpdateApi(boolean isEnabledUpdateApi) {
        this.isEnabledUpdateApi = isEnabledUpdateApi;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public void setProfiles(Profiles profiles) {
        this.profiles = profiles;
    }

    @XmlElement(name = "Tenants", required = true)
    public Tenants getTenants() {
        return tenants;
    }

    public void setTenants(Tenants tenants) {
        this.tenants = tenants;
    }

    public synchronized static void init() throws WebappPublisherConfigurationFailedException {
        if (isInitialized) {
            return;
        }
        try {
            File emailSenderConfig = new File(WEBAPP_PUBLISHER_CONFIG_PATH);
            Document doc = WebappPublisherUtil.convertToDocument(emailSenderConfig);

            /* Un-marshaling Email Sender configuration */
            JAXBContext ctx = JAXBContext.newInstance(WebappPublisherConfig.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            //unmarshaller.setSchema(getSchema());
            config = (WebappPublisherConfig) unmarshaller.unmarshal(doc);
            isInitialized = true;
        } catch (JAXBException e) {
            throw new WebappPublisherConfigurationFailedException("Error occurred while un-marshalling Webapp " +
                    "Publisher Config", e);
        }
    }

}
