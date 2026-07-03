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
import io.entgra.device.mgt.core.device.mgt.common.device.details.EventDetailsWrapper;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.EventPublishingException;
import io.entgra.device.mgt.core.application.mgt.core.util.Constants;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.config.ReportMgtConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.report.mgt.config.ReportMgtConfigurationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
        return publish(deviceDetailsWrapper.getJSONString(), eventUrl, Constants.REPORTING_DATA_LABEL);
    }

    public Future<Integer> publishLogData(EventDetailsWrapper logsDetailsWrapper, String eventUrl) {
        return publish(logsDetailsWrapper.getJSONString(), eventUrl, Constants.REPORTING_DEVICE_LOGS_LABEL);
    }

    private Future<Integer> publish(String jsonPayload, String eventUrl, String dataLabel) {
        return executorService.submit(new ReportingPublisher(jsonPayload, eventUrl, dataLabel));
    }
    public Future<Integer> publishLogData(String logPayload, String eventUrl) {
        return executorService.submit(new LogPublisher(logPayload, eventUrl));
    }
    private class ReportingPublisher implements Callable<Integer> {
        private final String payload;
        private final String endpoint;
        private final String dataLabel;

        private ReportingPublisher(String payload, String endpoint, String dataLabel) {
            this.payload = payload;
            this.endpoint = endpoint;
            this.dataLabel = dataLabel;
        }

        @Override
        public Integer call() throws EventPublishingException {
            HttpPost apiEndpoint = new HttpPost(endpoint);
            apiEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());

            try {
                StringEntity requestEntity = new StringEntity(payload, ContentType.APPLICATION_JSON);
                apiEndpoint.setEntity(requestEntity);
                try (CloseableHttpResponse response = httpClient.execute(apiEndpoint)) {
                    int statusCode = response.getStatusLine().getStatusCode();

                    if (log.isDebugEnabled()) {
                        log.debug("Published " + dataLabel + " to reporting backend: " + endpoint +
                                ", Response code: " + statusCode);
                    }
                    return statusCode;
                }
            } catch (ConnectException e) {
                String message = "Connection refused while publishing " + dataLabel + " to the API: " + endpoint;
                log.error(message, e);
                throw new EventPublishingException(message, e);
            } catch (IOException e) {
                String message = "Error occurred when publishing " + dataLabel + " to the API: " + endpoint;
                log.error(message, e);
                throw new EventPublishingException(message, e);
            }
        }
    }

    private static class LogPublisher implements Callable<Integer> {
        private final String logPayload;
        private final String endpointUrl;

        LogPublisher(String logPayload, String endpointUrl) {
            this.logPayload = logPayload;
            this.endpointUrl = endpointUrl;
        }
        @Override
        public Integer call() throws EventPublishingException {

            try (CloseableHttpClient client =
                         HttpClients.custom().setConnectionManager(poolingManager).build()) {

                HttpPost post = new HttpPost(endpointUrl);
                post.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
                post.setEntity(new StringEntity(logPayload, ContentType.APPLICATION_JSON));

                HttpResponse response = client.execute(post);
                int status = response.getStatusLine().getStatusCode();

                if (log.isDebugEnabled()) {
                    log.debug("Published DEVICE_LOG to reporting backend: "
                            + endpointUrl + " | Status: " + status);
                }

                return status;

            } catch (ConnectException e) {
                String msg = "Connection refused while publishing DEVICE_LOG → " + endpointUrl;
                log.error(msg, e);
                throw new EventPublishingException(msg, e);
            } catch (IOException e) {
                String msg = "I/O error while publishing DEVICE_LOG → " + endpointUrl;
                log.error(msg, e);
                throw new EventPublishingException(msg, e);
            }
        }
    }


}
