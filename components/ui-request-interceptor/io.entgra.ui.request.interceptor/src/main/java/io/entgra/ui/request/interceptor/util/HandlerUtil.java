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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import io.entgra.ui.request.interceptor.beans.ProxyResponse;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

public class HandlerUtil {

    private static final Log log = LogFactory.getLog(HandlerUtil.class);

    /***
     *
     * @param httpRequest - httpMethod e.g:- HttpPost, HttpGet
     * @return response as string
     * @throws IOException IO exception returns if error occurs when executing the httpMethod
     */
    public static ProxyResponse execute(HttpRequestBase httpRequest) throws IOException {
        try (CloseableHttpClient client = getHttpClient()) {
            HttpResponse response = client.execute(httpRequest);
            ProxyResponse proxyResponse = new ProxyResponse();

            if (response == null) {
                log.error("Received null response for http request : " + httpRequest.getMethod() + " " + httpRequest
                        .getURI().toString());
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
                            log.error(
                                    "Received " + statusCode + " response for http request : " + httpRequest.getMethod()
                                            + " " + httpRequest.getURI().toString() + ". Error message: " + jsonString);
                            proxyResponse.setCode(statusCode);
                            proxyResponse.setData(jsonString);
                            proxyResponse.setExecutorResponse(
                                    HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + getStatusKey(statusCode));
                            return proxyResponse;
                        }
                    }
                    log.error("Received " + statusCode +
                            " response for http request : " + httpRequest.getMethod() + " " + httpRequest.getURI()
                            .toString() + ". Error message: " + jsonString);
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
    public static String getStatusKey(int statusCode) {
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
    public static void handleError(HttpServletResponse resp, ProxyResponse proxyResponse) throws IOException {
        Gson gson = new Gson();
        if (proxyResponse == null) {
            proxyResponse = new ProxyResponse();
            proxyResponse.setCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            proxyResponse.setExecutorResponse(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil
                    .getStatusKey(HandlerConstants.INTERNAL_ERROR_CODE));
        }
        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());

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
    public static void handleSuccess(HttpServletResponse resp, ProxyResponse proxyResponse) throws IOException {
        if (proxyResponse == null) {
            handleError(resp, null);
            return;
        }
        resp.setStatus(proxyResponse.getCode());
        resp.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        resp.setCharacterEncoding(Consts.UTF_8.name());
        JSONObject response = new JSONObject();
        String responseData = proxyResponse.getData();

        if (!StringUtils.isEmpty(responseData)) {
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

    /**
     * Get gateway port according to request received scheme
     *
     * @param scheme https or https
     * @return {@link String} gateway port
     */
    public static String getGatewayPort(String scheme) {
        String gatewayPort = System.getProperty(HandlerConstants.IOT_GW_HTTPS_PORT_ENV_VAR);
        if (HandlerConstants.HTTP_PROTOCOL.equals(scheme)) {
            gatewayPort = System.getProperty(HandlerConstants.IOT_GW_HTTP_PORT_ENV_VAR);
        }
        return gatewayPort;
    }

    /**
     * Get core port according to request received scheme
     *
     * @param scheme https or https
     * @return {@link String} gateway port
     */
    public static String getCorePort(String scheme) {
        String productCorePort = System.getProperty(HandlerConstants.IOT_CORE_HTTPS_PORT_ENV_VAR);
        if (HandlerConstants.HTTP_PROTOCOL.equals(scheme)) {
            productCorePort = System.getProperty(HandlerConstants.IOT_CORE_HTTP_PORT_ENV_VAR);
        }
        return productCorePort;
    }

    /**
     * Retrieve Http client based on hostname verification.
     *
     * @return {@link CloseableHttpClient} http client
     */
    public static CloseableHttpClient getHttpClient() {
        boolean isIgnoreHostnameVerification = Boolean.parseBoolean(System.
                getProperty("org.wso2.ignoreHostnameVerification"));
        if (isIgnoreHostnameVerification) {
            return HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
        } else {
            return HttpClients.createDefault();
        }
    }

    /**
     * Send UnAuthorized Response to the user
     *
     * @param resp HttpServletResponse object
     */
    public static void sendUnAuthorizeResponse(HttpServletResponse resp)
            throws IOException {
        ProxyResponse proxyResponse = new ProxyResponse();
        proxyResponse.setCode(HttpStatus.SC_UNAUTHORIZED);
        proxyResponse.setExecutorResponse(
                HandlerConstants.EXECUTOR_EXCEPTION_PREFIX + HandlerUtil.getStatusKey(HttpStatus.SC_UNAUTHORIZED));
        handleError(resp, proxyResponse);
    }

    /**
     * Generates the target URL for the proxy request.
     *
     * @param req incoming {@link HttpServletRequest}
     * @param apiEndpoint API Endpoint URL
     * @return Target URL
     */
    public static String generateBackendRequestURL(HttpServletRequest req, String apiEndpoint) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(apiEndpoint).append(HandlerConstants.API_COMMON_CONTEXT)
                .append(req.getPathInfo().replace(" ", "%20"));
        if (StringUtils.isNotEmpty(req.getQueryString())) {
            urlBuilder.append("?").append(req.getQueryString());
        }
        return urlBuilder.toString();
    }

    /***
     * Constructs the application registration payload for DCR.
     *
     * @param tags - tags which are retrieved by reading app manager configuration
     * @param username - username provided from login form or admin username
     * @param password - password provided from login form or admin password
     * @return {@link StringEntity} of the payload to create the client application
     */
    public static StringEntity constructAppRegPayload(JsonArray tags, String appName, String username, String password) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(HandlerConstants.APP_NAME_KEY, appName);
        jsonObject.addProperty(HandlerConstants.USERNAME, username);
        jsonObject.addProperty(HandlerConstants.PASSWORD, password);
        jsonObject.addProperty(HandlerConstants.IS_ALLOWED_TO_ALL_DOMAINS_KEY, "false");
        jsonObject.add(HandlerConstants.TAGS_KEY, tags);
        String payload = jsonObject.toString();
        return new StringEntity(payload, ContentType.APPLICATION_JSON);
    }

    /***
     * Retrieves UI configuration and returns as Json.
     *
     * @param uiConfigUrl - UI configurations endpoint URL
     * @param gatewayUrl - gateway endpoint URL
     * @param httpSession - current active HttpSession
     * @param resp - HttpServletResponse
     * @return {@link JsonObject} of UI configurations
     */
    public static JsonObject getUIConfigAndPersistInSession(String uiConfigUrl, String gatewayUrl, HttpSession httpSession,
                                                      HttpServletResponse resp) throws IOException {
        HttpGet uiConfigEndpoint = new HttpGet(uiConfigUrl);
        ProxyResponse uiConfigResponse = HandlerUtil.execute(uiConfigEndpoint);
        String executorResponse = uiConfigResponse.getExecutorResponse();
        if (!StringUtils.isEmpty(executorResponse) && executorResponse
                .contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
            log.error("Error occurred while getting UI configurations by invoking " + uiConfigUrl);
            HandlerUtil.handleError(resp, uiConfigResponse);
        }

        if (uiConfigResponse.getData() == null) {
            log.error("UI config retrieval is failed, and didn't find UI configuration for App manager.");
            HandlerUtil.handleError(resp, null);
        }
        JsonParser jsonParser = new JsonParser();

        JsonElement uiConfigJsonElement = jsonParser.parse(uiConfigResponse.getData());
        JsonObject uiConfigJsonObject = null;
        if (uiConfigJsonElement.isJsonObject()) {
            uiConfigJsonObject = uiConfigJsonElement.getAsJsonObject();
            if (uiConfigJsonObject == null) {
                log.error(
                        "Either UI config json element is not an json object or converting rom json element to json object is failed.");
                HandlerUtil.handleError(resp, null);
            }
            httpSession.setAttribute(HandlerConstants.UI_CONFIG_KEY, uiConfigJsonObject);
            httpSession.setAttribute(HandlerConstants.PLATFORM, gatewayUrl);
        }
        return uiConfigJsonObject;
    }

    /***
     * Converts scopes from JsonArray to string with space separated values.
     *
     * @param scopes - scope Json Array and it is retrieved by reading UI config.
     * @return string value of the defined scopes
     */
    public static String getScopeString(JsonArray scopes) {
        if (scopes != null && scopes.size() > 0) {
            StringBuilder builder = new StringBuilder();
            for (JsonElement scope : scopes) {
                String tmpScope = scope.getAsString() + " ";
                builder.append(tmpScope);
            }
            return builder.toString();
        } else {
            return null;
        }
    }

    /***
     * Converts xml file into string.
     *
     * @param xmlFile - xmlFile which needs to be converted into string.
     * @return string value of the xml file.
     */
    public static String xmlToString(File xmlFile) {
        String stringOutput = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            OutputFormat format = new OutputFormat(doc);
            StringWriter stringWriterOutput = new StringWriter();
            XMLSerializer serial = new XMLSerializer(stringWriterOutput, format);
            serial.serialize(doc);
            stringOutput = stringWriterOutput.toString();
        } catch (IOException e) {
            log.error("Error occurred while sending the response into the socket. ", e);
        } catch (ParserConfigurationException e) {
            log.error("Error while creating the document builder.");
        } catch ( SAXException e) {
            log.error("Error while parsing xml file.", e);
        }

        return stringOutput;
    }
}
