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
package io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.common.exception.GrafanaManagementException;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.exception.MaliciousQueryAttempt;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.exception.QueryMisMatch;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.exception.QueryNotFound;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.service.GrafanaAPIService;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.service.GrafanaQueryService;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.service.bean.Datasource;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.service.cache.CacheManager;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.service.cache.QueryTemplateCacheKey;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.sql.query.GrafanaPreparedQueryBuilder;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.sql.query.PreparedQuery;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.sql.query.encoder.QueryEncoderFactory;
import io.entgra.device.mgt.core.analytics.mgt.grafana.proxy.core.util.GrafanaConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DBConnectionException;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

public class GrafanaQueryServiceImpl implements GrafanaQueryService {

    private static final Log log = LogFactory.getLog(GrafanaQueryServiceImpl.class);
    private final GrafanaAPIService grafanaAPIService;

    public GrafanaQueryServiceImpl(GrafanaAPIService grafanaAPIService) {
        this.grafanaAPIService = grafanaAPIService;
    }

    public void buildSafeQuery(JsonObject queryRequestBody, String dashboardUID, String panelId, URI requestUri)
            throws IOException, SQLException, GrafanaManagementException, DBConnectionException, io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException {
        JsonArray queries = queryRequestBody.getAsJsonArray(GrafanaConstants.QUERY_BODY_QUERIES_KEY);
        for (int i = 0; i < queries.size(); i++) {
            JsonObject queryObj = queries.get(i).getAsJsonObject();
            JsonElement refIdJson = queryObj.get(GrafanaConstants.QUERY_REF_ID_KEY);
            JsonElement rawSqlJson = queryObj.get(GrafanaConstants.RAW_SQL_KEY);
            JsonElement datasourceIdJson = queryObj.get(GrafanaConstants.DATASOURCE_ID_KEY);
            if (refIdJson == null || rawSqlJson == null || datasourceIdJson == null) {
                String errMsg = "Query json body: refId, rawSql and datasourceId cannot be null";
                log.error(errMsg);
                throw new MaliciousQueryAttempt(errMsg);
            }
            String refId = refIdJson.getAsString();
            String rawSql = rawSqlJson.getAsString();
            int datasourceId = datasourceIdJson.getAsInt();
            CacheManager cacheManager = CacheManager.getInstance();
            String encodedQuery = cacheManager.getEncodedQueryCache().getIfPresent(rawSql);
            if (encodedQuery != null && !encodedQuery.isEmpty()) {
                // Checks if the tenant ID in the cached query (encodedQuery) is matching the current tenant ID
                // taken from Carbon Context and if it's not matching then the query is modified with the current
                // tenant ID and then added to the cache
                if (encodedQuery.contains(GrafanaConstants.ENCODED_QUERY_TENANT_ID_KEY)) {
                    String encodedQueryTenantId = GrafanaPreparedQueryBuilder.getEncodedQueryTenantId(encodedQuery);
                    boolean isMatchingTenantId = GrafanaPreparedQueryBuilder.isMatchingTenantId(encodedQueryTenantId);
                    if (isMatchingTenantId) {
                        queryObj.addProperty(GrafanaConstants.RAW_SQL_KEY, encodedQuery);
                    } else {
                        String modifiedEncodedQuery = GrafanaPreparedQueryBuilder.modifyEncodedQuery(encodedQuery);
                        CacheManager.getInstance().getEncodedQueryCache().put(rawSql, modifiedEncodedQuery);
                        queryObj.addProperty(GrafanaConstants.RAW_SQL_KEY, modifiedEncodedQuery);
                    }
                } else {
                    queryObj.addProperty(GrafanaConstants.RAW_SQL_KEY, encodedQuery);
                }
                return;
            }
            Datasource datasource = cacheManager.getDatasourceAPICache().getIfPresent(datasourceId);
            if (datasource == null) {
                datasource = grafanaAPIService.getDatasource(datasourceId, requestUri.getScheme());
            }
            String queryTemplate = cacheManager.getQueryTemplateAPICache().
                    getIfPresent(new QueryTemplateCacheKey(dashboardUID, panelId, refId));
            try {
                if (queryTemplate != null) {
                    try {
                        encodeQuery(queryObj, datasource, queryTemplate, rawSql);
                    } catch (QueryMisMatch e) {
                        log.error("Error occurred while encoding query, " +
                                "retrying to encode by getting the query template from api instead of cache", e);
                        queryTemplate = grafanaAPIService.getQueryTemplate(dashboardUID, panelId, refId, requestUri.getScheme());
                        encodeQuery(queryObj, datasource, queryTemplate, rawSql);
                    }
                } else {
                    queryTemplate = grafanaAPIService.getQueryTemplate(dashboardUID, panelId, refId, requestUri.getScheme());
                    encodeQuery(queryObj, datasource, queryTemplate, rawSql);
                }
            } catch (QueryNotFound e) {
                String errMsg = "No query exists for {dashboard: " + dashboardUID +
                        ", panelId: " + panelId + ", refId: " +  refId + "}";
                log.error(errMsg);
                throw new QueryNotFound(errMsg);
            }
        }
    }

    private void encodeQuery(JsonObject queryObj, Datasource datasource, String queryTemplate, String rawSql)
            throws SQLException, GrafanaManagementException, DBConnectionException,
            io.entgra.device.mgt.core.application.mgt.common.exception.DBConnectionException {
        PreparedQuery pq = GrafanaPreparedQueryBuilder.build(queryTemplate, rawSql);
        String encodedQuery = QueryEncoderFactory.createEncoder(datasource.getType(), datasource.getName()).encode(pq);
        CacheManager.getInstance().getEncodedQueryCache().put(rawSql, encodedQuery);
        if(log.isDebugEnabled()) {
            log.debug("Encoded query: " + encodedQuery);
        }
        queryObj.addProperty(GrafanaConstants.RAW_SQL_KEY, encodedQuery);
    }

}
