package io.entgra.device.mgt.core.device.mgt.core.notification.mgt;

import java.util.List;

public class NotificationConfig {
    private String operationID;
    private String Configype;
    private Recipients recipients;
    private boolean acknowledgable;

    public static class Recipients {
        private List<String> roles;
        private List<String> users;
    }

    public String getOperationID() {
        return operationID;
    }

    public void setOperationID(String operationID) {
        this.operationID = operationID;
    }

    public String getConfigType() {
        return Configype;
    }

    public void setConfigType(String type) {
        this.Configype = Configype;
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
}
