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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes whether a logout token matched persistent mappings and identifies possible node-local sessions.
 */
public class BackChannelLogoutResult {

    private final List<String> httpSessionIds;
    private final boolean mappingFound;

    public BackChannelLogoutResult(List<String> httpSessionIds, boolean mappingFound) {
        this.httpSessionIds = httpSessionIds == null ? Collections.emptyList() :
                Collections.unmodifiableList(new ArrayList<>(httpSessionIds));
        this.mappingFound = mappingFound;
    }

    public List<String> getHttpSessionIds() {
        return httpSessionIds;
    }

    public boolean isMappingFound() {
        return mappingFound;
    }
}
