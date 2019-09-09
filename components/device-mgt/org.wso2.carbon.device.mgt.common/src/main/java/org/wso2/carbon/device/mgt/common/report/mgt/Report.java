package org.wso2.carbon.device.mgt.common.report.mgt;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Report object which is used to transfer Report data to MDM core
 */
@ApiModel(value = "Report", description = "This is used to communicate Operation Reports to MDM.")
public class Report {

    @JsonProperty(value = "id", required = false)
    @ApiModelProperty(name = "id", value = "Defines the report ID.", required = false)
    private int id;


    @JsonProperty(value = "body", required = false)
    @ApiModelProperty(name = "body", value = "Provides report body",
            required = true)
    private String body;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "report {" + "  id='" + id + '\'' + ", body=" + body + '}';
    }
}

