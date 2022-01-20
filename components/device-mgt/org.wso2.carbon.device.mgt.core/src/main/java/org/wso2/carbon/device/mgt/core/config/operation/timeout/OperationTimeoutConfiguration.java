package org.wso2.carbon.device.mgt.core.config.operation.timeout;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by amalka on 1/19/22.
 */
@XmlRootElement(name = "OperationTimeoutConfigurations")
public class OperationTimeoutConfiguration {
    private List<OperationTimeout> operationTimeoutList;

    public List<OperationTimeout> getOperationTimeoutList() {
        return operationTimeoutList;
    }

    @XmlElementWrapper(name = "OperationTimeouts", required = true)
    @XmlElement(name = "OperationTimeout", required = false)
    public void setOperationTimeoutList(List<OperationTimeout> operationTimeoutList) {
        this.operationTimeoutList = operationTimeoutList;
    }
}
