package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.NotificationConfigurationService;
import io.entgra.device.mgt.core.device.mgt.core.dto.notification.mgt.NotificationConfigDTO;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.NotificationConfigServiceImpl;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.util.List;

public class NotificationConfigurationServiceImpl implements NotificationConfigurationService {
    private static final Log log = LogFactory.getLog(NotificationConfigurationServiceImpl.class);

    MetadataDAO metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
    NotificationConfigServiceImpl notificationConfigService = new NotificationConfigServiceImpl(metadataDAO);

    public Response createNotificationConfig(List<NotificationConfigDTO> configurations) {
        try {
            if (configurations == null || configurations.isEmpty()) {
                String msg = "Received empty or null configuration list.";
                log.error(msg);
                return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
            }
            //More validations need to be added
            for (NotificationConfigDTO config : configurations) {
                if (config.getConfigType() == null || config.getRecipients() == null) {
                    String msg = "Invalid configuration object: " + config;
                    log.error(msg);
                    return Response.status(Response.Status.BAD_REQUEST).entity(msg).build();
                }
                log.info("Processing configuration: " + config.toString());
                int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                notificationConfigService.addNotificationConfigContext(tenantId, configurations);
            }
            return Response.status(Response.Status.CREATED).entity("Notification configuration(s) created successfully.").build();
        } catch (ProcessingException e) {
            String msg = "Error occurred while processing notification configuration(s).";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        } catch (Exception e) {
            String msg = "Unexpected error occurred while creating notification configurations.";
            log.error(msg, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(msg).build();
        }
    }
}
