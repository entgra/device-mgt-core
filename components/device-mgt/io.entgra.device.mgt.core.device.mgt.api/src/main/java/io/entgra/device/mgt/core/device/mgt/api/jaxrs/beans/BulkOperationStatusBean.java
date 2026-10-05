/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * Payload for updating the status of multiple operations on a single device.
 */
@ApiModel(
        value = "BulkOperationStatusBean",
        description = "Shared status plus the list of operations to update on a device."
)
public class BulkOperationStatusBean {

    @ApiModelProperty(
            name = "status",
            value = "Status to apply to all listed operations.",
            required = true
    )
    private String status;

    @ApiModelProperty(
            name = "operations",
            value = "Operations to update. Each item needs operationId; operationCode is optional " +
                    "but recommended for application install/uninstall side effects.",
            required = true
    )
    private List<OperationStatusBean> operations;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OperationStatusBean> getOperations() {
        return operations;
    }

    public void setOperations(List<OperationStatusBean> operations) {
        this.operations = operations;
    }
}
