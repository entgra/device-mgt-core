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
package io.entgra.device.mgt.core.apimgt.webapp.publisher;

import io.entgra.device.mgt.core.apimgt.webapp.publisher.dto.ApiScope;
import io.entgra.device.mgt.core.apimgt.webapp.publisher.dto.ApiUriTemplate;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Set;

/**
 * This bean class carries the properties used by some API that needs to be published within the underlying
 * API-Management infrastructure.
 * A sample API configuration accepted by this particular bean class would look like what's shown below.
 * e.g.
 *
 * <API>
 * <Name>enrollment</Name>
 * <Owner>admin</Owner>
 * <Context>/enrol</Context>
 * <Version>1.0.0</Version>
 * <Endpoint>http://localhost:9763/</Endpoint>
 * <Transports>http,https</Transports>
 * </API>
 */
@XmlRootElement(name = "API")
public class APIConfig {

    private String name;
    private String owner;
    private String context;
    private String apiDocumentationName;
    private String apiDocumentationSummary;
    private String apiDocumentationSourceFile;
    private String endpoint;
    private String version;
    private String policy;
    private String transports;
    private boolean isSecured;
    private Set<ApiUriTemplate> uriTemplates;
    private boolean isSharedWithAllTenants;
    private String tenantDomain;
    private String[] tags;
    private Set<ApiScope> scopes;
    private boolean isDefault = true;
    private String endpointType;
    private String inSequenceName;
    private String inSequenceConfig;
    private String asyncApiDefinition;

    @XmlElement(name = "Policy", required = true)
    public String getPolicy() {
        return policy;
    }

    @SuppressWarnings("unused")
    public void setPolicy(String policy) {
        this.policy = policy;
    }

    @XmlElement(name = "Name", required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "ApiDocumentationName", required = false)
    public String getApiDocumentationName() {
        return apiDocumentationName;
    }

    public void setApiDocumentationName(String apiDocumentationName) {
        this.apiDocumentationName = apiDocumentationName;
    }

    @XmlElement(name = "ApiDocumentationSummary", required = false)
    public String getApiDocumentationSummary() {
        return apiDocumentationSummary;
    }

    public void setApiDocumentationSummary(String apiDocumentationSummary) {
        this.apiDocumentationSummary = apiDocumentationSummary;
    }

    @XmlElement(name = "ApiDocumentationSourceFile", required = false)
    public String getApiDocumentationSourceFile() {
        return apiDocumentationSourceFile;
    }

    public void setApiDocumentationSourceFile(String apiDocumentationSourceFile) {
        this.apiDocumentationSourceFile = apiDocumentationSourceFile;
    }

    @XmlElement(name = "Owner", required = true)
    public String getOwner() {
        return owner;
    }

    @SuppressWarnings("unused")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @XmlElement(name = "Context", required = true)
    public String getContext() {
        return context;
    }

    @SuppressWarnings("unused")
    public void setContext(String context) {
        this.context = context;
    }

    @XmlElement(name = "Endpoint", required = true)
    public String getEndpoint() {
        return endpoint;
    }

    @SuppressWarnings("unused")
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @XmlElement(name = "Version", required = false)
    public String getVersion() {
        return version;
    }

    @SuppressWarnings("unused")
    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "Transports", required = false)
    public String getTransports() {
        return transports;
    }

    @SuppressWarnings("unused")
    public void setTransports(String transports) {
        this.transports = transports;
    }

    @XmlElement(name = "isSecured", required = false)
    public boolean isSecured() {
        return isSecured;
    }

    @SuppressWarnings("unused")
    public void setSecured(boolean secured) {
        isSecured = secured;
    }

    @XmlElement(name = "UriTemplates", required = false)
    public Set<ApiUriTemplate> getUriTemplates() {
        return uriTemplates;
    }

    @SuppressWarnings("unused")
    public void setUriTemplates(Set<ApiUriTemplate> uriTemplates) {
        this.uriTemplates = uriTemplates;
    }

    @XmlElement(name = "isSharedWithAllTenants", required = false)
    public boolean isSharedWithAllTenants() {
        return isSharedWithAllTenants;
    }

    @SuppressWarnings("unused")
    public void setSharedWithAllTenants(boolean isSharedWithAllTenants) {
        this.isSharedWithAllTenants = isSharedWithAllTenants;
    }

    @XmlElement(name = "tenantDomain", required = false)
    public String getTenantDomain() {
        return tenantDomain;
    }

    @SuppressWarnings("unused")
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    @XmlElement(name = "tags", required = false)
    public String[] getTags() {
        return tags;
    }

    @SuppressWarnings("unused")
    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public Set<ApiScope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<ApiScope> scopes) {
        this.scopes = scopes;
    }

    @XmlElement(name = "isDefault")
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @XmlElement(name = "endpointType")
    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    @XmlElement(name = "inSequenceName")
    public String getInSequenceName() {
        return inSequenceName;
    }

    public void setInSequenceName(String inSequenceName) {
        this.inSequenceName = inSequenceName;
    }

    @XmlElement(name = "inSequenceConfig")
    public String getInSequenceConfig() {
        return inSequenceConfig;
    }

    public void setInSequenceConfig(String inSequenceConfig) {
        this.inSequenceConfig = inSequenceConfig;
    }

    @XmlElement(name = "asyncApiDefinition")
    public String getAsyncApiDefinition() {
        return asyncApiDefinition;
    }

    public void setAsyncApiDefinition(String asyncApiDefinition) {
        this.asyncApiDefinition = asyncApiDefinition;
    }
}
