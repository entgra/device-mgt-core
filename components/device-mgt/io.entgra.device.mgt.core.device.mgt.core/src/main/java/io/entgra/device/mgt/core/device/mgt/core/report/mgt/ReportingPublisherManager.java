/*
 * Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.core.report.mgt;

import io.entgra.device.mgt.core.device.mgt.common.device.details.DeviceDetailsWrapper;
import io.entgra.device.mgt.core.device.mgt.common.device.details.LogsDetailsWrapper;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.EventPublishingException;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.config.ReportMgtConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.config.ReportMgtConfigurationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ReportingPublisherManager {

    private static final Log log = LogFactory.getLog(ReportingPublisherManager.class);
    private final PoolingHttpClientConnectionManager poolingManager;
    private final ExecutorService executorService;
    private final CloseableHttpClient httpClient;
    private static volatile ReportingPublisherManager instance;

    public static ReportingPublisherManager getInstance() {
        if (instance == null) {
            synchronized (ReportingPublisherManager.class) {
                if (instance == null) {
                    instance = new ReportingPublisherManager();
                }
            }
        }
        return instance;
    }

    private ReportingPublisherManager() {
        ReportMgtConfiguration config = ReportMgtConfigurationManager.getInstance().getConfiguration();
        this.executorService = Executors.newFixedThreadPool(config.getThreadPoolSize());

        this.poolingManager = new PoolingHttpClientConnectionManager();
        this.poolingManager.setMaxTotal(config.getMaxConnections());
        this.poolingManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerRoute());

        this.httpClient = HttpClients.custom()
                .setConnectionManager(poolingManager)
                .build();
    }

    public Future<Integer> publishData(DeviceDetailsWrapper deviceDetailsWrapper, String eventUrl) {
        return executorService.submit(new ReportingPublisher(deviceDetailsWrapper, eventUrl));
    }

    public Future<Integer> publishLogData(LogsDetailsWrapper logsDetailsWrapper, String eventUrl) {
        return executorService.submit(
                new LogsReportingPublisher(logsDetailsWrapper, eventUrl)
        );
    }


    private class ReportingPublisher implements Callable<Integer> {
        private final DeviceDetailsWrapper payload;
        private final String endpoint;

        private ReportingPublisher(DeviceDetailsWrapper payload, String eventUrl) {
            this.payload = payload;
            this.endpoint = eventUrl;
        }

        @Override
        public Integer call() throws EventPublishingException {
            HttpPost apiEndpoint = new HttpPost(endpoint);
            apiEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

            try {
                StringEntity requestEntity = new StringEntity(payload.getJSONString(), ContentType.APPLICATION_JSON);
                apiEndpoint.setEntity(requestEntity);
                try (CloseableHttpResponse response = httpClient.execute(apiEndpoint)) {
                    int statusCode = response.getStatusLine().getStatusCode();

                    if (log.isDebugEnabled()) {
                        log.debug("Published data to reporting backend: " + endpoint +
                                ", Response code: " + statusCode);
                    }
                    return statusCode;
                }
            } catch (ConnectException e) {
                String message = "Connection refused while publishing reporting data to the API: " + endpoint;
                log.error(message, e);
                throw new EventPublishingException(message, e);
            } catch (IOException e) {
                String message = "Error occurred when publishing reporting data to the API: " + endpoint;
                log.error(message, e);
                throw new EventPublishingException(message, e);
            }
        }
    }

    private class LogsReportingPublisher implements Callable<Integer> {

        private final LogsDetailsWrapper payload;
        private final String endpoint;

        private LogsReportingPublisher(LogsDetailsWrapper payload, String endpoint) {
            this.payload = payload;
            this.endpoint = endpoint;
        }

        @Override
        public Integer call() throws EventPublishingException {

            HttpPost apiEndpoint = new HttpPost(endpoint);
            apiEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

            try {
                StringEntity requestEntity =
                        new StringEntity(payload.getJSONString(), ContentType.APPLICATION_JSON);
                apiEndpoint.setEntity(requestEntity);

                try (CloseableHttpResponse response = httpClient.execute(apiEndpoint)) {
                    int statusCode = response.getStatusLine().getStatusCode();

                    if (log.isDebugEnabled()) {
                        log.debug("Published DEVICE_LOGS to reporting backend: "
                                + endpoint + ", Response code: " + statusCode);
                    }
                    return statusCode;
                }

            } catch (ConnectException e) {
                String message = "Connection refused while publishing DEVICE_LOGS to: " + endpoint;
                log.error(message, e);
                throw new EventPublishingException(message, e);
            } catch (IOException e) {
                String message = "Error occurred while publishing DEVICE_LOGS to: " + endpoint;
                log.error(message, e);
                throw new EventPublishingException(message, e);
            }
        }
    }

}
