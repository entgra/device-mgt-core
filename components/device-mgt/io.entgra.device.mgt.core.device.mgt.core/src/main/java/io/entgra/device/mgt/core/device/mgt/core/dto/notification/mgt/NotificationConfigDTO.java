package io.entgra.device.mgt.core.device.mgt.core.dto.notification.mgt;

import java.util.List;

public class NotificationConfigDTO {
    private String operationId;
    private String configType;
    private Recipients recipients;
    private boolean acknowledgable;


    public static class Recipients {
        private List<String> roles;
        private List<String> users;

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }

        public List<String> getUsers() {
            return users;
        }

        public void setUsers(List<String> users) {
            this.users = users;
        }

        @Override
        public String toString() {
            return "Recipients{" +
                    "roles=" + roles +
                    ", users=" + users +
                    '}';
        }
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public Recipients getRecipients() {
        return recipients;
    }

    public void setRecipients(Recipients recipients) {
        this.recipients = recipients;
    }

    public boolean isAcknowledgable() {
        return acknowledgable;
    }

    public void setAcknowledgable(boolean acknowledgable) {
        this.acknowledgable = acknowledgable;
    }

    @Override
    public String toString() {
        return "NotificationConfigDTO{" +
                "operationId='" + operationId + '\'' +
                ", configType='" + configType + '\'' +
                ", recipients=" + recipients +
                ", acknowledgable=" + acknowledgable +
                '}';
    }
}
