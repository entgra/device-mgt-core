package org.wso2.carbon.device.application.mgt.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "VPPDeviceDTO", description = "VPPDeviceDTO represents VPP Device details.")
public class VPPDeviceDTO {

    @ApiModelProperty(name = "id",
            value = "The ID given to the device when it is stored in the APPM database")
    private int id;

    @ApiModelProperty(name = "serialNumber",
            value = "serialNumber of the ios device",
            required = true)
    private String serialNumber;

    @ApiModelProperty(name = "userId",
            value = "userId of the device owner",
            required = true)
    private String userId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
