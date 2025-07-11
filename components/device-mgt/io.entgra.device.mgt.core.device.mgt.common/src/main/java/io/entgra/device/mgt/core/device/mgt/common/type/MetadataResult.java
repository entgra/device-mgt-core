/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.device.mgt.common.type;

import java.util.List;

public class MetadataResult<T> {
    private boolean exists;
    private List<T> definitions;

    public MetadataResult(boolean exists, List<T> definitions) {
        this.exists = exists;
        this.definitions = definitions;
    }

    public boolean isExists() {
        return exists;
    }

    public List<T> getDefinitions() {
        return definitions;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public void setDefinitions(List<T> definitions) {
        this.definitions = definitions;
    }
}

