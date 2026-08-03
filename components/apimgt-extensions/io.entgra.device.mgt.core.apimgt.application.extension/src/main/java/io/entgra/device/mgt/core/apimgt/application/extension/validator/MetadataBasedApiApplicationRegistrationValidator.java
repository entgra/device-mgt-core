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
package io.entgra.device.mgt.core.apimgt.application.extension.validator;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;
import io.entgra.device.mgt.core.apimgt.application.extension.internal.APIApplicationManagerExtensionDataHolder;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validates OAuth application registration by checking:
 * <ol>
 *   <li>Whether the target tenant is in a metadata-backed allow-list, and</li>
 *   <li>Whether the required APIs are published on the gateway for that tenant.</li>
 * </ol>
 * Instantiate with the metadata key that holds the enabled tenant list and the
 * API name search query to match against the dev-portal.
 */
public class MetadataBasedApiApplicationRegistrationValidator implements ApiApplicationRegistrationValidator {

    private static final Log log = LogFactory.getLog(MetadataBasedApiApplicationRegistrationValidator.class);
    private static final Gson gson = new Gson();

    private final String enabledTenantsMetadataKey;
    private final String apiSearchQuery;

    public MetadataBasedApiApplicationRegistrationValidator(String enabledTenantsMetadataKey,
                                                            String apiSearchQuery) {
        this.enabledTenantsMetadataKey = enabledTenantsMetadataKey;
        this.apiSearchQuery = apiSearchQuery;
    }

    @Override
    public void validate(String tenantDomain) throws APIManagerException {
        List<String> enabledTenants = getTenantDomainsFromMetadata(enabledTenantsMetadataKey);
        if (!enabledTenants.contains(tenantDomain)) {
            return;
        }

        if (!ApiAvailabilityUtil.areApisPublished(apiSearchQuery, tenantDomain)) {
            String msg = "Required APIs matching query [" + apiSearchQuery + "] are not published for tenant: "
                    + tenantDomain + ". Application registration aborted.";
            log.error(msg);
            throw new APIManagerException(msg);
        }
    }

    private static List<String> getTenantDomainsFromMetadata(String metadataKey) throws APIManagerException {
        MetadataManagementService metadataManagementService =
                APIApplicationManagerExtensionDataHolder.getInstance().getMetadataManagementService();
        Metadata metaData;
        try {
            if (Objects.equals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                metaData = metadataManagementService.retrieveMetadata(metadataKey);
            } else {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext()
                            .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                    metaData = metadataManagementService.retrieveMetadata(metadataKey);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        } catch (MetadataManagementException e) {
            String msg = "Failed to load tenant domains from metadata registry for key: " + metadataKey;
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }

        if (metaData == null) {
            String msg = "Null retrieved for metadata entry with key: " + metadataKey;
            log.error(msg);
            throw new APIManagerException(msg);
        }

        JsonArray tenants = gson.fromJson(metaData.getMetaValue(), JsonArray.class);
        List<String> tenantDomains = new ArrayList<>();
        for (JsonElement tenant : tenants) {
            tenantDomains.add(tenant.getAsString());
        }
        return tenantDomains;
    }
}
