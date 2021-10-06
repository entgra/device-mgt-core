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

package org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.exceptions.GrafanaManagementException;
import org.wso2.carbon.device.mgt.core.grafana.mgt.config.GrafanaConfiguration;
import org.wso2.carbon.device.mgt.core.grafana.mgt.config.GrafanaConfigurationManager;
import org.wso2.carbon.device.mgt.core.grafana.mgt.config.xml.bean.CacheConfiguration;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.bean.Datasource;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache.impl.LRUCache;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache.impl.GenericMapCache;
import org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache.impl.QueryTemplateCacheKey;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaConstants;
import org.wso2.carbon.device.mgt.core.grafana.mgt.util.GrafanaUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    private static final Log log = LogFactory.getLog(CacheManager.class);
    private static final Map<String, Integer> cacheCapacityMap = new ConcurrentHashMap<>();

    private final Map<Integer, Cache<QueryTemplateCacheKey, String>> queryTemplateAPICache;
    private final Map<Integer, Cache<String, String>> encodedQueryCache;
    private final GenericMapCache<Integer, Datasource> datasourceAPICache;

    private CacheManager() {
        this.queryTemplateAPICache = new ConcurrentHashMap<>();
        this.encodedQueryCache = new ConcurrentHashMap<>();
        this.datasourceAPICache = new GenericMapCache<>();
    }

    private static final class CacheManagerHolder {
        static final CacheManager cacheManager = new CacheManager();
    }

    public static CacheManager getInstance() {
        return CacheManagerHolder.cacheManager;
    }

    public Cache<String, String> getEncodedQueryCache() throws GrafanaManagementException {
        String cacheName = GrafanaConstants.ENCODED_QUERY_CACHE_NAME;
        initCacheCapacityIfNotExist(cacheName);
        int tenantId = GrafanaUtil.getTenantId();
        return encodedQueryCache.computeIfAbsent(tenantId, k ->
                new LRUCache<>(cacheCapacityMap.get(cacheName)));
    }

    public Cache<QueryTemplateCacheKey, String> getQueryTemplateAPICache() throws GrafanaManagementException {
        String cacheName = GrafanaConstants.QUERY_API_CACHE_NAME;
        initCacheCapacityIfNotExist(cacheName);
        int tenantId = GrafanaUtil.getTenantId();
        return queryTemplateAPICache.computeIfAbsent(tenantId, k ->
                new LRUCache<>(cacheCapacityMap.get(cacheName)));
    }

    public Cache<Integer, Datasource> getDatasourceAPICache() {
        return datasourceAPICache;
    }

    private static void initCacheCapacityIfNotExist(String cacheName) throws GrafanaManagementException {
        if (cacheCapacityMap.get(cacheName) == null) {
            initCacheCapacity(cacheName);
        }
    }

    private synchronized static void initCacheCapacity(String cacheName) throws GrafanaManagementException {
        if (cacheCapacityMap.get(cacheName) == null) {
            GrafanaConfiguration configuration = GrafanaConfigurationManager.getInstance().getGrafanaConfiguration();
            CacheConfiguration cacheConfig = configuration.getCacheByName(cacheName);
            if (cacheConfig == null) {
                log.error("CacheConfiguration config not defined for " + cacheName);
                throw new GrafanaManagementException("Query API CacheConfiguration configuration not properly defined");
            }
            cacheCapacityMap.put(cacheName, cacheConfig.getCapacity());
        }
    }

}
