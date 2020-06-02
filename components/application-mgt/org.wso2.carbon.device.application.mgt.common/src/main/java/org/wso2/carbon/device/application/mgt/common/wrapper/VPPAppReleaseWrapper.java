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

@ApiModel(value = "VPP App Release Wrapper", description = "This class holds the details when releasing a VPP App"
        + " Release to application store")
public class VPPAppReleaseWrapper {

    @ApiModelProperty(name = "description",
            value = "Description of the VPP app release")
    @NotNull
    private String description;

    @ApiModelProperty(name = "releaseType",
            value = "Release type of the VPP app release",
            required = true,
            example = "alpha, beta etc")
    @NotNull
    private String releaseType;

    @ApiModelProperty(name = "price",
            value = "Price of the VPP app release",
            required = true)
    private Double price;

    @ApiModelProperty(name = "isSharedWithAllTenants",
            value = "If VPP app release is shared with all tenants it is equal to 1 otherwise 0",
            required = true)
    @NotNull
    private boolean isSharedWithAllTenants;

    @ApiModelProperty(name = "metaData",
            value = "Meta data of the VPP app release",
            required = true)
    private String metaData;

    @ApiModelProperty(name = "version",
            value = "Version of the VPP app release.",
            required = true)
    @NotNull
    private String version;

    @ApiModelProperty(name = "packageName",
            value = "Package name of the VPP app release.",
            required = true)
    @NotNull
    private String packageName;

    @ApiModelProperty(name = "supportedOsVersions",
            value = "Application release supported OS versions",
            required = true,
            example = "4.0-10.0")
    @NotNull
    private String supportedOsVersions;

    @ApiModelProperty(name = "smallIconName",
            value = "icon file location",
            required = true)
    @NotNull
    private String smallIconName;

    @ApiModelProperty(name = "largeIconName",
            value = "icon file location",
            required = true)
    @NotNull
    private String largeIconName;

    @ApiModelProperty(name = "installerName",
            value = "VPP app URL",
            required = true)
    @NotNull
    private String installerName;

    @ApiModelProperty(name = "ratedUsers",
            value = "Number of users who has rated the application release")
    private int ratedUsers;

    @ApiModelProperty(name = "rating",
            value = "Rating value of the application release")
    private double rating;

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

    public void setIsSharedWithAllTenants(boolean isSharedWithAllTenants) {
        this.isSharedWithAllTenants = isSharedWithAllTenants;
    }

    public String getSmallIconName() {
        return smallIconName;
    }

    public void setSmallIconName(String smallIconName) {
        this.smallIconName = smallIconName;
    }

    public String getLargeIconName() {
        return largeIconName;
    }

    public void setLargeIconName(String largeIconName) {
        this.largeIconName = largeIconName;
    }

    public String getInstallerName() {
        return installerName;
    }

    public void setInstallerName(String installerName) {
        this.installerName = installerName;
    }

    public int getRatedUsers() {
        return ratedUsers;
    }

    public void setRatedUsers(int ratedUsers) {
        this.ratedUsers = ratedUsers;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public boolean getIsSharedWithAllTenants() {
        return isSharedWithAllTenants;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public boolean isSharedWithAllTenants() { return isSharedWithAllTenants; }

    public void setSharedWithAllTenants(boolean sharedWithAllTenants) { isSharedWithAllTenants = sharedWithAllTenants; }

    public String getVersion() { return version; }

    public void setVersion(String version) { this.version = version; }

    public String getPackageName() { return packageName; }

    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getSupportedOsVersions() { return supportedOsVersions; }

    public void setSupportedOsVersions(String supportedOsVersions) { this.supportedOsVersions = supportedOsVersions; }
}
