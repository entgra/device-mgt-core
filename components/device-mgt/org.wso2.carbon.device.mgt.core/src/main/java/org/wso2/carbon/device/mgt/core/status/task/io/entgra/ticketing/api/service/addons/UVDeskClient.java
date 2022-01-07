/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.api.service.addons;

import com.google.common.net.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.TicketingClient;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.beans.TicketingClientDeviceInfo;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.config.TicketingGateway;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.core.config.TicketingConfigurationManager;

import java.io.IOException;

import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.TicketingHandlerConstants;


public class UVDeskClient implements TicketingClient {
    private static final Log log = LogFactory.getLog(UVDeskClient.class);

    public String createIssue(TicketingClientDeviceInfo deviceInfo) throws IOException {
        String responseBody="Something went wrong";
        TicketingGateway ticketingGateway = getTicketingGateway(TicketingHandlerConstants.GATEWAY_NAME);

        //Retrieve the properties in the Ticketing Gateway by passing the property name
        String fromName = ticketingGateway.getPropertyByName(TicketingHandlerConstants.FROM_NAME).getValue();
        String fromEmail = ticketingGateway.getPropertyByName(TicketingHandlerConstants.FROM_EMAIL).getValue();

        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("message",deviceInfo.getMessage())
                .addFormDataPart("actAsType","customer")
                .addFormDataPart("name",fromName)
                .addFormDataPart("subject","IoT Device is in "+deviceInfo.getSubject()+" state")
                .addFormDataPart("from",fromEmail)
                .build();

        responseBody = sendToUvDesk(body);
        return responseBody;
    }

    private String sendToUvDesk (RequestBody body) throws IOException{

        //Retrieve the Ticketing Gateway by passing the Gateway name
        TicketingGateway ticketingGateway = getTicketingGateway(TicketingHandlerConstants.GATEWAY_NAME);

        //Retrieve the properties in the Ticketing Gateway by passing the property name
        String endpoint = ticketingGateway.getPropertyByName(TicketingHandlerConstants.ENDPOINT).getValue();
        String authorization = ticketingGateway.getPropertyByName(TicketingHandlerConstants.AUTHORIZATION).getValue();
        String authorizationKey = ticketingGateway.getPropertyByName(TicketingHandlerConstants.AUTHORIZATION_KEY).getValue();

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(endpoint)
                .method("POST", body)
                .addHeader(authorization, authorizationKey)
                .build();
        okhttp3.Response response = client.newCall(request).execute();

        return String.valueOf(response);
    }
    
    private TicketingGateway getTicketingGateway(String gatewayName){
        return TicketingConfigurationManager.getInstance().getTicketingConfig().getTicketingGateway(gatewayName);
    }
}
