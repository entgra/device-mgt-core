package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.api.service.addons;

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
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.TicketingClient;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.beans.TicketingClientDeviceInfo;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.config.TicketingGateway;
import org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.core.config.TicketingConfigurationManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class UVDeskClient implements TicketingClient {
    private static final Log log = LogFactory.getLog(UVDeskClient.class);

    public String createIssue(TicketingClientDeviceInfo deviceInfo){
        String responseBody="Something went wrong";
        //Retrieve the default (isDefault=true) Ticketing Gateway in the Ticketing configuration in ticketing-config.xml
        TicketingGateway ticketingGateway = TicketingConfigurationManager.getInstance().getTicketingConfig().getDefaultTicketingGateway();

        //Retrieve the Ticketing Gateway by passing the Gateway name
        ticketingGateway = TicketingConfigurationManager.getInstance().getTicketingConfig().getTicketingGateway("sample");

        //Retrieve the properties in the Ticketing Gateway by passing the property name
        String fromName = ticketingGateway.getPropertyByName("from-name").getValue();
        String fromEmail = ticketingGateway.getPropertyByName("from-email").getValue();

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            List<NameValuePair> form = new ArrayList<>();
            String message="Hi, \r\nThe IoT device bearing "+deviceInfo.getDeviceIdentifier()+ " device dentifier ";
            message+="is in "+deviceInfo.getSubject()+" state. \r\n\n";

            message+="Device Type - "+deviceInfo.getDeviceType()+", \r\n";
            message+="Device Identifier - "+deviceInfo.getDeviceIdentifier()+", \r\n";
            message+="Device Id - "+deviceInfo.getDeviceId()+", \r\n";
            message+="Device Name - "+deviceInfo.getDeviceName()+", \r\n";

            form.add(new BasicNameValuePair("message", message));
            form.add(new BasicNameValuePair("actAsType", "customer"));
            form.add(new BasicNameValuePair("name", fromName));
            form.add(new BasicNameValuePair("subject", "IoT Device is in "+deviceInfo.getSubject()+" state"));
            form.add(new BasicNameValuePair("from", fromEmail));

            responseBody = sendToUvDesk(form);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    private String sendToUvDesk (List<NameValuePair> form) throws IOException{

        //Retrieve the default (isDefault=true) Ticketing Gateway in the Ticketing configuration in ticketing-config.xml
        TicketingGateway ticketingGateway = TicketingConfigurationManager.getInstance().getTicketingConfig().getDefaultTicketingGateway();
        //Retrieve the Ticketing Gateway by passing the Gateway name
        ticketingGateway = TicketingConfigurationManager.getInstance().getTicketingConfig().getTicketingGateway("sample");

        //Retrieve the properties in the Ticketing Gateway by passing the property name
        String endpoint = ticketingGateway.getPropertyByName("create-issue-api-endpoint").getValue();
        String authorization = ticketingGateway.getPropertyByName("authorization").getValue();
        String authorizationKey = ticketingGateway.getPropertyByName("authorization-key").getValue();

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);
            HttpPost httpPost = new HttpPost(endpoint);
            httpPost.setHeader(authorization, authorizationKey);
            httpPost.setEntity(entity);

            // Create a custom response handler
            ResponseHandler responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity responseEntity = response.getEntity();
                    return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            return (String) httpclient.execute(httpPost, responseHandler);
        }
    }
}
