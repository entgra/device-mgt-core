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

package io.entgra.device.mgt.core.device.mgt.core.metadata.mgt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.SSOConfigurationManagementService;
import io.entgra.device.mgt.core.device.mgt.core.config.ui.SSOConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.config.ui.UIConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.config.ui.UIConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.util.MetadataConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SSOConfigurationManagementServiceImpl implements SSOConfigurationManagementService {

    private static final Log log = LogFactory.getLog(SSOConfigurationManagementServiceImpl.class);
    private final MetadataDAO metadataDAO;

    public SSOConfigurationManagementServiceImpl() {
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
    }

    @Override
    public void addDefaultSSOConfigurationIfNotExist(int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (!metadataDAO.isExist(tenantId, MetadataConstants.SSO_CONFIGURATION_META_KEY)) {
                metadataDAO.addMetadata(tenantId, constructSSOConfigurationMetadata(getDefaultSSOConfiguration()));
            }
            MetadataManagementDAOFactory.commitTransaction();
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while inserting SSO Configuration metadata entry.";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source.";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public Map<String, String> getSSOConfiguration(int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.openConnection();
            Metadata metadata = metadataDAO.getMetadata(tenantId, MetadataConstants.SSO_CONFIGURATION_META_KEY);
            if (metadata == null) {
                String msg = "Couldn't find the meta data value for meta key: "
                        + MetadataConstants.SSO_CONFIGURATION_META_KEY + " and tenant Id: " + tenantId;
                log.error(msg);
                throw new MetadataKeyNotFoundException(msg);
            }
            String metaValue = metadata.getMetaValue();
            JsonObject jsonObject = JsonParser.parseString(metaValue).getAsJsonObject();
            Map<String, String> ssoConfigMap = new HashMap<>();
            ssoConfigMap.put("issuer", jsonObject.get("issuer").getAsString());
            ssoConfigMap.put("ssoEnabled", jsonObject.get("ssoEnabled").getAsString());
            return ssoConfigMap;
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving SSO Configuration metadata for tenant: " + tenantId;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    /**
     * Constructs a {@link Metadata} object containing the SSO configuration
     * metadata for a given {@link SSOConfiguration}.
     *
     * @param ssoConfiguration The SSO configuration to be serialized into metadata.
     * @return A {@link Metadata} object representing the given SSO configuration.
     */
    private Metadata constructSSOConfigurationMetadata(SSOConfiguration ssoConfiguration) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("issuer", ssoConfiguration.getIssuer());
        jsonObject.addProperty("ssoEnabled", String.valueOf(ssoConfiguration.isSsoEnabled()));
        Metadata metadata = new Metadata();
        metadata.setMetaKey(MetadataConstants.SSO_CONFIGURATION_META_KEY);
        metadata.setMetaValue(jsonObject.toString());
        return metadata;
    }

    /**
     * Retrieves the default SSO configuration from the UI configuration.
     * If not available, a default configuration is returned with safe fallback values.
     *
     * @return A non-null {@link SSOConfiguration} instance.
     */
    public SSOConfiguration getDefaultSSOConfiguration() {
        UIConfiguration uiConfiguration = UIConfigurationManager.getInstance().getUIConfig();
        if (uiConfiguration != null) {
            return uiConfiguration.getSsoConfiguration();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("UIConfiguration is null while retrieving SSOConfiguration.");
            }
            return null;
        }
    }
}
