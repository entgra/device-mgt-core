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

package io.entgra.device.mgt.core.apimgt.keymgt.extension.service;

import io.entgra.device.mgt.core.apimgt.keymgt.extension.DCRResponse;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.TokenRequest;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.TokenResponse;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.exception.BadRequestException;
import io.entgra.device.mgt.core.apimgt.keymgt.extension.exception.KeyMgtException;

import java.util.List;

public interface KeyMgtService {

    /***
     * This method will handle the DCR requests for applications
     *
     * @param clientName client name of the application
     * @param owner owner of the application
     * @param grantTypes grant types to be provided
     * @param callBackUrl callback url of the application
     * @param tags api tags for api subscription of the application
     * @param isSaasApp if the application is a saas app
     * @param password Password of the owner
     * @return @{@link DCRResponse} DCR Response object with client credentials
     * @throws KeyMgtException if any error occurs during DCR process
     */
    DCRResponse dynamicClientRegistration(String clientName, String owner, String grantTypes, String callBackUrl,
                                          String[] tags, boolean isSaasApp, int validityPeriod, String password,
                                          List<String> supportedGrantTypes, String callbackUrl) throws KeyMgtException;

    /***
     * This method will handle the access token requests
     *
     * @param tokenRequest token request object
     * @return @{@link TokenResponse} Access token information
     * @throws KeyMgtException if any errors occurred while generating access token
     * @throws BadRequestException if any parameters provided are invalid
     */
    TokenResponse generateAccessToken(TokenRequest tokenRequest) throws KeyMgtException, BadRequestException;
}
