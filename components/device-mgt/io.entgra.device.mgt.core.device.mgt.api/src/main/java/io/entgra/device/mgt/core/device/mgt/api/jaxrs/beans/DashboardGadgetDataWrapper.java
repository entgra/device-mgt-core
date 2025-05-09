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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import java.util.List;

public class DashboardGadgetDataWrapper {

    private String context;
    private String groupingAttribute;
    private List<?> data;

    @SuppressWarnings("unused")
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @SuppressWarnings("unused")
    public String getGroupingAttribute() {
        return groupingAttribute;
    }

    public void setGroupingAttribute(String groupingAttribute) {
        this.groupingAttribute = groupingAttribute;
    }

    @SuppressWarnings("unused")
    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }

}
