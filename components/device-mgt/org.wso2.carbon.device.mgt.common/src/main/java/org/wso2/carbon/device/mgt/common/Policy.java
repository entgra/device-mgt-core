package org.wso2.carbon.device.mgt.common;

import javax.xml.bind.annotation.XmlElement;
import io.swagger.annotations.*;
import java.io.Serializable;
import java.util.List;


@ApiModel(
        value = "Policy",
        description = "This class carries all information related to a policies."
)
public class Policy implements Serializable {
    @ApiModelProperty(
            name = "id",
            value = "Feature Id.",
            required = true
    )
    private int id;

    @ApiModelProperty(
            name = "name",
            value = "A name that describes a feature.",
            required = true
    )
    private String name;

    @ApiModelProperty(
            name = "description",
            value = "Provides a description of the features.",
            required = true
    )
    private String description;

    @ApiModelProperty(
            name = "panels",
            value = "Properties related to features.",
            required = true
    )
    private List<DataPanels> panels;

    @XmlElement
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<DataPanels> getPanels() {
        return panels;
    }

    public void setPanels(List<DataPanels> panels) {
        this.panels = panels;
    }

    public static class DataPanels implements Serializable {
        private Object panel;

        public Object getPanel() {
            return panel;
        }

        public void setPanel(Object value) {
            this.panel = value;
        }

    }
}
