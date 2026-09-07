/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.device.mgt.api.jaxrs.beans;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value = "RoleScopeBindingUpdateWrapper",
        description = "The set of scopes a role should be added to and/or removed from within a tenant.")
public class RoleScopeBindingUpdateWrapper {

    @ApiModelProperty(
            name = "addedScopes",
            value = "The scope names the role should be added to the bindings of. "
                    + "Scopes not listed here are not modified.")
    private List<String> addedScopes;

    @ApiModelProperty(
            name = "removedScopes",
            value = "The scope names the role should be removed from the bindings of. "
                    + "Only scopes listed here are considered for removal.")
    private List<String> removedScopes;

    public List<String> getAddedScopes() {
        return addedScopes;
    }

    public void setAddedScopes(List<String> addedScopes) {
        this.addedScopes = addedScopes;
    }

    public List<String> getRemovedScopes() {
        return removedScopes;
    }

    public void setRemovedScopes(List<String> removedScopes) {
        this.removedScopes = removedScopes;
    }
}
