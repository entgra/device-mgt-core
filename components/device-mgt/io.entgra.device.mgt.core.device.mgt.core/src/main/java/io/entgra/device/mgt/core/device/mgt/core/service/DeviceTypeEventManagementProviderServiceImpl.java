/*
 * Copyright (C) 2018 - 2025 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEventUpdateResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.ATTRIBUTES;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.DEVICE_EVENT_META_KEY_PATTERN;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.EVENT_ATTRIBUTES;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.EVENT_NAME;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.EVENT_TOPIC_STRUCTURE;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.NAME;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.TRANSPORT;
import static io.entgra.device.mgt.core.device.mgt.common.type.Constants.TYPE;

public class DeviceTypeEventManagementProviderServiceImpl implements DeviceTypeEventManagementProviderService {

    private static final Log log = LogFactory.getLog(DeviceTypeEventManagementProviderServiceImpl.class);
    private final MetadataManagementService metadataManagementService;

    /**
     * Constructor for DeviceTypeEventManagementProviderServiceImpl.
     *
     * @param metadataManagementService the MetadataManagementService to use (must not be null)
     * @throws IllegalArgumentException if metadataManagementService is null
     */
    public DeviceTypeEventManagementProviderServiceImpl(MetadataManagementService metadataManagementService) {
        if (metadataManagementService == null) {
            throw new IllegalArgumentException("MetadataManagementService must not be null");
        }
        this.metadataManagementService = metadataManagementService;
    }

    @Override
    public List<DeviceTypeEvent> getDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            Metadata metadata = metadataManagementService
                    .retrieveMetadata(String.format(DEVICE_EVENT_META_KEY_PATTERN, deviceType));
            try {
                if (metadata != null) {
                    String eventDefinitionsJson = metadata.getMetaValue();
                    if (eventDefinitionsJson != null && !eventDefinitionsJson.isEmpty()) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                        return objectMapper.readValue(
                                eventDefinitionsJson,
                                objectMapper.getTypeFactory().constructCollectionType(List.class, DeviceTypeEvent.class)
                        );
                    }
                }
            } catch (IOException e) {
                String msg = "I/O error while processing event definition JSON for deviceType: " + deviceType +
                        ", tenantId: " + tenantId;
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            }
            return Collections.emptyList();
        } catch (MetadataManagementException e) {
            String msg = "Error occurred while retrieving metadata";
            log.error(msg, e);
            throw new DeviceManagementException(e);
        }
    }

    @Override
    public boolean createDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            // Initialize ObjectMapper for Jackson processing
            String updatedEventDefinitions;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, Object>> eventDefinitions = addNewEventDefinitions(deviceTypeEvents);
                // Serialize event definitions
                updatedEventDefinitions = objectMapper.writeValueAsString(eventDefinitions);
            } catch (IOException e) {
                String msg = "Failed to process JSON while creating EVENT_DEFINITIONS for device type: " +
                        deviceType + ", tenantId: " + tenantId;
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            }
            Metadata metadata = new Metadata();
            metadata.setMetaKey(String.format(DEVICE_EVENT_META_KEY_PATTERN, deviceType));
            metadata.setMetaValue(updatedEventDefinitions);
            return metadataManagementService.createMetadata(metadata) != null;
        } catch (MetadataManagementException e) {
            String msg = "Error occurred in updating event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    /**
     * Converts a list of {@link DeviceTypeEvent} objects into a list of maps, where each map
     * represents the structure of an event definition in a key-value format.
     * <p>
     * This transformation is useful for serializing event definitions into a format
     * that can be used in configuration files, REST responses, or UI representations.
     * </p>
     *
     * @param deviceTypeEvents The list of {@link DeviceTypeEvent} objects to be transformed.
     * @return A list of maps where each map contains the properties of an event such as name,
     * transport type, attributes, and topic structure.
     */
    private List<Map<String, Object>> addNewEventDefinitions(List<DeviceTypeEvent> deviceTypeEvents) {
        // Create a new list to avoid modifying the original existingEvents list directly
        List<Map<String, Object>> updatedEvents = new ArrayList<>();
        for (DeviceTypeEvent event : deviceTypeEvents) {
            // Create a new map for each event to avoid overriding the existing attributes
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put(EVENT_NAME, event.getEventName());
            eventMap.put(TRANSPORT, event.getTransportType().name());
            Map<String, Object> eventAttributes = new HashMap<>();
            // Add attributes: a list of attribute details inside eventAttributes
            List<Map<String, String>> attributes = event.getEventAttributeList().getList().stream()
                    .map(attr -> {
                        Map<String, String> attributeMap = new HashMap<>();
                        attributeMap.put(NAME, attr.getName());
                        attributeMap.put(TYPE, attr.getType().name()); // Assuming AttributeType is an enum
                        return attributeMap;
                    })
                    .collect(Collectors.toList());
            eventAttributes.put(ATTRIBUTES, attributes); // Nested inside eventAttributes
            // Add the eventAttributes map to the eventMap
            eventMap.put(EVENT_ATTRIBUTES, eventAttributes);
            eventMap.put(EVENT_TOPIC_STRUCTURE, event.getEventTopicStructure());
            // Add the event to the updated events list
            updatedEvents.add(eventMap);
        }
        // Return the updated list of events
        return updatedEvents;
    }

    @Override
    public boolean updateDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            String updatedEventDefinitions;
            try {
                // Initialize ObjectMapper for Jackson processing
                ObjectMapper objectMapper = new ObjectMapper();
                List<Map<String, Object>> eventDefinitions = addNewEventDefinitions(deviceTypeEvents);
                // Serialize event definitions
                updatedEventDefinitions = objectMapper.writeValueAsString(eventDefinitions);
                // Update the database with the new event definitions
            } catch (IOException e) {
                String msg = "Failed to process JSON while updating EVENT_DEFINITIONS for device type: " +
                        deviceType + ", tenantId: " + tenantId;
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            }
            Metadata metadata = new Metadata();
            metadata.setMetaKey(String.format(DEVICE_EVENT_META_KEY_PATTERN, deviceType));
            metadata.setMetaValue(updatedEventDefinitions);
            return metadataManagementService.updateMetadata(metadata) != null;
        } catch (MetadataManagementException e) {
            String msg = "Error occurred in updating event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    @Override
    public boolean deleteDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException {
        try {
            return metadataManagementService
                    .deleteMetadata(String.format(DEVICE_EVENT_META_KEY_PATTERN, deviceType));
        } catch (MetadataManagementException e) {
            String msg = "Error occurred in updating event definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    @Override
    public DeviceTypeEventUpdateResult computeUpdatedDeviceTypeEvents
            (String deviceType, List<DeviceTypeEvent> incomingEvents) throws DeviceManagementException {
        List<DeviceTypeEvent> existingEvents = getDeviceTypeEventDefinitions(deviceType);
        Map<String, DeviceTypeEvent> existingEventMap = mapByName(existingEvents);
        Map<String, DeviceTypeEvent> incomingEventMap = mapByName(incomingEvents);

        List<DeviceTypeEvent> updatedEvents = new ArrayList<>();
        List<DeviceTypeEvent> unchangedEvents = new ArrayList<>();

        for (DeviceTypeEvent incoming : incomingEvents) {
            DeviceTypeEvent existing = existingEventMap.get(incoming.getEventName());
            if (existing == null || !incoming.equals(existing)) {
                updatedEvents.add(incoming);
            }
        }

        for (DeviceTypeEvent existing : existingEvents) {
            DeviceTypeEvent incoming = incomingEventMap.get(existing.getEventName());
            if (incoming == null || existing.equals(incoming)) {
                unchangedEvents.add(existing);
            }
        }

        List<DeviceTypeEvent> mergedEvents = new ArrayList<>();
        mergedEvents.addAll(updatedEvents);
        mergedEvents.addAll(unchangedEvents);

        return new DeviceTypeEventUpdateResult(updatedEvents, mergedEvents);
    }

    /**
     * Creates a map of event names to {@link DeviceTypeEvent} objects from a given list.
     *
     * @param events the list of {@link DeviceTypeEvent} to map
     * @return a map where the key is the event name and the value is the event object
     */
    private Map<String, DeviceTypeEvent> mapByName(List<DeviceTypeEvent> events) {
        return events.stream().collect(Collectors.toMap(DeviceTypeEvent::getEventName, e -> e, (a, b) -> b));
    }
}
