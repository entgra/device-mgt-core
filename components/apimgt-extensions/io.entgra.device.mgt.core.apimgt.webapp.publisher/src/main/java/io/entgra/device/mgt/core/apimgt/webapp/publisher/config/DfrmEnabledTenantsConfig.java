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
 *
 */

package io.entgra.device.mgt.core.apimgt.webapp.publisher.config;

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

@XmlRootElement(name = "DfrmEnabledTenantsConfig")
public class DfrmEnabledTenantsConfig {
    private Tenants tenants;
    private static DfrmEnabledTenantsConfig config;
    private static boolean isInitialized = false;
    private static final String DFRM_ENABLED_TENANTS_CONFIG_PATH =
            CarbonUtils.getCarbonConfigDirPath() + File.separator + "dfrm-tenants-config.xml";
    private DfrmEnabledTenantsConfig() {}

    public static DfrmEnabledTenantsConfig getInstance() {
        if (!isInitialized) {
            try {
                init();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return config;
    }

    @XmlElement(name = "Tenants", required = true)
    public Tenants getTenants() {
        return tenants;
    }

    public void setTenants(Tenants tenants) {this.tenants = tenants;}

    public synchronized static void init() throws WebappPublisherConfigurationFailedException {
        if (isInitialized) {
            return;
        }
        try {
            File dfrmEnabledTenantConfig = new File(DFRM_ENABLED_TENANTS_CONFIG_PATH);
            Document doc = WebappPublisherUtil.convertToDocument(dfrmEnabledTenantConfig);

            JAXBContext ctx = JAXBContext.newInstance(DfrmEnabledTenantsConfig.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            config = (DfrmEnabledTenantsConfig) unmarshaller.unmarshal(doc);
            isInitialized = true;
        } catch (JAXBException e) {
            throw new WebappPublisherConfigurationFailedException("Error occurred while un-marshalling DFRM Enabled " +
                    "Tenant Config", e);
        }
    }
}
