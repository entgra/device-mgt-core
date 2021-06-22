/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class holds required parameters for a querying a paginated device response.
 */
public class PaginationRequest {

    private int startIndex;
    private int rowCount;
    private int groupId;
    private String owner;
    private String ownerPattern;
    private String deviceType;
    private String deviceName;
    private String ownership;
    private String ownerRole;
    private Date since;
    private String filter;
    private String serialNumber;
    private Map<String, Object> property = new HashMap<>();
    private List<String> statusList = new ArrayList<>();
    private OperationLogFilters operationLogFilters = new OperationLogFilters();
    public OperationLogFilters getOperationLogFilters() {
        return operationLogFilters;
    }
    public void setOperationLogFilters(OperationLogFilters operationLogFilters) {
        this.operationLogFilters = operationLogFilters;
    }
    public PaginationRequest(int start, int rowCount) {
        this.startIndex = start;
        this.rowCount = rowCount;
    }
    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public List<String> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<String> statusList) {
        this.statusList = statusList;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOwnership() {
        return ownership;
    }

    public void setOwnership(String ownership) {
        this.ownership = ownership;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public String getOwnerRole() {
        return ownerRole;
    }

    public void setOwnerRole(String ownerRole) {
        this.ownerRole = ownerRole;
    }

    public String getOwnerPattern() {
        return ownerPattern;
    }

    public void setOwnerPattern(String ownerPattern) {
        this.ownerPattern = ownerPattern;
    }

    public void setProperty(String key, Object value) {
        this.property.put(key, value);
    }

    public void setProperties(Map<String, Object> parameters) {
        this.property.putAll(parameters);
    }

    public Object getProperty(String key) {
        return this.property.get(key);
    }

    public String getSerialNumber() { return serialNumber; }

    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public Map<String, Object> getProperties() {
        Map<String, Object> temp = new HashMap<>();
        temp.putAll(property);
        return temp;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "Device type '" + this.deviceType + "' Device Name '" + this.deviceName + "' row count: " + this.rowCount
                + " Owner role '" + this.ownerRole + "' owner pattern '" + this.ownerPattern + "' ownership "
                + this.ownership + "' Status '" + this.statusList + "' owner '" + this.owner + "' groupId: " + this.groupId
                + " start index: " + this.startIndex;
    }
}
