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
package io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm;

/**
 * An immutable description of a single FCM wake-up call to be delivered asynchronously by the
 * {@link FCMWakeUpDispatcher}. The actual HTTP send is captured as a {@link Sender} so that the
 * credential/token resolution stays in the {@link FCMNotificationStrategy}; the dispatcher only
 * owns prioritisation, deduplication and retry.
 */
public class FCMWakeUpRequest {

    /** Performs the blocking FCM HTTP POST. Implemented by the strategy. */
    @FunctionalInterface
    public interface Sender {
        void send() throws Exception;
    }

    private final int tenantId;
    private final String deviceType;
    private final String deviceIdentifier;
    private final boolean highPriority;
    private final int operationId;
    private final int enrolmentId;
    private final Sender sender;

    private FCMWakeUpRequest(Builder builder) {
        this.tenantId = builder.tenantId;
        this.deviceType = builder.deviceType;
        this.deviceIdentifier = builder.deviceIdentifier;
        this.highPriority = builder.highPriority;
        this.operationId = builder.operationId;
        this.enrolmentId = builder.enrolmentId;
        this.sender = builder.sender;
    }

    public int getTenantId() {
        return tenantId;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public boolean isHighPriority() {
        return highPriority;
    }

    public int getOperationId() {
        return operationId;
    }

    public int getEnrolmentId() {
        return enrolmentId;
    }

    public Sender getSender() {
        return sender;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int tenantId;
        private String deviceType;
        private String deviceIdentifier;
        private boolean highPriority;
        private int operationId;
        private int enrolmentId;
        private Sender sender;

        public Builder tenantId(int tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder deviceType(String deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public Builder deviceIdentifier(String deviceIdentifier) {
            this.deviceIdentifier = deviceIdentifier;
            return this;
        }

        public Builder highPriority(boolean highPriority) {
            this.highPriority = highPriority;
            return this;
        }

        public Builder operationId(int operationId) {
            this.operationId = operationId;
            return this;
        }

        public Builder enrolmentId(int enrolmentId) {
            this.enrolmentId = enrolmentId;
            return this;
        }

        public Builder sender(Sender sender) {
            this.sender = sender;
            return this;
        }

        public FCMWakeUpRequest build() {
            return new FCMWakeUpRequest(this);
        }
    }
}
