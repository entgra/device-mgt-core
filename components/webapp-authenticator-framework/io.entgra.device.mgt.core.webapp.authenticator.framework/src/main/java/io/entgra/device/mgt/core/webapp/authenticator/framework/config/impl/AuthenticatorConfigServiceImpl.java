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

package io.entgra.device.mgt.core.webapp.authenticator.framework.config.impl;

import io.entgra.device.mgt.core.webapp.authenticator.framework.config.AuthenticatorConfig;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.AuthenticatorConfigService;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.InvalidConfigurationStateException;
import io.entgra.device.mgt.core.webapp.authenticator.framework.config.WebappAuthenticatorConfig;

import java.util.List;

/**
 * This holds implementation of AuthenticatorConfigService.
 */
public class AuthenticatorConfigServiceImpl implements AuthenticatorConfigService {

    @Override
    public AuthenticatorConfig getAuthenticatorConfig(String authenticatorName) throws
            InvalidConfigurationStateException {
        List<AuthenticatorConfig> configs = WebappAuthenticatorConfig.getInstance().getAuthenticators();
        int index;
        if (authenticatorName == null || authenticatorName.isEmpty()) {
            return null;
        }
        for (int i = 0; i < configs.size(); i++) {
            AuthenticatorConfig authenticatorConfig = configs.get(i);
            if (authenticatorName.equals(authenticatorConfig.getName())) {
                index = i;
                return configs.get(index);
            }
        }
        return null;
    }
}
