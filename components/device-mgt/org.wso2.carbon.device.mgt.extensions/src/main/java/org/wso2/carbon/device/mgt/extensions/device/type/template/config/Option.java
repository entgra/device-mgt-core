package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class Option {
    @XmlElement(name = "OptionName", required = true)
    private String name;

    @XmlElement(name = "OptionValue", required = true)
    protected String value;

    public String getOptionName() {
        return name;
    }

    public void setOptionName(String name) {
        this.name = name;
    }

    public String getOptionValue() {
        return value;
    }

    public void setOptionValue(String value) {
        this.value = value;
    }
}
