package io.entgra.device.mgt.core.device.mgt.core.dto.notification.mgt;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "BasicUserInfoWrapper", description = "This contains basic details of a set of users that matches " +
        "a given criteria as a collection and a message if there's any.")
import java.util.List;

public class NotificationConfigWrapper{
    private List<NotificationConfigDTO> configurations;

    // Getters and setters
    public List<NotificationConfigDTO> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<NotificationConfigDTO> configurations) {
        this.configurations = configurations;
    }

    @ApiModelProperty(
            name="",
            value="",
            required = true
    )

    private notificationConfig notificationConfig;

}
