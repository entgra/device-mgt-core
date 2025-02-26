package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import java.util.List;

public class NotificationConfigRecipients {
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
