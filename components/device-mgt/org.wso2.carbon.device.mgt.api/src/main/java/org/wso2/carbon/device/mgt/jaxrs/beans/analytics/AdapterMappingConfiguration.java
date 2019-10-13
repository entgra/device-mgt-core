/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.jaxrs.beans.analytics;

import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter mapping configuration definition
 * Attributes : Mapping format and lists for each type of property
 */
public class AdapterMappingConfiguration {
    @ApiModelProperty(value = "Mapping format")
    private MessageFormat messageFormat;
    @ApiModelProperty(value = "Input mapping for json,text and xml mappings")
    private String inputMappingString;
    @ApiModelProperty(value = "Input mapping for json,map and xml mappings")
    private List<MappingProperty> inputMappingProperties = new ArrayList<>();
    @ApiModelProperty(value = "Name-scape mapping for xml mapping")
    private List<MappingProperty> namespaceMappingProperties = new ArrayList<>();
    @ApiModelProperty(value = "Correlation mapping for wso2 mapping")
    private List<MappingProperty> correlationMappingProperties = new ArrayList<>();
    @ApiModelProperty(value = "Payload mapping for wso2 mapping")
    private List<MappingProperty> payloadMappingProperties = new ArrayList<>();
    @ApiModelProperty(value = "Meta mapping for wso2 mapping")
    private List<MappingProperty> metaMappingProperties = new ArrayList<>();

    public MessageFormat getMessageFormat() {
        return messageFormat;
    }

    public void setMessageFormat(MessageFormat messageFormat) {
        this.messageFormat = messageFormat;
    }

    public String getInputMappingString() {
        return inputMappingString;
    }

    public void setInputMappingString(String inputMappingString) {
        this.inputMappingString = inputMappingString;
    }

    public List<MappingProperty> getInputMappingProperties() {
        return inputMappingProperties;
    }

    public void setInputMappingProperties(
            List<MappingProperty> inputMappingProperties) {
        this.inputMappingProperties = inputMappingProperties;
    }

    public List<MappingProperty> getNamespaceMappingProperties() {
        return namespaceMappingProperties;
    }

    public void setNamespaceMappingProperties(
            List<MappingProperty> namespaceMappingProperties) {
        this.namespaceMappingProperties = namespaceMappingProperties;
    }

    public List<MappingProperty> getCorrelationMappingProperties() {
        return correlationMappingProperties;
    }

    public void setCorrelationMappingProperties(
            List<MappingProperty> correlationMappingProperties) {
        this.correlationMappingProperties = correlationMappingProperties;
    }

    public List<MappingProperty> getPayloadMappingProperties() {
        return payloadMappingProperties;
    }

    public void setPayloadMappingProperties(
            List<MappingProperty> payloadMappingProperties) {
        this.payloadMappingProperties = payloadMappingProperties;
    }

    public List<MappingProperty> getMetaMappingProperties() {
        return metaMappingProperties;
    }

    public void setMetaMappingProperties(
            List<MappingProperty> metaMappingProperties) {
        this.metaMappingProperties = metaMappingProperties;
    }
}
