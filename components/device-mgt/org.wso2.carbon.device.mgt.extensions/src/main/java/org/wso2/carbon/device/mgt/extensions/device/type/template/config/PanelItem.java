package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class PanelItem {
    @XmlElement(name = "Label", required = true)
    private String label;

    @XmlAttribute(name = "type", required = true)
    protected String type;

    @XmlElement(name = "Tooltip")
    protected String tooltip;

    @XmlElement(name = "ItemId")
    protected String id;

    @XmlElement(name = "Optional")
    protected OptionalData optional;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OptionalData getOptionalData() {
        return optional;
    }

    public void setOptionalData(OptionalData optional) {
        this.optional = optional;
    }

}
