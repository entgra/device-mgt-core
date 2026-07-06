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
package io.entgra.device.mgt.core.device.mgt.core.config.push.notification;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * FCM specific push notification configurations. These control the dedicated wake-up call
 * dispatcher used by the FCM notification strategy: the per-device cooldown that suppresses
 * duplicate wake-up calls, and the two priority thread pools (user vs task) that prevent
 * task initiated wake-up calls from delaying user initiated ones.
 */
@XmlRootElement(name = "FCMConfiguration")
public class FCMConfiguration {

    private int deviceCooldownSeconds = 120;
    private int userPoolSize = 50;
    private int taskPoolSize = 10;
    private int userQueueCapacity = 10000;
    private int taskQueueCapacity = 20000;
    private int httpMaxRequests = 200;
    private int httpMaxRequestsPerHost = 200;
    private int httpConnectTimeoutSeconds = 10;
    private int httpWriteTimeoutSeconds = 15;
    private int httpReadTimeoutSeconds = 30;
    private int httpCallTimeoutSeconds = 60;
    private int httpConnectionPoolMaxIdle = 25;
    private int httpConnectionKeepAliveMinutes = 1;

    /**
     * Period (in seconds) during which further FCM wake-up calls to the same device are
     * suppressed after a call has been sent. User initiated (high priority) calls bypass this.
     */
    @XmlElement(name = "DeviceCooldownSeconds")
    public int getDeviceCooldownSeconds() {
        return deviceCooldownSeconds;
    }

    public void setDeviceCooldownSeconds(int deviceCooldownSeconds) {
        this.deviceCooldownSeconds = deviceCooldownSeconds;
    }

    /**
     * Number of worker threads dedicated to user initiated (high priority) wake-up calls.
     */
    @XmlElement(name = "UserPoolSize")
    public int getUserPoolSize() {
        return userPoolSize;
    }

    public void setUserPoolSize(int userPoolSize) {
        this.userPoolSize = userPoolSize;
    }

    /**
     * Number of worker threads dedicated to task initiated (low priority) wake-up calls.
     */
    @XmlElement(name = "TaskPoolSize")
    public int getTaskPoolSize() {
        return taskPoolSize;
    }

    public void setTaskPoolSize(int taskPoolSize) {
        this.taskPoolSize = taskPoolSize;
    }

    /**
     * Bounded queue capacity for the user pool.
     */
    @XmlElement(name = "UserQueueCapacity")
    public int getUserQueueCapacity() {
        return userQueueCapacity;
    }

    public void setUserQueueCapacity(int userQueueCapacity) {
        this.userQueueCapacity = userQueueCapacity;
    }

    /**
     * Bounded queue capacity for the task pool. When exceeded the oldest queued task wake-up
     * call is dropped (the periodic scheduler task will eventually re-notify those devices).
     */
    @XmlElement(name = "TaskQueueCapacity")
    public int getTaskQueueCapacity() {
        return taskQueueCapacity;
    }

    public void setTaskQueueCapacity(int taskQueueCapacity) {
        this.taskQueueCapacity = taskQueueCapacity;
    }

    /**
     * Maximum number of concurrent FCM HTTP requests across all hosts (OkHttp dispatcher limit).
     */
    @XmlElement(name = "HttpMaxRequests")
    public int getHttpMaxRequests() {
        return httpMaxRequests;
    }

    public void setHttpMaxRequests(int httpMaxRequests) {
        this.httpMaxRequests = httpMaxRequests;
    }

    /**
     * Maximum number of concurrent FCM HTTP requests per host. FCM is a single host, so this is
     * the effective concurrency limit; OkHttp's default of 5 is too low for the wake-up
     * dispatcher's fan-out and causes queued calls to time out under load.
     */
    @XmlElement(name = "HttpMaxRequestsPerHost")
    public int getHttpMaxRequestsPerHost() {
        return httpMaxRequestsPerHost;
    }

    public void setHttpMaxRequestsPerHost(int httpMaxRequestsPerHost) {
        this.httpMaxRequestsPerHost = httpMaxRequestsPerHost;
    }

    /**
     * OkHttp connect timeout (seconds) for FCM requests. Zero means no timeout.
     */
    @XmlElement(name = "HttpConnectTimeoutSeconds")
    public int getHttpConnectTimeoutSeconds() {
        return httpConnectTimeoutSeconds;
    }

    public void setHttpConnectTimeoutSeconds(int httpConnectTimeoutSeconds) {
        this.httpConnectTimeoutSeconds = httpConnectTimeoutSeconds;
    }

    /**
     * OkHttp write timeout (seconds) for FCM requests. Zero means no timeout.
     */
    @XmlElement(name = "HttpWriteTimeoutSeconds")
    public int getHttpWriteTimeoutSeconds() {
        return httpWriteTimeoutSeconds;
    }

    public void setHttpWriteTimeoutSeconds(int httpWriteTimeoutSeconds) {
        this.httpWriteTimeoutSeconds = httpWriteTimeoutSeconds;
    }

    /**
     * OkHttp read timeout (seconds) for FCM requests. Governs the response-header read; zero
     * means no timeout.
     */
    @XmlElement(name = "HttpReadTimeoutSeconds")
    public int getHttpReadTimeoutSeconds() {
        return httpReadTimeoutSeconds;
    }

    public void setHttpReadTimeoutSeconds(int httpReadTimeoutSeconds) {
        this.httpReadTimeoutSeconds = httpReadTimeoutSeconds;
    }

    /**
     * OkHttp call timeout (seconds) spanning the entire FCM request including the time a call
     * spends queued in the dispatcher. Zero means no timeout.
     */
    @XmlElement(name = "HttpCallTimeoutSeconds")
    public int getHttpCallTimeoutSeconds() {
        return httpCallTimeoutSeconds;
    }

    public void setHttpCallTimeoutSeconds(int httpCallTimeoutSeconds) {
        this.httpCallTimeoutSeconds = httpCallTimeoutSeconds;
    }

    /**
     * Maximum number of idle connections kept in the OkHttp connection pool.
     */
    @XmlElement(name = "HttpConnectionPoolMaxIdle")
    public int getHttpConnectionPoolMaxIdle() {
        return httpConnectionPoolMaxIdle;
    }

    public void setHttpConnectionPoolMaxIdle(int httpConnectionPoolMaxIdle) {
        this.httpConnectionPoolMaxIdle = httpConnectionPoolMaxIdle;
    }

    /**
     * Keep-alive duration (minutes) for idle connections in the OkHttp connection pool.
     */
    @XmlElement(name = "HttpConnectionKeepAliveMinutes")
    public int getHttpConnectionKeepAliveMinutes() {
        return httpConnectionKeepAliveMinutes;
    }

    public void setHttpConnectionKeepAliveMinutes(int httpConnectionKeepAliveMinutes) {
        this.httpConnectionKeepAliveMinutes = httpConnectionKeepAliveMinutes;
    }
}
