package io.entgra.device.mgt.core.notification.mgt.common.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.Duration;
import java.time.LocalDateTime;

@ApiModel(value = "NotificationConfig", description = "Notification Configurations")
public class NotificationConfig {

//    private static int idCounter = 1;
//
//    public NotificationConfig() {
//        this.configId = idCounter++; // Assign unique ID when object is created
//    }

    @ApiModelProperty(name = "id", value = "The ID of the notification configuration.", required = true)
    private int configId;

    @ApiModelProperty(name = "name", value = "The name of the notification configuration.", required = true)
    private String configName;


    @ApiModelProperty(name = "description", value = "The description of the notification configuration.", required = true)
    private String description;


    public enum archiveType{
        DLB,ELK
    }

    @ApiModelProperty(name = "opcode", value = "The operation or task code associated with the notification.", required = true)
    private String code;

    @ApiModelProperty(name = "priority", value = "The priority of the notification configuration.", required = true)
    private int priority = 1;

    @ApiModelProperty(name = "archiveAfter", value = "The duration after which notifications are archived.", required = true)
    private long archiveAfter;

    @ApiModelProperty(name = "recipients", value = "The recipients of the notification.", required = true)
    private NotificationConfigRecipients recipients;

    @ApiModelProperty(name = "configuredBy", value = "Information about the person who configured the notification.", required = true)
    private ConfiguredBy configuredBy;

    @ApiModelProperty(name = "notificationSettings", value = "The notification settings.", required = true)
    private NotificationConfigurationSettings notificationConfigSettings;

    @ApiModelProperty(name = "Critical criteria", value = "The critical criteria for notification configurations", required = false)
    private NotificationConfigCriticalCriteria criticalCriteria;

    public enum configType {
        OPERATION_RELATED,
        TASK_RELATED
    }

    public NotificationConfigCriticalCriteria getCriticalCriteria() {
        return criticalCriteria;
    }

    public void setCriticalCriteria(NotificationConfigCriticalCriteria criticalCriteria) {
        this.criticalCriteria = criticalCriteria;
    }

//    @ApiModelProperty(name = "meta data", value = "Meta data in notification configurations.", required = false)
//    private Metadata metaData;

    // Getters and Setters

    public static class ConfiguredBy {
        @JsonProperty("user")
        private String user;

        @JsonProperty("lastModifiedAt")
        private LocalDateTime lastModifiedAt;
    }

    public int getId() {
        return configId;
    }

    public void setId(int id) {
        this.configId = id;
    }

    public void setName(String name) {
        this.configName = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NotificationConfigRecipients getRecipients() {
        return recipients;
    }

    public void setRecipients(NotificationConfigRecipients recipients) {
        this.recipients = recipients;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Duration getArchiveAfter() {
        return Duration.ofSeconds(archiveAfter);
    }

    public void setArchiveAfter(Duration archiveAfter) {
        this.archiveAfter = archiveAfter.getSeconds();
    }

    public ConfiguredBy getConfiguredBy() {
        return configuredBy;
    }

    public void setConfiguredBy(ConfiguredBy configuredBy) {
        this.configuredBy = configuredBy;
    }

}
