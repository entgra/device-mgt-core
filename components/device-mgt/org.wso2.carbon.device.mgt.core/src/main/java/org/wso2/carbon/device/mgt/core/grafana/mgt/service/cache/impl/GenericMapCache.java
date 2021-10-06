/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache.impl;

import org.wso2.carbon.device.mgt.core.grafana.mgt.service.bean.Datasource;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenericMapCache<K, V> implements Cache<K, V> {
    private final Map<K, V> cache;

    public GenericMapCache() {
        cache = new ConcurrentHashMap<>();
    }
    @Override
    public void add(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }
}
