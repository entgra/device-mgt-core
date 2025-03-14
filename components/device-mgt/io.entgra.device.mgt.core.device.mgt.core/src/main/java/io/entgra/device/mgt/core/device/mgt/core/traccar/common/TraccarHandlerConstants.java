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

package io.entgra.device.mgt.core.device.mgt.core.traccar.common;

public class TraccarHandlerConstants {

    public static class TraccarConfig {
        public static final String TRACCAR_CONFIG_XML_NAME = "traccar-config.xml";
        public static final String GATEWAY_NAME = "sample";
        public static final String ENDPOINT = "api-endpoint";
        public static final String AUTHORIZATION = "authorization";
        public static final String AUTHORIZATION_KEY = "authorization-key";
        public static final String DEFAULT_PORT = "default-port";
        public static final String LOCATION_UPDATE_PORT = "location-update-port";
    }

    // TODO: Get these from http client
    public static class Methods {
        public static final String POST = "POST";
        public static final String GET = "GET";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
    }

    public static class Types {
        public static final String DEVICE = "DEVICE";
        public static final String GROUP = "GROUP";
        public static final String USER = "USER";
        public static final String PERMISSION = "PERMISSION";

        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

        public static final int DEFAULT_RANDOM = 10;
        public static final int TRACCAR_TOKEN = 32;
        public static final int REMOVE_TYPE_MULTIPLE = -1;
        public static final int REMOVE_TYPE_SINGLE = 1;
    }

}
