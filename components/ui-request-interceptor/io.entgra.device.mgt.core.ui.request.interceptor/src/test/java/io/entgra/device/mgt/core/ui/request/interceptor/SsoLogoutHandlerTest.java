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

package io.entgra.device.mgt.core.ui.request.interceptor;

import io.entgra.device.mgt.core.ui.request.interceptor.beans.AuthData;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SsoLogoutHandlerTest {

    private static final String LOGOUT_ENDPOINT = "https://localhost:9443/oidc/logout";

    @Test
    public void testBuildLogoutUrlWithIdTokenHint() {
        AuthData authData = new AuthData();
        authData.setClientId("client-id");
        authData.setIdToken("header.payload.signature");

        String logoutUrl = SsoLogoutHandler.buildLogoutUrl(LOGOUT_ENDPOINT, authData,
                "https://localhost:9443/app/login?logged_out=true");

        Assert.assertEquals(logoutUrl, LOGOUT_ENDPOINT +
                "?id_token_hint=header.payload.signature&post_logout_redirect_uri=" +
                "https%3A%2F%2Flocalhost%3A9443%2Fapp%2Flogin%3Flogged_out%3Dtrue");
    }

    @Test
    public void testBuildLogoutUrlFallsBackToClientId() {
        AuthData authData = new AuthData();
        authData.setClientId("client id");

        Assert.assertEquals(SsoLogoutHandler.buildLogoutUrl(LOGOUT_ENDPOINT, authData, null),
                LOGOUT_ENDPOINT + "?client_id=client+id");
    }
}
