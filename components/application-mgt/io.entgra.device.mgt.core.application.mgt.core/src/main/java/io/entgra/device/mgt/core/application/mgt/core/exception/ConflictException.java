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

package io.entgra.device.mgt.core.application.mgt.core.exception;

import io.entgra.device.mgt.core.application.mgt.common.exception.ApplicationManagementException;

/**
 * Thrown when the request conflicts with an existing application resource (e.g. duplicate package name).
 */
public class ConflictException extends ApplicationManagementException {

    public ConflictException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ConflictException(String message) {
        setMessage(message);
    }

}
