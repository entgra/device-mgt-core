package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.api.service.addons;

import com.google.common.net.MediaType;
import okhttp3.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.TicketingClient;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.beans.TicketingClientDeviceInfo;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.config.TicketingGateway;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.core.config.TicketingConfigurationManager;

import java.io.IOException;
import java.util.List;

import static org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.TicketingHandlerConstants.*;


public class UVDeskOkHttpClient implements TicketingClient {
    private static final Log log = LogFactory.getLog(UVDeskOkHttpClient.class);

    public String createIssue(TicketingClientDeviceInfo deviceInfo) throws IOException {
        String responseBody="Something went wrong";
        TicketingGateway ticketingGateway = getTicketingGateway(GATEWAY_NAME);

        //Retrieve the properties in the Ticketing Gateway by passing the property name
        String fromName = ticketingGateway.getPropertyByName(FROM_NAME).getValue();
        String fromEmail = ticketingGateway.getPropertyByName(FROM_EMAIL).getValue();

        String message="Hi, \r\nThe IoT device bearing "+deviceInfo.getDeviceIdentifier()+ " device dentifier ";
        message+="is in "+deviceInfo.getSubject()+" state. \r\n\n";

        message+="Device Type - "+deviceInfo.getDeviceType()+", \r\n";
        message+="Device Identifier - "+deviceInfo.getDeviceIdentifier()+", \r\n";
        message+="Device Id - "+deviceInfo.getDeviceId()+", \r\n";
        message+="Device Name - "+deviceInfo.getDeviceName()+", \r\n";

        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("message",message)
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
        TicketingGateway ticketingGateway = getTicketingGateway(GATEWAY_NAME);

        //Retrieve the properties in the Ticketing Gateway by passing the property name
        String endpoint = ticketingGateway.getPropertyByName(ENDPOINT).getValue();
        String authorization = ticketingGateway.getPropertyByName(AUTHORIZATION).getValue();
        String authorizationKey = ticketingGateway.getPropertyByName(AUTHORIZATION_KEY).getValue();

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(ENDPOINT)
                .method("POST", body)
                .addHeader(AUTHORIZATION, AUTHORIZATION_KEY)
                .build();
        okhttp3.Response response = client.newCall(request).execute();

        return String.valueOf(response);
    }
    
    private TicketingGateway getTicketingGateway(String gatewayName){
        return TicketingConfigurationManager.getInstance().getTicketingConfig().getTicketingGateway(gatewayName);
    }
}
