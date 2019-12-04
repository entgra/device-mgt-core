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
package org.wso2.carbon.device.application.mgt.core.dto;

import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;

import java.util.ArrayList;
import java.util.List;

public class ApplicationsDTO {

    public static ApplicationDTO getApp1() {
        ApplicationDTO app = new ApplicationDTO();
        List<String> categories = new ArrayList<>();

        categories.add("Test Category");
        app.setAppCategories(categories);
        app.setDescription("Test app Description");
        app.setDeviceTypeId(1);
        app.setName("First Test App");
        app.setSubType("I dont Know");
        app.setType("Idontknow");
        return app;
    }
}
