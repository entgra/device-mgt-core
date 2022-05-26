/*
 * Copyright (C) 2018 - 2022 Entgra (Pvt) Ltd, Inc - All Rights Reserved.
 *
 * Unauthorised copying/redistribution of this file, via any medium is strictly prohibited.
 *
 * Licensed under the Entgra Commercial License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://entgra.io/licenses/entgra-commercial/1.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.traccar.common.config;

public class TraccarConfigurationException extends Exception {
    public TraccarConfigurationException() {
        super();
    }

    public TraccarConfigurationException(String message) {
        super(message);
    }

    public TraccarConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TraccarConfigurationException(Throwable cause) {
        super(cause);
    }
}
