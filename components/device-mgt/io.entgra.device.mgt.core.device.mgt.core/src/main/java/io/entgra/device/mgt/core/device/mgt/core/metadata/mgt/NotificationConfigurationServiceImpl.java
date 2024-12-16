package io.entgra.device.mgt.core.device.mgt.core.metadata.mgt;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.util.MetadataConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.List;
import com.google.gson.JsonElement;
import java.util.ArrayList;


public class NotificationConfigurationServiceImpl {
    private static final Log log = LogFactory.getLog(NotificationConfigurationServiceImpl.class);

    private final MetadataDAO metadataDAO;

    public NotificationConfigurationServiceImpl(MetadataDAO metadataDAO) {
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
    }


    public void addNotificationConfigContext(int tenantId) throws MetadataManagementException{
        try{
            MetadataManagementDAOFactory.beginTransaction();
            if(!metadataDAO.isExist(tenantId, MetadataConstants.NOTIFICATION_CONFIG_META_KEY)){
                metadataDAO.addMetadata(tenantId, constructNotificationConfigContext())
            }

        }
        catch (MetadataManagementDAOException e){
            MetadataManagementDAOFactory.rollbackTransaction();
            String message = "Error adding notification configuration context";
            log.error(message, e);
        }null
    }

    /**
     * Constructs a Metadata object for Notification Configuration.
     *
     * @param configurations A list of configuration objects,
     *                       where each configuration contains details such as type, recipients,
     *                       acknowledgability, and operation ID.
     * @return A Metadata object containing the serialized notification configuration.
     */

    public List<Metadata> constructNotificationConfigContext(List<JsonObject> configurations) {
        List<Metadata> metadataList = new ArrayList<>();

        for (JsonObject config : configurations) {
            String type = config.get("type").getAsString();
            JsonObject recipientsObject = config.getAsJsonObject("recipients");

            List<String> roles = new ArrayList<>();
            List<String> users = new ArrayList<>();

            if (recipientsObject.has("roles")) {
                JsonArray rolesArray = recipientsObject.getAsJsonArray("roles");
                for (JsonElement role : rolesArray) {
                    roles.add(role.getAsString());
                }
            }

            if (recipientsObject.has("users")) {
                JsonArray usersArray = recipientsObject.getAsJsonArray("users");
                for (JsonElement user : usersArray) {
                    users.add(user.getAsString());
                }
            }

            boolean acknowledgable = config.get("acknowledgable").getAsBoolean();
            String operationID = config.get("operationID").getAsString();

            JsonObject configObject = new JsonObject();
            configObject.addProperty("type", type);

            JsonObject recipientsJsonObject = new JsonObject();
            if (!roles.isEmpty()) {
                JsonArray rolesArray = new JsonArray();
                for (String role : roles) {
                    rolesArray.add(role);
                }
                recipientsJsonObject.add("roles", rolesArray);
            }
            if (!users.isEmpty()) {
                JsonArray usersArray = new JsonArray();
                for (String user : users) {
                    usersArray.add(user);
                }
                recipientsJsonObject.add("users", usersArray);
            }
            configObject.add("recipients", recipientsJsonObject);
            configObject.addProperty("acknowledgable", acknowledgable);
            configObject.addProperty("operationID", operationID);

            JsonObject notificationConfigObject = new JsonObject();
            notificationConfigObject.add("config", configObject);

            Metadata configMetadata = new Metadata();
            configMetadata.setMetaKey(MetadataConstants.NOTIFICATION_CONFIG_META_KEY);
            configMetadata.setMetaValue(notificationConfigObject.toString());

            metadataList.add(configMetadata);
        }

        return metadataList;
    }


}
