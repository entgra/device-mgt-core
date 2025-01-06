package io.entgra.device.mgt.core.device.mgt.core.metadata.mgt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.util.MetadataConstants;
import io.entgra.device.mgt.core.device.mgt.core.dto.notification.mgt.NotificationConfigDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;


public class NotificationConfigServiceImpl {
    private static final Log log = LogFactory.getLog(NotificationConfigServiceImpl.class);

    private final MetadataDAO metadataDAO;

    public NotificationConfigServiceImpl(MetadataDAO metadataDAO) {
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
    }

    public void addNotificationConfigContext(int tenantId, List<NotificationConfigDTO> configurations) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (!metadataDAO.isExist(tenantId, MetadataConstants.NOTIFICATION_CONFIG_META_KEY)) {
                Metadata configMetadata = constructNotificationConfigContext(configurations);
                metadataDAO.addMetadata(tenantId, configMetadata);
            }
        } catch (TransactionManagementException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String message = "Error occurred while opening connection to the database";
            log.error(message, e);
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String message = "Error adding notification configuration context";
            log.error(message, e);
        } finally {
            MetadataManagementDAOFactory.commitTransaction();
        }
    }


    /**
     * Constructs a Metadata object for Notification Configuration using ObjectMapper.
     *
     * @param configurations A list of NotificationConfigDTO objects containing notification configuration details.
     * @return A Metadata object containing the serialized notification configuration.
     */
    public Metadata constructNotificationConfigContext(List<NotificationConfigDTO> configurations) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode notificationConfigObject = objectMapper.createObjectNode();
            ArrayNode configArray = objectMapper.createArrayNode();
            for (NotificationConfigDTO config : configurations) {
                ObjectNode configNode = objectMapper.valueToTree(config);
                configArray.add(configNode);
            }
            notificationConfigObject.set("configurations", configArray);
            Metadata configMetadata = new Metadata();
            configMetadata.setMetaKey(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);
            configMetadata.setMetaValue(notificationConfigObject.toString());
            return configMetadata;
        } catch (Exception e) {
            throw new RuntimeException("Error constructing Notification Config Context", e);
        }
    }

    /**
     * Deletes a specific notification configuration from the Metadata context for a given tenant.
     *
     * @param tenantId    The tenant ID associated with the notification configurations.
     * @param operationId The unique identifier (operationId) of the notification configuration to be deleted.
     * @throws MetadataManagementException If no configuration is found with the specified operationId, or
     * if any error occurs during the database transaction or processing
     * This method retrieves the existing notification configuration context for the given tenant, removes the
     * configuration matching the provided operationId, and updates the Metadata context with the remaining configurations.
     */


    public void deleteNotificationConfigContext(int tenantId, String operationId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.beginTransaction();
            Metadata existingMetadata = metadataDAO.getMetadata(tenantId, MetadataConstants.NOTIFICATION_CONFIG_META_KEY);
            if (existingMetadata == null) {
                throw new MetadataManagementException("No notification configuration context found for tenant: " + tenantId);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            List<NotificationConfigDTO> configurations = objectMapper.readValue(
                    existingMetadata.getMetaValue(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, NotificationConfigDTO.class)
            );
            boolean isRemoved = configurations.removeIf(config -> config.getOperationId().equals(operationId));
            if (!isRemoved) {
                throw new MetadataManagementException("No configuration found with operationId: " + operationId);
            }
            existingMetadata.setMetaValue(objectMapper.writeValueAsString(configurations));
            metadataDAO.updateMetadata(tenantId, existingMetadata);

        } catch (TransactionManagementException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String message = "Error occurred while managing the database transaction";
            log.error(message, e);
            throw new MetadataManagementException(message, e);
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String message = "Error deleting notification configuration context";
            log.error(message, e);
            throw new MetadataManagementException(message, e);
        } catch (Exception e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String message = "Unexpected error occurred while deleting notification configuration";
            log.error(message, e);
            throw new MetadataManagementException(message, e);
        } finally {
            MetadataManagementDAOFactory.commitTransaction();
        }
    }

    /**
     * Updates an existing notification configuration or adds a new configuration to the Metadata context for a given tenant.
     *
     * @param tenantId      The tenant ID associated with the notification configurations.
     * @param updatedConfig The notification configuration to be updated or added.
     *                      If a configuration with the same operationId exists, it will be updated; otherwise, it will be added as a new entry.
     * @throws MetadataManagementException If any error occurs during the database transaction or processing.
     *
     * This method retrieves the existing notification configuration context for the given tenant. If a configuration with the same
     * operationId as the provided configuration exists, it updates that configuration with the new details. Otherwise, it appends
     * the provided configuration as a new entry. The updated configurations are then serialized and saved back to the Metadata context.
     */

    public void upsertNotificationConfigContext(int tenantId, NotificationConfigDTO updatedConfig) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.beginTransaction();
            Metadata existingMetadata = metadataDAO.getMetadata(tenantId, MetadataConstants.NOTIFICATION_CONFIG_META_KEY);

            ObjectMapper objectMapper = new ObjectMapper();
            List<NotificationConfigDTO> configurations;

            if (existingMetadata != null) {
                configurations = objectMapper.readValue(
                        existingMetadata.getMetaValue(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, NotificationConfigDTO.class)
                );

                boolean isUpdated = false;

                for (int i = 0; i < configurations.size(); i++) {
                    if (configurations.get(i).getOperationId().equals(updatedConfig.getOperationId())) {
                        configurations.set(i, updatedConfig);
                        isUpdated = true;
                        break;
                    }
                }
                if (!isUpdated) {
                    configurations.add(updatedConfig);
                }
            } else {
                configurations = new ArrayList<>();
                configurations.add(updatedConfig);
            }

            Metadata updatedMetadata = new Metadata();
            updatedMetadata.setMetaKey(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);
            updatedMetadata.setMetaValue(objectMapper.writeValueAsString(configurations));

            if (existingMetadata != null) {
                metadataDAO.updateMetadata(tenantId, updatedMetadata);
            } else {
                metadataDAO.addMetadata(tenantId, updatedMetadata);
            }

            MetadataManagementDAOFactory.commitTransaction();
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String message = "Error updating or adding notification configuration context";
            log.error(message, e);
            throw new MetadataManagementException(message, e);
        } catch (Exception e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String message = "Error processing notification configuration context";
            log.error(message, e);
            throw new MetadataManagementException(message, e);
        }
    }


}





