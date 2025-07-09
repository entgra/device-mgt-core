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
package io.entgra.device.mgt.core.device.mgt.core.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.device.mgt.common.type.MetadataResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DeviceTypeMetaDefinitionProcessor<T> {

    private static final Log log = LogFactory.getLog(DeviceTypeMetaDefinitionProcessor.class);
    private final MetadataManagementService metadataManagementService;
    private final String metaKeyPattern;
    private final Class<T> type;

    public DeviceTypeMetaDefinitionProcessor(MetadataManagementService metadataManagementService,
                                             String metaKeyPattern,
                                             Class<T> type) {
        this.metadataManagementService = metadataManagementService;
        this.metaKeyPattern = metaKeyPattern;
        this.type = type;
    }

    public MetadataResult<T> getDefinitions(String deviceType) throws DeviceManagementException {
        try {
            Metadata metadata = metadataManagementService
                    .retrieveMetadata(String.format(metaKeyPattern, deviceType));
            if (metadata != null) {
                String json = metadata.getMetaValue();
                if (json != null && !json.isEmpty()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
                    List<T> definitions = objectMapper.readValue(
                            json,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, type)
                    );
                    return new MetadataResult<>(true, definitions);
                }
                // Metadata record exists but value is empty
                return new MetadataResult<>(true, Collections.emptyList());
            }
            // Metadata record doesn't exist at all
            return new MetadataResult<>(false, Collections.emptyList());
        } catch (MetadataManagementException | IOException e) {
            String msg = "Error occurred while retrieving definitions for deviceType: " + deviceType;
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    public boolean createDefinitions(String deviceType, List<T> definitions,
                                     Function<List<T>, List<Map<String, Object>>> transformer) throws DeviceManagementException {
        return saveDefinitions(deviceType, definitions, transformer, true);
    }

    public boolean updateDefinitions(String deviceType, List<T> definitions,
                                     Function<List<T>, List<Map<String, Object>>> transformer) throws DeviceManagementException {
        return saveDefinitions(deviceType, definitions, transformer, false);
    }

    private boolean saveDefinitions(String deviceType, List<T> definitions,
                                    Function<List<T>, List<Map<String, Object>>> transformer, boolean isCreate)
            throws DeviceManagementException {
        try {
            String json;
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                if (transformer != null) {
                    List<Map<String, Object>> transformed = transformer.apply(definitions);
                    json = objectMapper.writeValueAsString(transformed);
                } else {
                    json = objectMapper.writeValueAsString(definitions);
                }
            } catch (IOException e) {
                String msg = "Failed to process JSON for device type: " + deviceType +
                        "Definitions: " + definitions + ". " ;
                log.error(msg, e);
                throw new DeviceManagementException(msg, e);
            }
            Metadata metadata = new Metadata();
            metadata.setMetaKey(String.format(metaKeyPattern, deviceType));
            metadata.setMetaValue(json);
            if (isCreate) {
                return metadataManagementService.createMetadata(metadata) != null;
            } else {
                return metadataManagementService.updateMetadata(metadata) != null;
            }
        } catch (MetadataManagementException e) {
            String msg = "Error occurred in saving definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    public boolean deleteDefinitions(String deviceType) throws DeviceManagementException {
        try {
            return metadataManagementService
                    .deleteMetadata(String.format(metaKeyPattern, deviceType));
        } catch (MetadataManagementException e) {
            String msg = "Error occurred in deleting definitions.";
            log.error(msg, e);
            throw new DeviceManagementException(msg, e);
        }
    }

    public Map<String, T> mapByKey(List<T> list, Function<T, String> keyExtractor) {
        return list.stream().collect(Collectors.toMap(keyExtractor, t -> t, (a, b) -> b));
    }
}