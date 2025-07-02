/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
import io.entgra.device.mgt.core.ui.request.interceptor.beans.ProxyResponse;
import io.entgra.device.mgt.core.ui.request.interceptor.exceptions.TenantInvokerException;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerConstants;
import io.entgra.device.mgt.core.ui.request.interceptor.util.HandlerUtil;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

@MultipartConfig
@WebServlet(
        name = "TenantRequestHandlerServlet",
        description = "This servlet intercepts the api requests initiated from the user interface and validate before" +
                      " forwarding to the backend with the tenant domain appended to the context path",
        urlPatterns = {
                "/tenant/*"
        }
)
public class TenantInvokerHandler extends HttpServlet {
    private static final Log log = LogFactory.getLog(TenantInvokerHandler.class);
    private static final long serialVersionUID = 7152993491562766958L;
    private static AuthData authData;
    private static AuthData tenantContextAuthData;
    private static String apiEndpoint;
    private static String kmManagerUrl;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                ClassicHttpRequest postRequest = createTenantAwareHttpRequest(req, HandlerConstants.HTTP_METHOD_POST);
                ProxyResponse proxyResponse = HandlerUtil.execute(postRequest);

                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = HandlerUtil.retryRequestWithRefreshedToken(req, postRequest, kmManagerUrl,
                            HandlerUtil.isTenantAwareUsername(getUsernameFromTenantAuthData()));
                    if (!HandlerUtil.isResponseSuccessful(proxyResponse)) {
                        HandlerUtil.handleError(resp, proxyResponse);
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the POST API endpoint.");
                    HandlerUtil.handleError(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (FileUploadException e) {
            log.error("Error occurred when processing Multipart POST request.", e);
        } catch (IOException e) {
            log.error("Error occurred when processing POST request.", e);
        } catch (TenantInvokerException e) {
            log.error("Error occurred when generating POST request.", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                ClassicHttpRequest getRequest = createTenantAwareHttpRequest(req, HandlerConstants.HTTP_METHOD_GET);
                        ProxyResponse proxyResponse = HandlerUtil.execute(getRequest);
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = HandlerUtil.retryRequestWithRefreshedToken(req, getRequest, kmManagerUrl,
                            HandlerUtil.isTenantAwareUsername(getUsernameFromTenantAuthData()));
                    if (!HandlerUtil.isResponseSuccessful(proxyResponse)) {
                        HandlerUtil.handleError(resp, proxyResponse);
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    if (proxyResponse.getCode() == HttpStatus.SC_UNAUTHORIZED) {
                        proxyResponse = HandlerUtil.retryRequestWithRefreshedToken(req, getRequest, kmManagerUrl, true);
                        if (!HandlerUtil.isResponseSuccessful(proxyResponse)) {
                            HandlerUtil.handleError(resp, proxyResponse);
                            return;
                        }
                    } else {
                        log.error("Error occurred while invoking the GET API endpoint.");
                        HandlerUtil.handleError(resp, proxyResponse);
                        return;
                    }
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (IOException | FileUploadException e) {
            log.error("Error occurred when processing GET request.", e);
        } catch (TenantInvokerException e) {
            log.error("Error occurred when generating GET request.", e);
        }
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                ClassicHttpRequest headRequest = createTenantAwareHttpRequest(req, HandlerConstants.HTTP_METHOD_HEAD);
                        ProxyResponse proxyResponse = HandlerUtil.execute(headRequest);
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = HandlerUtil.retryRequestWithRefreshedToken(req, headRequest, kmManagerUrl,
                            HandlerUtil.isTenantAwareUsername(getUsernameFromTenantAuthData()));
                    if (!HandlerUtil.isResponseSuccessful(proxyResponse)) {
                        HandlerUtil.handleError(resp, proxyResponse);
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the HEAD API endpoint.");
                    HandlerUtil.handleError(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (IOException | FileUploadException e) {
            log.error("Error occurred when processing HEAD request.", e);
        } catch (TenantInvokerException e) {
            log.error("Error occurred when generating HEAD request.", e);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (validateRequest(req, resp)) {
                ClassicHttpRequest putRequest = createTenantAwareHttpRequest(req, HandlerConstants.HTTP_METHOD_PUT);
                        ProxyResponse proxyResponse = HandlerUtil.execute(putRequest);

                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = HandlerUtil.retryRequestWithRefreshedToken(req, putRequest, kmManagerUrl,
                            HandlerUtil.isTenantAwareUsername(getUsernameFromTenantAuthData()));
                    if (!HandlerUtil.isResponseSuccessful(proxyResponse)) {
                        HandlerUtil.handleError(resp, proxyResponse);
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the PUT API endpoint.");
                    HandlerUtil.handleError(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (FileUploadException e) {
            log.error("Error occurred when processing Multipart PUT request.", e);
        } catch (IOException e) {
            log.error("Error occurred when processing PUT request.", e);
        } catch (TenantInvokerException e) {
            log.error("Error occurred when generating PUT request.", e);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String jsonPayload = null;
            if (req.getContentLength() > 0) {
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                try (BufferedReader reader = req.getReader()) {
                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
                    }
                }
                jsonPayload = jsonBuilder.toString();
            }
            if (validateRequest(req, resp)) {
                ClassicHttpRequest deleteRequest = createTenantAwareHttpRequest(req, HandlerConstants.HTTP_METHOD_DELETE);
                if (jsonPayload != null && !jsonPayload.isEmpty()) {
                    StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
                    deleteRequest.setEntity(entity);
                }
                ProxyResponse proxyResponse = HandlerUtil.execute(deleteRequest);
                if (HandlerConstants.TOKEN_IS_EXPIRED.equals(proxyResponse.getExecutorResponse())) {
                    proxyResponse = HandlerUtil.retryRequestWithRefreshedToken(req, deleteRequest, kmManagerUrl,
                            HandlerUtil.isTenantAwareUsername(getUsernameFromTenantAuthData()));
                    if (!HandlerUtil.isResponseSuccessful(proxyResponse)) {
                        HandlerUtil.handleError(resp, proxyResponse);
                        return;
                    }
                }
                if (proxyResponse.getExecutorResponse().contains(HandlerConstants.EXECUTOR_EXCEPTION_PREFIX)) {
                    log.error("Error occurred while invoking the DELETE API endpoint.");
                    HandlerUtil.handleError(resp, proxyResponse);
                    return;
                }
                HandlerUtil.handleSuccess(resp, proxyResponse);
            }
        } catch (IOException | FileUploadException e) {
            log.error("Error occurred when processing DELETE request.", e);
        } catch (TenantInvokerException e) {
            log.error("Error occurred when generating DELETE request.", e);
        }
    }

    /***
     * Validates the incoming request.
     *
     * @param req {@link HttpServletRequest}
     * @param resp {@link HttpServletResponse}
     * @return If request is a valid one, returns TRUE, otherwise return FALSE
     * @throws IOException If and error occurs while witting error response to client side
     */
    private static boolean validateRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        apiEndpoint = req.getScheme() + HandlerConstants.SCHEME_SEPARATOR +
                System.getProperty(HandlerConstants.IOT_GW_HOST_ENV_VAR) + HandlerConstants.COLON +
                HandlerUtil.getGatewayPort(req.getScheme());

        kmManagerUrl = HandlerUtil.getKeyManagerUrl(req.getScheme());

        if (HandlerConstants.REPORTS.equalsIgnoreCase(req.getHeader(HandlerConstants.APP_NAME))){
            apiEndpoint = System.getProperty("iot.reporting.webapp.host");
            if (StringUtils.isBlank(apiEndpoint)){
                log.error("Reporting Endpoint is not defined in the iot-server.sh properly.");
                HandlerUtil.handleError(resp, HttpStatus.SC_INTERNAL_SERVER_ERROR);
                return false;
            }
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            log.error("Unauthorized, You are not logged in. Please log in to the portal");
            HandlerUtil.handleError(resp, HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        authData = (AuthData) session.getAttribute(HandlerConstants.SESSION_AUTH_DATA_KEY);
        if (authData == null) {
            log.error("Unauthorized, Access token not found in the current session");
            HandlerUtil.handleError(resp, HttpStatus.SC_UNAUTHORIZED);
            return false;
        }
        if (authData.getUsername() == null) {
            log.error("Unauthorized, Username not found in the current session");
            HandlerUtil.handleError(resp, HttpStatus.SC_UNAUTHORIZED);
            return false;
        }

        tenantContextAuthData = (AuthData) session.getAttribute("tenantContextAuthInfo");
        if (HandlerUtil.isTenantAwareUsername(authData.getUsername())) {
            if (tenantContextAuthData == null) {
                log.error("Unauthorized, Tenant Context auth info not found in the current session");
                HandlerUtil.handleError(resp, HttpStatus.SC_UNAUTHORIZED);
                return false;
            }
            if (!HandlerUtil.isTenantAwareUsername(tenantContextAuthData.getUsername())) {
                log.error("Unauthorized, Username does not have a tenant domain");
                HandlerUtil.handleError(resp, HttpStatus.SC_UNAUTHORIZED);
                return false;
            }
        }

        if (req.getMethod() == null) {
            log.error("Bad Request, Request method is empty");
            HandlerUtil.handleError(resp, HttpStatus.SC_BAD_REQUEST);
            return false;
        }
        return true;
    }

    /**
     * Creates the HTTP request with the tenant API context path added if the logged in session user has a valid tenant
     * domain and sets the auth data based on that.
     *
     * @param req {@link HttpServletRequest}
     * @param httpMethod HTTP method of the request
     * @return {@link ClassicHttpRequest} containing the correct method, URL and access token
     * @throws IOException if an error occurred while creating PUT or POST requests
     * @throws FileUploadException if an error occurred while creating PUT or POST requests
     * @throws TenantInvokerException if an error occurred while creating the {@link ClassicHttpRequest}
     */
    private ClassicHttpRequest createTenantAwareHttpRequest(HttpServletRequest req, String httpMethod)
            throws IOException, FileUploadException, TenantInvokerException {

        ClassicHttpRequest classicHttpRequest = null;
        String username = getUsernameFromTenantAuthData();
        String tenantDomain = HandlerUtil.getTenantDomainFromUsername(username);

        boolean isPostOrPutRequest = HandlerConstants.HTTP_METHOD_POST.equals(httpMethod) ||
                HandlerConstants.HTTP_METHOD_PUT.equals(httpMethod);

        classicHttpRequest = setHttpMethod(req, classicHttpRequest, httpMethod, tenantDomain);
        if (classicHttpRequest != null) {
            if (isPostOrPutRequest) {
                HandlerUtil.generateRequestEntity(req, classicHttpRequest);
            } else {
                HandlerUtil.copyRequestHeaders(req, classicHttpRequest, false);
            }

            if (StringUtils.isNotBlank(tenantDomain)) {
                classicHttpRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER +
                        tenantContextAuthData.getAccessToken());
            } else {
                classicHttpRequest.setHeader(HttpHeaders.AUTHORIZATION, HandlerConstants.BEARER +
                        authData.getAccessToken());
            }
            return classicHttpRequest;
        } else {
            String msg = "Error occurred while creating HTTP request.";
            log.error(msg);
            throw new TenantInvokerException(msg);
        }
    }

    /**
     * Creates the HTTP request based on the HTTP method using {@link ClassicRequestBuilder}
     *
     * @param req {@link HttpServletRequest}
     * @param classicHttpRequest {@link ClassicHttpRequest}
     * @param httpMethod HTTP method of the request
     * @param tenantDomain tenant domain of the session user
     * @return {@link ClassicHttpRequest} containing the HTTP method and URL
     */
    private static ClassicHttpRequest setHttpMethod(HttpServletRequest req, ClassicHttpRequest classicHttpRequest,
                                                    String httpMethod, String tenantDomain) {
        switch (httpMethod) {
            case HandlerConstants.HTTP_METHOD_POST:
                classicHttpRequest = ClassicRequestBuilder.post(HandlerUtil.generateTenantBackendRequestURL(
                        req, apiEndpoint, tenantDomain)).build();
                break;
            case HandlerConstants.HTTP_METHOD_PUT:
                classicHttpRequest = ClassicRequestBuilder.put(HandlerUtil.generateTenantBackendRequestURL(
                        req, apiEndpoint, tenantDomain)).build();
                break;
            case HandlerConstants.HTTP_METHOD_DELETE:
                classicHttpRequest = ClassicRequestBuilder.delete(HandlerUtil.generateTenantBackendRequestURL(
                        req, apiEndpoint, tenantDomain)).build();
                break;
            case HandlerConstants.HTTP_METHOD_GET:
                classicHttpRequest = ClassicRequestBuilder.get(HandlerUtil.generateTenantBackendRequestURL(
                        req, apiEndpoint, tenantDomain)).build();
                break;
            case HandlerConstants.HTTP_METHOD_HEAD:
                classicHttpRequest = ClassicRequestBuilder.head(HandlerUtil.generateTenantBackendRequestURL(
                        req, apiEndpoint, tenantDomain)).build();
                break;
        }
        return classicHttpRequest;
    }

    /**
     * Gets the username from the auth session data
     *
     * @return username from the {@link AuthData}
     */
    private String getUsernameFromTenantAuthData() {
        return tenantContextAuthData != null ? tenantContextAuthData.getUsername() : null;
    }
}
