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

package io.entgra.device.mgt.core.application.mgt.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * This class holds necessary data to represent a paginated result.
 */
@ApiModel(value = "PaginationResult", description = "This class carries all information related Pagination Result")
public class PaginationResult implements Serializable {

    private static final long serialVersionUID = 1998101711L;

    @ApiModelProperty(name = "recordsTotal", value = "The total number of records that are given before filtering", required = true)
    private int recordsTotal;

    @ApiModelProperty(name = "recordsFiltered", value = "The total number of records that are given after filtering", required = true)
    private int recordsFiltered;

    @ApiModelProperty(name = "draw", value = "The draw counter that this object is a response to, from the draw parameter sent as part of the data request", required = true)
    private int draw;

    @ApiModelProperty(name = "data", value = "This holds the database records that matches given criteria", required = true)
    private List<?> data;

    public int getRecordsTotal() {
        return recordsTotal;
    }

    public int getRecordsFiltered() {
        return recordsFiltered;
    }

    public void setRecordsFiltered(int recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }

    public void setRecordsTotal(int recordsTotal) {
        this.recordsTotal = recordsTotal;

    }

    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }

    public int getDraw() {
        return draw;
    }

    public void setDraw(int draw) {
        this.draw = draw;
    }
}
