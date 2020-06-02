package org.wso2.carbon.device.application.mgt.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "LicenseDTO", description = "LicenseDTO represents License details.")
public class LicenseDTO {
    @ApiModelProperty(name = "id",
            value = "The ID given to the license when it is stored in the APPM database")
    private int id;

    @ApiModelProperty(name = "licenseId",
            value = "The identifier of the assigned license")
    private String licenseId;

    @ApiModelProperty(name = "adamId",
            value = "The unique identifier for a product in the iTunes Store")
    private String adamId;

    @ApiModelProperty(name = "status",
            value = "The current state of the license",
            example = "Available, Refunded")
    private String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public String getAdamId() {
        return adamId;
    }

    public void setAdamId(String adamId) {
        this.adamId = adamId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
