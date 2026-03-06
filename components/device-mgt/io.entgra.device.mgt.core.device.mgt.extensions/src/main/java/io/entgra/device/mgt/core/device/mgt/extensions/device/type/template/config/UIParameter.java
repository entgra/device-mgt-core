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

package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Java class for uiParams complex type.
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <xs:element name="uiParam" maxOccurs="unbounded">
 *   <xs:complexType>
 *     <xs:sequence>
 *       <xs:element name="type" type="xs:string" />
 *       <xs:element name="name" type="xs:string" />
 *       <xs:element name="id" type="xs:string" />
 *       <xs:element name="values">
 *         <xs:complexType>
 *           <xs:sequence>
 *             <xs:element name="value" type="xs:string" />
 *           </xs:sequence>
 *         </xs:complexType>
 *       </xs:element>
 *     </xs:sequence>
 *     <xs:attribute name="optional" type="xs:string" />
 *   </xs:complexType>
 * </xs:element>
 * </pre>
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UIParameter {

    @XmlElement(name = "id", required = true)
    protected String id;

    @XmlAttribute(name = "optional", required = true)
    private boolean optional;

    @XmlElement(name = "type", required = true)
    protected String type;

    @XmlElement(name = "name")
    protected String name;

    @XmlElement(name = "label")
    private String label;

    @XmlElement(name = "helper")
    private String helper;

    @XmlElementWrapper(name = "values")
    @XmlElement(name = "value")
    protected List<String> value;

    @XmlElementWrapper(name = "payloadValues")
    @XmlElement(name = "value")
    protected List<String> payloadValue;

    @XmlElement(name = "key")
    protected String key;

    @XmlElementWrapper(name = "Conditions")
    @XmlElement(name = "Condition")
    private List<Condition> conditions;

    @XmlElement(name = "defaultValue")
    private String defaultValue;

    @XmlElementWrapper(name = "conditionLabels")
    @XmlElement(name = "conditionLabel")
    private List<ConditionLabel> conditionLabels;

    @XmlElementWrapper(name = "rules")
    @XmlElement(name = "rule")
    private List<Rule> rules;

    @XmlElement(name = "isDisplay")
    private boolean display;

    @XmlElement(name = "payloadKey")
    private String payloadKey;

    @XmlElement(name = "dataSourceKey")
    private String dataSourceKey;

    @XmlElement(name = "dataSourcePath")
    private String dataSourcePath;

    @XmlElement(name = "dataSourceSearchPath")
    private String dataSourceSearchPath;

    @XmlElement(name = "rowKey")
    private String rowKey;

    @XmlElementWrapper(name = "tableSelectColumns")
    @XmlElement(name = "tableSelectColumn")
    private List<TableSelectColumn> tableSelectColumns;

    /** Optional link for extra resources. */
    @XmlElement(name = "configLinkPath")
    private String configLinkPath;

    /** Optional i18n key for the config link label. */
    @XmlElement(name = "configLinkLabelKey")
    private String configLinkLabelKey;

    /** Scope required to load the table list. */
    @XmlElement(name = "requiredScope")
    private String requiredScope;

    /** Scope required for search requests. */
    @XmlElement(name = "searchScope")
    private String searchScope;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHelper() {
        return helper;
    }

    public void setHelper(String helper) {
        this.helper = helper;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(
            List<Condition> conditions) {
        this.conditions = conditions;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<ConditionLabel> getConditionLabels() {
        return conditionLabels;
    }

    public void setConditionLabels(
            List<ConditionLabel> conditionLabels) {
        this.conditionLabels = conditionLabels;
    }

    public List<String> getPayloadValue() {
        return payloadValue;
    }

    public void setPayloadValue(List<String> payloadValue) {
        this.payloadValue = payloadValue;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public boolean isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public String getPayloadKey() {
        return payloadKey;
    }

    public void setPayloadKey(String payloadKey) {
        this.payloadKey = payloadKey;
    }

    public String getDataSourceKey() {
        return dataSourceKey;
    }

    public void setDataSourceKey(String dataSourceKey) {
        this.dataSourceKey = dataSourceKey;
    }

    public String getDataSourcePath() {
        return dataSourcePath;
    }

    public void setDataSourcePath(String dataSourcePath) {
        this.dataSourcePath = dataSourcePath;
    }

    public String getDataSourceSearchPath() {
        return dataSourceSearchPath;
    }

    public void setDataSourceSearchPath(String dataSourceSearchPath) {
        this.dataSourceSearchPath = dataSourceSearchPath;
    }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public List<TableSelectColumn> getTableSelectColumns() {
        return tableSelectColumns;
    }

    public void setTableSelectColumns(List<TableSelectColumn> tableSelectColumns) {
        this.tableSelectColumns = tableSelectColumns;
    }

    public String getConfigLinkPath() {
        return configLinkPath;
    }

    public void setConfigLinkPath(String configLinkPath) {
        this.configLinkPath = configLinkPath;
    }

    public String getConfigLinkLabelKey() {
        return configLinkLabelKey;
    }

    public void setConfigLinkLabelKey(String configLinkLabelKey) {
        this.configLinkLabelKey = configLinkLabelKey;
    }

    public String getRequiredScope() {
        return requiredScope;
    }

    public void setRequiredScope(String requiredScope) {
        this.requiredScope = requiredScope;
    }

    public String getSearchScope() {
        return searchScope;
    }

    public void setSearchScope(String searchScope) {
        this.searchScope = searchScope;
    }
}
