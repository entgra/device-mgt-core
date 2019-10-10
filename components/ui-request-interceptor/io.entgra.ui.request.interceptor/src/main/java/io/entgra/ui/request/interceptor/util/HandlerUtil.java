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

package io.entgra.ui.request.interceptor.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.device.application.mgt.common.ProxyResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class HandlerUtil {

    private static final Log log = LogFactory.getLog(HandlerUtil.class);

    /***
     *
     * @param httpRequest - httpMethod e.g:- HttpPost, HttpGet
     * @return response as string
     * @throws IOException IO exception returns if error occurs when executing the httpMethod
     */
    public static ProxyResponse execute(HttpRequestBase httpRequest) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(httpRequest);
            ProxyResponse proxyResponse = new ProxyResponse();

            if (response == null) {
                proxyResponse.setCode(HandlerConstants.INTERNAL_ERROR_CODE);
                proxyResponse.setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(
                        HandlerConstants.INTERNAL_ERROR_CODE));
                return proxyResponse;
            } else {
                int statusCode = response.getStatusLine().getStatusCode();
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }

                    String jsonString = result.toString();
                    if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {
                        proxyResponse.setCode(statusCode);
                        proxyResponse.setData(jsonString);
                        proxyResponse.setExecutorResponse("SUCCESS");
                        return proxyResponse;
                    } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                        if (jsonString.contains("Access token expired") || jsonString
                                .contains("Invalid input. Access token validation failed")) {
                            proxyResponse.setCode(statusCode);
                            proxyResponse.setExecutorResponse(HandlerConstants.TOKEN_IS_EXPIRED);
                            return proxyResponse;
                        } else {
                            proxyResponse.setCode(statusCode);
                            proxyResponse.setData(jsonString);
                            proxyResponse.setExecutorResponse(
                                    HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(statusCode));
                            return proxyResponse;
                        }
                    }
                    proxyResponse.setCode(statusCode);
                    proxyResponse.setData(jsonString);
                    proxyResponse
                            .setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(statusCode));
                    return proxyResponse;
                }
            }
        }
    }

    /***
     *
     * @param statusCode Provide status code, e.g:- 400, 401, 500 etc
     * @return relative status code key for given status code.
     */
    public static String getStatusKey (int statusCode){
        String statusCodeKey;

        switch (statusCode) {
        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            statusCodeKey = "internalServerError";
            break;
        case HttpStatus.SC_BAD_REQUEST:
            statusCodeKey = "badRequest";
            break;
        case HttpStatus.SC_UNAUTHORIZED:
            statusCodeKey = "unauthorized";
            break;
        case HttpStatus.SC_FORBIDDEN:
            statusCodeKey = "forbidden";
            break;
        case HttpStatus.SC_NOT_FOUND:
            statusCodeKey = "notFound";
            break;
        case HttpStatus.SC_METHOD_NOT_ALLOWED:
            statusCodeKey = "methodNotAllowed";
            break;
        case HttpStatus.SC_NOT_ACCEPTABLE:
            statusCodeKey = "notAcceptable";
            break;
        case HttpStatus.SC_UNSUPPORTED_MEDIA_TYPE:
            statusCodeKey = "unsupportedMediaType";
            break;
        default:
            statusCodeKey = "defaultPage";
            break;
        }
        return statusCodeKey;
    }


    /***
     *
     * @param resp {@link HttpServletResponse}
     * Return Error Response.
     */
    public static void handleError(HttpServletRequest req, HttpServletResponse resp, String serverUrl,
            String platform, ProxyResponse proxyResponse) throws IOException {

        HttpSession httpSession = req.getSession(true);
        Gson gson = new Gson();
        if (proxyResponse == null){
            proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            proxyResponse.setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil
                    .getStatusKey(HandlerConstants.INTERNAL_ERROR_CODE));
        }
        if (platform == null){
            platform = "default";
        }

        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());

        if (httpSession != null) {
            JsonObject uiConfig = (JsonObject) httpSession.getAttribute(HandlerConstants.UI_CONFIG_KEY);
            if (uiConfig == null){
                proxyResponse.setUrl(serverUrl + "/" + platform + HandlerConstants.DEFAULT_ERROR_CALLBACK);
            } else{
                proxyResponse.setUrl(serverUrl + uiConfig.get(HandlerConstants.ERROR_CALLBACK_KEY).getAsJsonObject()
                        .get(proxyResponse.getExecutorResponse().split(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)[1])
                        .getAsString());
            }
        } else {
            proxyResponse.setUrl(serverUrl + "/" + platform + HandlerConstants.DEFAULT_ERROR_CALLBACK);
        }

        proxyResponse.setExecutorResponse(null);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(proxyResponse));
        }
    }

    /***
     *
     * @param resp {@link HttpServletResponse}
     * Return Success Response.
     */
    public static void handleSuccess(HttpServletRequest req, HttpServletResponse resp, String serverUrl,
            String platform, ProxyResponse proxyResponse) throws IOException {
        if (proxyResponse == null){
            handleError(req, resp, serverUrl, platform, null);
            return;
        }

        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());

        JSONObject response = new JSONObject();
        String redirectUrl = proxyResponse.getUrl();
        String responseData = proxyResponse.getData();

        if (!StringUtils.isEmpty(redirectUrl)){
            response.put("url", redirectUrl);
        }
        if (!StringUtils.isEmpty(responseData)){
            try {
                JSONObject responseDataJsonObj = new JSONObject(responseData);
                response.put("data", responseDataJsonObj);
            } catch (JSONException e) {
                log.debug("Response data is not valid json string");
                response.put("data", responseData);
            }
        }

        try (PrintWriter writer = resp.getWriter()) {
            writer.write(response.toString());
        }
    }

}
