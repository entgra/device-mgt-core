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

package io.entgra.device.mgt.core.device.mgt.core.metadata.mgt;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import io.entgra.device.mgt.core.device.mgt.common.PaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.PaginationResult;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyAlreadyExistsException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataKeyNotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.core.DeviceManagementConstants;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import com.google.gson.JsonParser;

/**
 * This class implements the MetadataManagementService.
 */
public class MetadataManagementServiceImpl implements MetadataManagementService {

    private static final Log log = LogFactory.getLog(MetadataManagementServiceImpl.class);

    private final MetadataDAO metadataDAO;

    public MetadataManagementServiceImpl() {
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
    }

    @Override
    public Metadata createMetadata(Metadata metadata)
            throws MetadataManagementException, MetadataKeyAlreadyExistsException {
        if (log.isDebugEnabled()) {
            log.debug("Creating Metadata : [" + metadata.toString() + "]");
        }

        if (isPerTenantMetaKey(metadata.getMetaKey())) {
            String tenantDomain = extractTenantDomain(metadata.getMetaKey());
            return createMetadataForTenant(metadata, tenantDomain);
        }

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (metadataDAO.isExist(tenantId, metadata.getMetaKey())) {
                String msg = "Specified metaKey already exists. {metaKey:" + metadata.getMetaKey() + "}";
                log.error(msg);
                throw new MetadataKeyAlreadyExistsException(msg);
            }
            metadataDAO.addMetadata(tenantId, metadata);
            MetadataManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("Metadata entry created successfully. " + metadata.toString());
            }
            return metadata;
        } catch (MetadataKeyAlreadyExistsException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Meta Key already exists.";
            throw new MetadataManagementException(msg, e);
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while creating the metadata entry. " + metadata.toString();
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    /**
     * Creates a metadata entry under the specified tenant's context rather than the currently
     * active (carbon.super) tenant. This is used when configuring per-tenant settings such as
     * per-device cost from the super tenant's admin console, ensuring the metadata is stored
     * against the correct target tenant's ID.
     *
     * @param metadata     the {@link Metadata} object to be created, containing the metaKey and metaValue
     * @param tenantDomain the domain of the target tenant under which the metadata should be stored
     * @return the created {@link Metadata} object
     * @throws MetadataManagementException       if an error occurs while resolving the tenant ID,
     *                                           opening a data source connection, or persisting the entry
     * @throws MetadataKeyAlreadyExistsException if a metadata entry with the same metaKey already
     *                                           exists under the target tenant
     */
    private Metadata createMetadataForTenant(Metadata metadata, String tenantDomain)
            throws MetadataManagementException, MetadataKeyAlreadyExistsException {
        int targetTenantId;
        try {
            targetTenantId = DeviceManagerUtil.getTenantId(tenantDomain);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while resolving tenant ID for domain: " + tenantDomain;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(targetTenantId, true);

            MetadataManagementDAOFactory.beginTransaction();
            if (metadataDAO.isExist(targetTenantId, metadata.getMetaKey())) {
                String msg = "Specified metaKey already exists for tenant: " + tenantDomain +
                        " {metaKey:" + metadata.getMetaKey() + "}";
                log.error(msg);
                throw new MetadataKeyAlreadyExistsException(msg);
            }
            metadataDAO.addMetadata(targetTenantId, metadata);
            MetadataManagementDAOFactory.commitTransaction();
            if (log.isDebugEnabled()) {
                log.debug("Per-tenant metadata created successfully for tenant: " + tenantDomain +
                        " " + metadata.toString());
            }
            return metadata;
        } catch (MetadataKeyAlreadyExistsException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Meta Key already exists.";
            throw new MetadataManagementException(msg, e);
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while creating per-tenant metadata entry. " + metadata.toString();
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public Metadata retrieveMetadata(String metaKey) throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving Metadata for metaKey:" + metaKey);
        }
        try {
            MetadataManagementDAOFactory.openConnection();
            int tenantId;
            if (metaKey.equals("EVALUATE_TENANTS")) {
                // for getting per device cost and evaluate tenant list to provide the billing feature and live chat feature
                 tenantId = MultitenantConstants.SUPER_TENANT_ID;
            } else {
                tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
            }
            return metadataDAO.getMetadata(tenantId, metaKey);
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving the metadata entry for metaKey:" + metaKey;
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

    @Override
    public List<Metadata> retrieveAllMetadata() throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving all Metadata entries");
        }
        try {
            MetadataManagementDAOFactory.openConnection();
            PaginationRequest request = new PaginationRequest(0, -1);
            return metadataDAO.getAllMetadata(request,
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true));
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving all metadata entries";
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

    @Override
    public PaginationResult retrieveAllMetadata(PaginationRequest request) throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving Metadata entries for given PaginationRequest [rowCount:" +
                    request.getRowCount() + ", startIndex:" + request.getStartIndex() + "]");
        }
        PaginationResult paginationResult = new PaginationResult();
        request = DeviceManagerUtil.validateMetadataListPageSize(request);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            MetadataManagementDAOFactory.openConnection();
            List<Metadata> metadata = metadataDAO.getAllMetadata(request, tenantId);
            int count = metadataDAO.getMetadataCount(tenantId);
            paginationResult.setData(metadata);
            paginationResult.setRecordsFiltered(count);
            paginationResult.setRecordsTotal(count);
            return paginationResult;
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving metadata entries for given PaginationRequest [rowCount:" +
                    request.getRowCount() + ", startIndex:" + request.getStartIndex() + "]";
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

    @Override
    public Metadata updateMetadata(Metadata metadata) throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Updating Metadata : [" + metadata.toString() + "]");
        }

        if (isPerTenantMetaKey(metadata.getMetaKey())) {
            String tenantDomain = extractTenantDomain(metadata.getMetaKey());
            return updateMetadataForTenant(metadata, tenantDomain);
        }

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (metadataDAO.isExist(tenantId, metadata.getMetaKey())) {
                metadataDAO.updateMetadata(tenantId, metadata);
            } else {
                metadataDAO.addMetadata(tenantId, metadata);
            }
            MetadataManagementDAOFactory.commitTransaction();
            return metadata;
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating metadata entry. " + metadata.toString();
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    /**
     * Updates or inserts a metadata entry under the specified tenant's context rather than the
     * currently active (carbon.super) tenant. This is used when configuring per-tenant settings
     * such as per-device cost from the super tenant's admin console, ensuring the metadata is
     * stored against the correct target tenant's ID.
     *
     * <p>If a metadata entry with the given metaKey already exists under the target tenant,
     * it will be updated. Otherwise, a new entry will be inserted.</p>
     *
     * @param metadata     the {@link Metadata} object to be updated or inserted,
     *                     containing the metaKey and metaValue
     * @param tenantDomain the domain of the target tenant under which the metadata should be stored
     * @return the updated or inserted {@link Metadata} object
     * @throws MetadataManagementException if an error occurs while resolving the tenant ID,
     *                                     opening a data source connection, or persisting the entry
     */
    private Metadata updateMetadataForTenant(Metadata metadata, String tenantDomain)
            throws MetadataManagementException {
        int targetTenantId;
        try {
            targetTenantId = DeviceManagerUtil.getTenantId(tenantDomain);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while resolving tenant ID for domain: " + tenantDomain;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(targetTenantId, true);

            MetadataManagementDAOFactory.beginTransaction();
            if (metadataDAO.isExist(targetTenantId, metadata.getMetaKey())) {
                metadataDAO.updateMetadata(targetTenantId, metadata);
                if (log.isDebugEnabled()) {
                    log.debug("Per-tenant metadata updated for tenant: " + tenantDomain);
                }
            } else {
                metadataDAO.addMetadata(targetTenantId, metadata);
                if (log.isDebugEnabled()) {
                    log.debug("Per-tenant metadata created for tenant: " + tenantDomain);
                }
            }
            MetadataManagementDAOFactory.commitTransaction();
            return metadata;
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating per-tenant metadata entry. " + metadata.toString();
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean deleteMetadata(String key) throws MetadataManagementException, MetadataKeyNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting metadata entry. {metaKey:" + key + "}");
        }

        if (isPerTenantMetaKey(key)) {
            String tenantDomain = extractTenantDomain(key);
            return deleteMetadataForTenant(key, tenantDomain);
        }

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            MetadataManagementDAOFactory.beginTransaction();
            boolean status = metadataDAO.deleteMetadata(tenantId, key);
            if (status) {
                MetadataManagementDAOFactory.commitTransaction();
                if (log.isDebugEnabled()) {
                    log.debug("Metadata entry deleted successfully. {metaKey:" + key + "}");
                }
                return true;
            } else {
                MetadataManagementDAOFactory.rollbackTransaction();
                String msg = "Specified Metadata entry not found. {metaKey:" + key + "}";
                log.error(msg);
                throw new MetadataKeyNotFoundException(msg);
            }
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting metadata entry. {metaKey:" + key + "}";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    /**
     * Deletes a metadata entry under the specified tenant's context rather than the currently
     * active (carbon.super) tenant. This is used when removing per-tenant configuration keys
     * such as per-device cost, DFRM enablement, and permission config, which are managed from
     * the super tenant's admin console but stored under the respective target tenant's context.
     *
     * @param key          the metadata key to be deleted
     * @param tenantDomain the domain of the target tenant under which the metadata is stored
     * @return {@code true} if the metadata entry was successfully deleted
     * @throws MetadataManagementException  if an error occurs while resolving the tenant ID,
     *                                      opening a data source connection, or deleting the entry
     * @throws MetadataKeyNotFoundException if no metadata entry exists for the given key
     *                                      under the target tenant
     */
    private boolean deleteMetadataForTenant(String key, String tenantDomain)
            throws MetadataManagementException, MetadataKeyNotFoundException {
        int targetTenantId;
        try {
            targetTenantId = DeviceManagerUtil.getTenantId(tenantDomain);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while resolving tenant ID for domain: " + tenantDomain;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(targetTenantId, true);

            MetadataManagementDAOFactory.beginTransaction();
            boolean status = metadataDAO.deleteMetadata(targetTenantId, key);
            if (status) {
                MetadataManagementDAOFactory.commitTransaction();
                if (log.isDebugEnabled()) {
                    log.debug("Per-tenant metadata deleted successfully for tenant: " + tenantDomain +
                            " {metaKey:" + key + "}");
                }
                return true;
            } else {
                MetadataManagementDAOFactory.rollbackTransaction();
                String msg = "Specified per-tenant metadata entry not found for tenant: " + tenantDomain +
                        " {metaKey:" + key + "}";
                log.error(msg);
                throw new MetadataKeyNotFoundException(msg);
            }
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while deleting per-tenant metadata entry for tenant: " +
                    tenantDomain + " {metaKey:" + key + "}";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean clearMetadataValue(String metaKey)
            throws MetadataManagementException, MetadataKeyNotFoundException {
        if (log.isDebugEnabled()) {
            log.debug("Clearing metadata value for metaKey: " + metaKey);
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (!metadataDAO.isExist(tenantId, metaKey)) {
                String msg = "Specified Metadata entry not found for clearing. {metaKey:" + metaKey + "}";
                log.error(msg);
                throw new MetadataKeyNotFoundException(msg);
            }
            boolean status = metadataDAO.clearMetadataValue(tenantId, metaKey);
            MetadataManagementDAOFactory.commitTransaction();
            if (status && log.isDebugEnabled()) {
                log.debug("Successfully cleared metadata value. {metaKey:" + metaKey + "}");
            }
            return status;
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while clearing metadata value. {metaKey:" + metaKey + "}";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public PaginationResult searchMetadata(PaginationRequest request) throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving filtered list of Metadata entries for given PaginationRequest [rowCount:" +
                    request.getRowCount() + ", startIndex:" + request.getStartIndex() + ", filter:" + request.getFilter() + " ]");
        }
        PaginationResult paginationResult = new PaginationResult();
        request = DeviceManagerUtil.validateMetadataListPageSize(request);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        try {
            MetadataManagementDAOFactory.openConnection();
            List<Metadata> metadata = metadataDAO.getFilteredMetadata(request, tenantId);
            int count = metadataDAO.getMetadataCountForFilteredResult(request, tenantId);

            if (request.getCustomProperty() != null && !request.getCustomProperty().isEmpty()) {
                //If custom property is present, navigate the request into advance filtering
                if (log.isDebugEnabled()) {
                    log.debug("Custom property is found in the request. Hence secondary level of filtering required " +
                            "with json key and value:" + request.getCustomProperty());
                }
                metadata = doAdvanceFiltering(metadata, request.getCustomProperty());
            }

            paginationResult.setData(metadata);
            paginationResult.setRecordsFiltered(metadata.size());
            paginationResult.setRecordsTotal(count);
            return paginationResult;
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving metadata entries for given PaginationRequest [rowCount:" +
                    request.getRowCount() + ", startIndex:" + request.getStartIndex() + "]";
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
     * Applies secondary filtering on metadata entries whose meta value is a JSON array, matching a custom
     * property key/value pair against each array element's corresponding field.
     *
     * @param metaList       the metadata entries to filter, as retrieved from the primary query
     * @param customProperty a map containing a single JSON key/value pair used to match against elements
     *                       within each metadata entry's JSON array value
     * @return a filtered list of metadata entries, with each entry's value replaced by the JSON array
     *         elements that matched the given custom property
     */
    private List<Metadata> doAdvanceFiltering(List<Metadata> metaList, Map<String, String> customProperty) {
        List <Metadata> filteredMetaList = new ArrayList<>();
        //since the custom property map always contains one object, the outer loop will only run
        //one iteration.
        Gson gson = new Gson();
        Set<String> jsonKeys = customProperty.keySet();
        for (String jsonKey : jsonKeys) {
            String rawValue = String.valueOf(customProperty.get(jsonKey));
            Pattern regexPattern = Pattern.compile("(?i).*" + Pattern.quote(rawValue) + ".*");
            String metaPayload = "";
            for (Metadata metaEntry : metaList) {
                String metaValue = metaEntry.getMetaValue();
                JsonElement rootElement = gson.fromJson(metaValue, JsonElement.class);
                if (rootElement != null && rootElement.isJsonArray()) {
                    JsonArray jsonArray = rootElement.getAsJsonArray();
                    for (JsonElement element : jsonArray) {
                        String value = element.getAsJsonObject().get(jsonKey).getAsString();
                        if (regexPattern.matcher(value).matches()) {
                            metaPayload = metaPayload.concat(element.toString());
                        }
                    }
                }
                metaEntry.setMetaValue(metaPayload);
                filteredMetaList.add(metaEntry);
            }
        }
        return filteredMetaList;
    }
    /**
     * Determines whether the given metaKey requires tenant-scoped storage under a specific
     * target tenant rather than the currently active tenant. This applies to per-tenant
     * configuration keys such as per-device cost, DFRM enablement, and permission config,
     * which are managed from the carbon.super admin console but must be stored under the
     * respective target tenant's context.
     *
     * @param metaKey the metadata key to evaluate
     * @return {@code true} if the metaKey requires tenant-scoped storage, {@code false} otherwise
     */
    private boolean isPerTenantMetaKey(String metaKey) {
        return metaKey.endsWith("_PER_DEVICE_COST")
                || metaKey.contains("_dfrm_enabled_tenant")
                || metaKey.contains("_permission_config");
    }

    /**
     * Extracts the target tenant domain from a per-tenant metaKey by stripping the
     * known per-tenant key suffixes. The tenant domain is expected to be the prefix
     * portion of the metaKey before the suffix delimiter.
     *
     * @param metaKey the per-tenant metadata key from which to extract the tenant domain
     * @return the tenant domain extracted from the metaKey
     */
    private String extractTenantDomain(String metaKey) {
        if (metaKey.endsWith("_PER_DEVICE_COST")) {
            return metaKey.replace("_PER_DEVICE_COST", "");
        } else if (metaKey.contains("_dfrm_enabled_tenant")) {
            return metaKey.replace("_dfrm_enabled_tenant", "");
        } else if (metaKey.contains("_permission_config")) {
            return metaKey.replace("_permission_config", "");
        }
        return metaKey;
    }
}