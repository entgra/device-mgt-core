package io.entgra.device.mgt.core.application.mgt.common.dto;

import java.util.List;

public class SubscriptionResponseDTO {

    private String UUID;
    private List<GroupSubscriptionDetailDTO> GroupsSubscriptions;
    private List<UserSubscriptionDTO> UserSubscriptions;
    private List<RoleSubscriptionDTO> RolesSubscriptions;
    private List<DeviceOperationDTO> DevicesOperations;

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public List<GroupSubscriptionDetailDTO> getGroupsSubscriptions() {
        return GroupsSubscriptions;
    }

    public void setGroupsSubscriptions(List<GroupSubscriptionDetailDTO> groupsSubscriptions) {
        GroupsSubscriptions = groupsSubscriptions;
    }

    public List<UserSubscriptionDTO> getUserSubscriptions() {
        return UserSubscriptions;
    }

    public void setUserSubscriptions(List<UserSubscriptionDTO> userSubscriptions) {
        UserSubscriptions = userSubscriptions;
    }

    public List<RoleSubscriptionDTO> getRolesSubscriptions() {
        return RolesSubscriptions;
    }

    public void setRolesSubscriptions(List<RoleSubscriptionDTO> rolesSubscriptions) {
        RolesSubscriptions = rolesSubscriptions;
    }

    public List<DeviceOperationDTO> getDevicesOperations() {
        return DevicesOperations;
    }

    public void setDevicesOperations(List<DeviceOperationDTO> devicesOperations) {
        DevicesOperations = devicesOperations;
    }
}
