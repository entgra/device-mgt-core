/* Copyright (c) 2019, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package org.wso2.carbon.device.application.mgt.common.wrapper;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(value = "VPPAppWrapper", description = "VPPAppWrapper represents an Application in App Store")
public class VPPAppWrapper {

    @ApiModelProperty(name = "adamId",
            value = "The unique identifier for a product in the iTunes Store",
            required = true)
    @NotNull
    private String adamId;

    @ApiModelProperty(name = "name",
            value = "Name of the VPP app",
            required = true)
    @NotNull
    private String name;

    @ApiModelProperty(name = "deviceType",
            value = "Related device type of the public app",
            required = true,
            example = "IoS, Android, Arduino, RaspberryPi etc")
    @NotNull
    private String deviceType;

    @ApiModelProperty(name = "description",
            value = "Description of the VPP app",
            required = true)
    @NotNull
    private String description;

    @ApiModelProperty(name = "categories",
            value = "List of Categories",
            required = true,
            example = "Educational, Gaming, Travel, Entertainment etc")
    @NotNull
    private List<String> categories;

    @ApiModelProperty(name = "subType",
            value = "Subscription method of the VPP app",
            required = true,
            example = "PAID, FREE")
    @NotNull
    private String subMethod;

    @ApiModelProperty(name = "paymentCurrency",
            value = "Payment currency of the VPP app",
            required = true,
            example = "$")
    private String paymentCurrency;

    @ApiModelProperty(name = "tags",
            value = "List of tags")
    @NotNull
    private List<String> tags;

    @ApiModelProperty(name = "unrestrictedRoles",
            value = "List of roles that users should have to view the VPP app")
    @NotNull
    private List<String> unrestrictedRoles;

    @ApiModelProperty(name = "applicationReleaseWrappers",
            value = "List of VPP app releases",
            required = true)
    @NotNull
    private List<VPPAppReleaseWrapper> vppAppReleaseWrappers;

    @ApiModelProperty(name = "rating",
            value = "Rating value of the application release")
    private double rating;

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getAdamId() {
        return adamId;
    }

    public void setAdamId(String adamId) {
        this.adamId = adamId;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public List<String> getTags() { return tags; }

    public void setTags(List<String> tags) { this.tags = tags; }

    public String getPaymentCurrency() { return paymentCurrency; }

    public void setPaymentCurrency(String paymentCurrency) { this.paymentCurrency = paymentCurrency; }

    public List<String> getUnrestrictedRoles() { return unrestrictedRoles; }

    public void setUnrestrictedRoles(List<String> unrestrictedRoles) { this.unrestrictedRoles = unrestrictedRoles; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public List<VPPAppReleaseWrapper> getVppAppReleaseWrappers() { return vppAppReleaseWrappers; }

    public void setVppAppReleaseWrappers(List<VPPAppReleaseWrapper> vppAppReleaseWrappers) {
        this.vppAppReleaseWrappers = vppAppReleaseWrappers; }

    public List<String> getCategories() { return categories; }

    public void setCategories(List<String> categories) { this.categories = categories; }

    public String getSubMethod() { return subMethod; }

    public void setSubMethod(String subMethod) { this.subMethod = subMethod; }
}

