/*
 *  Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.entgra.carbon.device.mgt.avn.validation.service.impl;

import io.entgra.carbon.device.mgt.avn.validation.exception.DeviceValidationException;
import io.entgra.carbon.device.mgt.avn.validation.service.DeviceValidationService;
import io.entgra.carbon.device.mgt.avn.validation.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Path("/avn")
@Consumes(MediaType.APPLICATION_JSON)
public class DeviceValidationServiceImpl implements DeviceValidationService {

    private static final Log log = LogFactory.getLog(DeviceValidationServiceImpl.class);

    @POST
    @Path("/validate")
    @Override
    public Response validateDevice(@QueryParam("vin") String vin,
                                   @QueryParam("rego") String rego,
                                   @QueryParam("username") String username,
                                   @QueryParam("role") String role) {
        if (vin != null && rego != null) {
            try {
                String validatedVIN = validateRego(rego);
                if (validatedVIN != null && validatedVIN.equals(vin)) {
                    UserStoreManager userStoreManager = getUserStoreManager();
                    List<String> rolesToAdd = new ArrayList<>();
                    List<String> rolesToDelete = new ArrayList<>();
                    rolesToAdd.add(role);
                    userStoreManager.updateRoleListOfUser(username,
                            rolesToDelete.toArray(new String[rolesToDelete.size()]),
                            rolesToAdd.toArray(new String[rolesToAdd.size()]));

                    return Response.status(Response.Status.OK).build();
                }
            } catch (DeviceValidationException e) {
                String msg = "Error occurred while validating device, please check VIN & REGO if they are valid";
                log.error(msg, e);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(msg).build();
            } catch (UserStoreException e) {
                String msg = "Error occurred while retrieving user store";
                log.error(msg, e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(msg).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("VIN / REGO not found").build();
    }

    private String validateRego(String rego) throws DeviceValidationException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("query", "{\n" +
                    "  nevdisPlateSearch_v2(plate: " + '"' + rego + '"' + ", state: VIC) {\n" +
                    "    vin\n" +
                    "    plate {\n" +
                    "      number\n" +
                    "      state\n" +
                    "    }\n" +
                    "    factory {\n" +
                    "      make\n" +
                    "      model\n" +
                    "      series\n" +
                    "      variant\n" +
                    "      buildYear\n" +
                    "      MY\n" +
                    "      body\n" +
                    "      fuel\n" +
                    "      drive\n" +
                    "      cylinders\n" +
                    "      litres\n" +
                    "      transmission\n" +
                    "      seats\n" +
                    "      doors\n" +
                    "    }\n" +
                    "  }\n" +
                    "}");
            HttpPost apiEndpoint = new HttpPost(
                    Constants.VALIDATION_API_URL);
            apiEndpoint.setEntity(new StringEntity(String.valueOf(jsonObj), ContentType.APPLICATION_JSON));
            apiEndpoint.setHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            apiEndpoint.setHeader("Authorization", Constants.VALIDATION_API_TOKEN);
            HttpResponse response = client.execute(apiEndpoint);
            if (response != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String line = null;
                StringBuilder builder = new StringBuilder();
                while ((line = reader.readLine()) != null) {

                    builder.append(line);

                }
                JSONObject jsonObject = new JSONObject(builder.toString());
                JSONObject dataObject = jsonObject.getJSONObject("data");
                JSONArray jsonArray = dataObject.getJSONArray("nevdisPlateSearch_v2");
                JSONObject vObject = (JSONObject) jsonArray.get(0);
                return vObject.getString("vin");

            } else {
                log.error("Response is 'NUll' for the device validation API call.");
                return null;
            }
        } catch (IOException e) {
            throw new DeviceValidationException("Error occured when invoking API. API endpoint: ", e);
        }
    }

    public UserStoreManager getUserStoreManager() throws UserStoreException {
        RealmService realmService;
        UserStoreManager userStoreManager;
        PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        realmService = (RealmService) ctx.getOSGiService(RealmService.class, null);
        if (realmService == null) {
            String msg = "Realm service has not initialized.";
            log.error(msg);
            throw new IllegalStateException(msg);
        }
        int tenantId = ctx.getTenantId();
        userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        return userStoreManager;
    }
}
