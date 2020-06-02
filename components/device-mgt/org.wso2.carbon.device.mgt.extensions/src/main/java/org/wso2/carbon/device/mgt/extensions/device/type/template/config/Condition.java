package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Condition")
public class Condition {

    String id;
    List<String> values;

    @XmlElement(name = "Id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    @XmlElementWrapper(name = "Values")
    @XmlElement(name = "Value")
    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
