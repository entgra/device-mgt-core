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

import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEvent;
import io.entgra.device.mgt.core.device.mgt.common.type.event.mgt.DeviceTypeEventUpdateResult;
import io.entgra.device.mgt.core.device.mgt.common.type.MetadataResult;

import java.util.ArrayList;
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

    private final DeviceTypeMetaDefinitionProcessor<DeviceTypeEvent> processor;

    public DeviceTypeEventManagementProviderServiceImpl(MetadataManagementService metadataManagementService) {
        this.processor = new DeviceTypeMetaDefinitionProcessor<>(
                metadataManagementService,
                DEVICE_EVENT_META_KEY_PATTERN,
                DeviceTypeEvent.class
        );
    }

    @Override
    public MetadataResult<DeviceTypeEvent> getDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException {
        return processor.getDefinitions(deviceType);
    }

    @Override
    public boolean createDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementException {
        return processor.createDefinitions(deviceType, deviceTypeEvents, this::eventToMapList);
    }

    @Override
    public boolean updateDeviceTypeMetaWithEvents(String deviceType, List<DeviceTypeEvent> deviceTypeEvents)
            throws DeviceManagementException {
        return processor.updateDefinitions(deviceType, deviceTypeEvents, this::eventToMapList);
    }

    @Override
    public boolean deleteDeviceTypeEventDefinitions(String deviceType) throws DeviceManagementException {
        return processor.deleteDefinitions(deviceType);
    }

    @Override
    public DeviceTypeEventUpdateResult computeUpdatedDeviceTypeEvents(String deviceType, List<DeviceTypeEvent> incomingEvents)
            throws DeviceManagementException {
        List<DeviceTypeEvent> existingEvents = getDeviceTypeEventDefinitions(deviceType).getDefinitions();
        Map<String, DeviceTypeEvent> existingMap = processor.mapByKey(existingEvents, DeviceTypeEvent::getEventName);
        Map<String, DeviceTypeEvent> incomingMap = processor.mapByKey(incomingEvents, DeviceTypeEvent::getEventName);

        List<DeviceTypeEvent> updatedEvents = new ArrayList<>();
        List<DeviceTypeEvent> unchangedEvents = new ArrayList<>();

        for (DeviceTypeEvent incoming : incomingEvents) {
            DeviceTypeEvent existing = existingMap.get(incoming.getEventName());
            if (existing == null || !incoming.equals(existing)) {
                updatedEvents.add(incoming);
            }
        }

        for (DeviceTypeEvent existing : existingEvents) {
            DeviceTypeEvent incoming = incomingMap.get(existing.getEventName());
            if (incoming == null || existing.equals(incoming)) {
                unchangedEvents.add(existing);
            }
        }

        List<DeviceTypeEvent> mergedEvents = new ArrayList<>();
        mergedEvents.addAll(updatedEvents);
        mergedEvents.addAll(unchangedEvents);

        return new DeviceTypeEventUpdateResult(updatedEvents, mergedEvents);
    }

    // Converts DeviceTypeEvent list to List<Map<String, Object>> for serialization
    private List<Map<String, Object>> eventToMapList(List<DeviceTypeEvent> deviceTypeEvents) {
        List<Map<String, Object>> updatedEvents = new ArrayList<>();
        for (DeviceTypeEvent event : deviceTypeEvents) {
            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put(EVENT_NAME, event.getEventName());
            eventMap.put(TRANSPORT, event.getTransportType().name());
            Map<String, Object> eventAttributes = new HashMap<>();
            List<Map<String, String>> attributes = event.getEventAttributeList().getList().stream()
                    .map(attr -> {
                        Map<String, String> attributeMap = new HashMap<>();
                        attributeMap.put(NAME, attr.getName());
                        attributeMap.put(TYPE, attr.getType().name());
                        return attributeMap;
                    })
                    .collect(Collectors.toList());
            eventAttributes.put(ATTRIBUTES, attributes);
            eventMap.put(EVENT_ATTRIBUTES, eventAttributes);
            eventMap.put(EVENT_TOPIC_STRUCTURE, event.getEventTopicStructure());
            updatedEvents.add(eventMap);
        }
        return updatedEvents;
    }
}