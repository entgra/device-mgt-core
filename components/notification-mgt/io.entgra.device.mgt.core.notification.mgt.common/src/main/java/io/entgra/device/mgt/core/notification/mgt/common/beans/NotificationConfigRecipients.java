package io.entgra.device.mgt.core.notification.mgt.common.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NotificationConfigRecipients {
    private List<String> roles = new ArrayList<>(List.of("admin"));
    private List<String> users = new ArrayList<>(List.of("admin"));

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = (roles != null) ? roles : new ArrayList<>();
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = (users != null) ? users : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Recipients{" +
                "roles=" + Objects.toString(roles, "[]") +
                ", users=" + Objects.toString(users, "[]") +
                '}';
    }
}
