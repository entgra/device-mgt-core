/*
 * Copyright (C) 2018 - 2025 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.core.constant;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class Constants {
    public static final String CONFIG_PATH = CarbonUtils.getCarbonConfigDirPath() + File.separator + "dynamic" +
            "-tasks.json";
    public static final String TASK_NAME_SEPARATOR = "_";
    public static final String TASK_PREFIX = "_TASK";

    public static class CONFIG_PREFIX {
        public static final String DEFAULT_CATEGORIZED_DYNAMIC_TASKS_TEMPLATE_PREFIX =
                "_DEFAULT_CATEGORIZED_DYNAMIC_TASKS_TEMPLATE";
        public static final String CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX =
                "_CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG";
    }

    public static class TASK_PROPERTY {
        public static final String CATEGORIZED_DYNAMIC_TASK = "CATEGORIZED_DYNAMIC_TASK";
        public static final String TENANT_ID = "TENANT_ID";
        public static final String CATEGORIZED_DYNAMIC_TASK_CLAZZ = "io.entgra.device.mgt.core.dynamic" +
                ".task.mgt.core.task.CategorizedDynamicNTask";
    }
}
