package org.wso2.carbon.device.mgt.common.app.mgt;

import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

public class Account implements Serializable {

    @ApiModelProperty(name = "name", value = "The device account's name", required = true)
    private String name;
    @ApiModelProperty(name = "type", value = "The device account's type", required = true)
    private String type;

    public Account() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
