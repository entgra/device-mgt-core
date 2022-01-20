package org.wso2.carbon.device.mgt.core.config.operation.timeout;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by amalka on 1/19/22.
 */
@XmlRootElement(name = "OperationTimeout")
public class OperationTimeout {

    private String code;
    private int timeout;
    private List<String> deviceTypes;
    private String initialStatus;
    private String nextStatus;

    public String getCode() {
        return code;
    }

    @XmlElement(name = "Code", required = true)
    public void setCode(String code) {
        this.code = code;
    }

    public int getTimeout() {
        return timeout;
    }

    @XmlElement(name = "Timeout", required = true)
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public List<String> getDeviceTypes() {
        return deviceTypes;
    }

    @XmlElementWrapper(name = "DeviceTypes", required = true)
    @XmlElement(name = "DeviceType", required = true)
    public void setDeviceTypes(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    public String getInitialStatus() {
        return initialStatus;
    }

    @XmlElement(name = "InitialStatus", required = true)
    public void setInitialStatus(String initialStatus) {
        this.initialStatus = initialStatus;
    }

    public String getNextStatus() {
        return nextStatus;
    }

    @XmlElement(name = "NextStatus", required = true)
    public void setNextStatus(String nextStatus) {
        this.nextStatus = nextStatus;
    }
}
