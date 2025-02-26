/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.core.dto.notification.mgt;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class NotificationConfigDTO {

    @JsonProperty("operationId")
    private String operationId;

    @JsonProperty("configType")
    private String configType;

    @JsonProperty("recipients")
    private Recipients recipients;

    @JsonProperty("acknowledgable")
    private boolean acknowledgable;

    public static class Recipients {

        @JsonProperty("roles")
        private List<String> roles;

        @JsonProperty("users")
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
