/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.device.mgt.core.dto.operation.mgt;

import java.sql.Timestamp;

public class OperationResponseMeta {

    private int id;
    private int enrolmentId;
    private int operationMappingId;
    private boolean isLargeResponse;
    private Timestamp receivedTimestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEnrolmentId() {
        return enrolmentId;
    }

    public void setEnrolmentId(int enrolmentId) {
        this.enrolmentId = enrolmentId;
    }

    public int getOperationMappingId() {
        return operationMappingId;
    }

    public void setOperationMappingId(int operationMappingId) {
        this.operationMappingId = operationMappingId;
    }

    public boolean isLargeResponse() {
        return isLargeResponse;
    }

    public void setLargeResponse(boolean largeResponse) {
        isLargeResponse = largeResponse;
    }

    public Timestamp getReceivedTimestamp() {
        return receivedTimestamp;
    }

    public void setReceivedTimestamp(Timestamp receivedTimestamp) {
        this.receivedTimestamp = receivedTimestamp;
    }
}
