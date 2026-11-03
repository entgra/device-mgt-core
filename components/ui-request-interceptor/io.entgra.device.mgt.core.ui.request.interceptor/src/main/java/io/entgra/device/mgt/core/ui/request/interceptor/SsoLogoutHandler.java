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

package io.entgra.device.mgt.core.ui.request.interceptor;

import io.entgra.device.mgt.core.ui.request.interceptor.beans.AuthData;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@MultipartConfig
@WebServlet("/ssoLogout")
public class SsoLogoutHandler extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No active SSO session is available");
            return;
        }

        AuthData authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
        String logoutEndpoint = (String) session.getAttribute(HandlerConstants.OIDC_LOGOUT_ENDPOINT_KEY);
        String postLogoutRedirectUri = req.getParameter("post_logout_redirect_uri");
        String logoutUrl = buildLogoutUrl(logoutEndpoint, authData, postLogoutRedirectUri);
        if (logoutUrl == null) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "OIDC logout endpoint and client session information are required");
            return;
        }

        for (String path : HandlerConstants.SSO_LOGOUT_COOKIE_PATHS) {
            removeCookie(HandlerConstants.JSESSIONID_KEY, path, resp);
        }
        session.invalidate();
        resp.sendRedirect(logoutUrl);
    }

    static String buildLogoutUrl(String logoutEndpoint, AuthData authData, String postLogoutRedirectUri) {
        if (isBlank(logoutEndpoint) || authData == null ||
                (isBlank(authData.getIdToken()) && isBlank(authData.getClientId()))) {
            return null;
        }
        StringBuilder logoutUrl = new StringBuilder(logoutEndpoint).append('?');
        if (!isBlank(authData.getIdToken())) {
            appendQueryParameter(logoutUrl, "id_token_hint", authData.getIdToken());
        } else {
            appendQueryParameter(logoutUrl, "client_id", authData.getClientId());
        }
        if (!isBlank(postLogoutRedirectUri)) {
            logoutUrl.append('&');
            appendQueryParameter(logoutUrl, "post_logout_redirect_uri", postLogoutRedirectUri);
        }
        return logoutUrl.toString();
    }

    private static void appendQueryParameter(StringBuilder url, String name, String value) {
        url.append(name).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static void removeCookie(String cookieName, String path, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath(path);
        cookie.setValue(null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
