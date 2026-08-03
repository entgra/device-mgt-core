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
package io.entgra.device.mgt.core.apimgt.application.extension.validator;

import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;

/**
 * Extension point for validating prerequisites before OAuth / API application registration.
 * <p>
 * Implementations are registered as OSGi services. Each validator should no-op when the given
 * tenant is outside its scope (for example, when the tenant is not listed in feature-specific
 * metadata).
 */
public interface ApiApplicationRegistrationValidator {

    /**
     * Validates whether API application registration may proceed for the given tenant.
     *
     * @param tenantDomain Tenant domain where registration will run
     * @throws APIManagerException when validation fails or cannot be completed
     */
    void validate(String tenantDomain) throws APIManagerException;
}
