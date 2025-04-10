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
package io.entgra.device.mgt.core.application.mgt.common.wrapper;

import io.entgra.device.mgt.core.device.mgt.common.Base64File;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(value = "Public App Release Wrapper", description = "This class holds the details when releasing an Public App"
        + " Release to application store")
public class PublicAppReleaseWrapper {

    @ApiModelProperty(name = "description",
            value = "Description of the public app release")
    @NotNull
    private String description;

    @ApiModelProperty(name = "releaseType",
            value = "Release type of the public app release",
            required = true,
            example = "alpha, beta etc")
    @NotNull
    private String releaseType;

    @ApiModelProperty(name = "price",
            value = "Price of the public app release",
            required = true)
    private Double price;

    @ApiModelProperty(name = "isSharedWithAllTenants",
            value = "If public app release is shared with all tenants it is equal to 1 otherwise 0",
            required = true)
    @NotNull
    private boolean isSharedWithAllTenants;

    @ApiModelProperty(name = "metaData",
            value = "Meta data of the public app release",
            required = true)
    private String metaData;

    @ApiModelProperty(name = "version",
            value = "Version of the public app release.",
            required = true)
    @NotNull
    private String version;

    @ApiModelProperty(name = "packageName",
            value = "Package name of the public app release.",
            required = true)
    @NotNull
    private String packageName;

    @ApiModelProperty(name = "supportedOsVersions",
            value = "Application release supported OS versions",
            required = true,
            example = "4.0-10.0")
    @NotNull
    private String supportedOsVersions;

    @ApiModelProperty(name = "screenshots",
            value = "screenshots of the application")
    private List<Base64File> screenshots;

    @ApiModelProperty(name = "icon",
            value = "icon of the application")
    private Base64File icon;

    @ApiModelProperty(name = "icon",
            value = "banner of the application")
    private Base64File banner;
    private boolean remoteStatus;

    public boolean isRemoteStatus() {
        return remoteStatus;
    }

    public void setRemoteStatus(boolean remoteStatus) {
        this.remoteStatus = remoteStatus;
    }
    private List<String> screenshotLinks;
    private String iconLink;
    private String bannerLink;

    public String getReleaseType() {
        return releaseType;
    }

    public void setReleaseType(String releaseType) {
        this.releaseType = releaseType;
    }

    public void setIsSharedWithAllTenants(boolean isSharedWithAllTenants) {
        this.isSharedWithAllTenants = isSharedWithAllTenants;
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

    public Base64File getIcon() {
        return icon;
    }

    public void setIcon(Base64File icon) {
        this.icon = icon;
    }

    public List<Base64File> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<Base64File> screenshots) {
        this.screenshots = screenshots;
    }

    public Base64File getBanner() {
        return banner;
    }

    public void setBanner(Base64File banner) {
        this.banner = banner;
    }

    public List<String> getScreenshotLinks() {
        return screenshotLinks;
    }

    public void setScreenshotLinks(List<String> screenshotLinks) {
        this.screenshotLinks = screenshotLinks;
    }

    public String getIconLink() {
        return iconLink;
    }

    public void setIconLink(String iconLink) {
        this.iconLink = iconLink;
    }

    public String getBannerLink() {
        return bannerLink;
    }

    public void setBannerLink(String bannerLink) {
        this.bannerLink = bannerLink;
    }
}
