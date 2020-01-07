package org.wso2.carbon.device.mgt.jaxrs.beans;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.ComplianceData;
import org.wso2.carbon.device.mgt.common.policy.mgt.monitor.NonComplianceData;

import java.util.ArrayList;
import java.util.List;

public class ComplianceDeviceList extends BasePaginatedResult{
    private List<ComplianceData> complianceData = new ArrayList<>();

    @ApiModelProperty(value = "List of devices returned")
    @JsonProperty("devices")
    public List<ComplianceData> getList() {
        return complianceData;
    }

    public void setList(List<ComplianceData> complianceData) {
        this.complianceData = complianceData;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");

        sb.append("  count: ").append(getCount()).append(",\n");
        sb.append("  devices: [").append(complianceData).append("\n");
        sb.append("]}\n");
        return sb.toString();
    }
}
