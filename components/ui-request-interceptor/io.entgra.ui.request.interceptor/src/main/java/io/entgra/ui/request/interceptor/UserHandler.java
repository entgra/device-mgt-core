/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.ui.request.interceptor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.entgra.ui.request.interceptor.beans.AuthData;
import io.entgra.ui.request.interceptor.util.HandlerConstants;
import io.entgra.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.wso2.carbon.device.application.mgt.common.ProxyResponse;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@MultipartConfig
@WebServlet("/user")
public class UserHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(UserHandler.class);
    private static final long serialVersionUID = 9050048549140517002L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String platform = req.getParameter(HandlerConstants.PLATFORM);
            String serverUrl =
                    req.getScheme() + HandlerConstants.SCHEME_SEPARATOR + req.getServerName() + HandlerConstants.COLON
                            + System.getProperty("iot.gateway.https.port");
            if (StringUtils.isBlank(platform)) {
                sendUnAuthorizeResponse(req, resp, serverUrl, platform);
                return;
            }

            HttpSession httpSession = req.getSession(false);
            if (httpSession == null) {
                sendUnAuthorizeResponse(req, resp, serverUrl, platform);
                return;
            }

            AuthData authData = (AuthData) httpSession.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
            if (authData == null) {
                sendUnAuthorizeResponse(req, resp, serverUrl, platform);
                return;
            }

            String accessToken = authData.getAccessToken();

            HttpPost tokenEndpoint = new HttpPost(serverUrl + HandlerConstants.INTROSPECT_ENDPOINT);
            tokenEndpoint.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString());
            StringEntity tokenEPPayload = new StringEntity("token=" + accessToken,
                    ContentType.APPLICATION_FORM_URLENCODED);
            tokenEndpoint.setEntity(tokenEPPayload);
            ProxyResponse tokenStatus = HandlerUtil.execute(tokenEndpoint);

            if (tokenStatus.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                log.error("Error occurred while invoking the API to get token status.");
                HandlerUtil.handleError(req, resp, serverUrl, platform, tokenStatus);
                return;
            }
            String tokenData = tokenStatus.getData();
            if (tokenData == null) {
                log.error("Invalid token data is received.");
                HandlerUtil.handleError(req, resp, serverUrl, platform, tokenStatus);
                return;
            }
            JsonParser jsonParser = new JsonParser();
            JsonElement jTokenResult = jsonParser.parse(tokenData);
            if (jTokenResult.isJsonObject()) {
                JsonObject jTokenResultAsJsonObject = jTokenResult.getAsJsonObject();
                if (!jTokenResultAsJsonObject.get("active").getAsBoolean()) {
                    sendUnAuthorizeResponse(req, resp, serverUrl, platform);
                    return;
                }
                ProxyResponse proxyResponse = new ProxyResponse();
                proxyResponse.setCode(HttpStatus.SC_OK);
                proxyResponse.setData(
                        jTokenResultAsJsonObject.get("username").getAsString().replaceAll("@carbon.super", ""));
                HandlerUtil.handleSuccess(req, resp, serverUrl, platform, proxyResponse);
            }
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (JsonSyntaxException e) {
            log.error("Error occurred while parsing the response. ", e);
        }
    }

    /**
     * Send UnAuthorized Response to the user
     * 
     * @param req HttpServletRequest object
     * @param resp HttpServletResponse object
     * @param serverUrl Url of the server
     * @param platform Requested platform
     */
    private void sendUnAuthorizeResponse(HttpServletRequest req, HttpServletResponse resp, String serverUrl, String platform)
            throws IOException {
        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(HttpStatus.SC_UNAUTHORIZED);
        proxyResponse.setExecutorResponse(
                HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_UNAUTHORIZED));
        HandlerUtil.handleError(req, resp, serverUrl, platform, proxyResponse);
    }
}
