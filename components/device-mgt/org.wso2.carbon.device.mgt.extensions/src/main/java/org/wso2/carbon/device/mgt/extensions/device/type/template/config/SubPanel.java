package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class SubPanel {
    @XmlElement(name = "PanelKey", required = true)
    protected String description;

    @XmlElementWrapper(name = "PanelItems")
    @XmlElement(name = "PanelItem")
    private List<PanelItem> panelItem;

    public String getDescription1() {
        return description;
    }

    public void setDescription1(String value) {
        this.description = value;
    }

    public List<PanelItem> getPanelItemList1() {
        return panelItem;
    }

    public void setPanelItemList1(List<PanelItem> panelItem) {
        this.panelItem = panelItem;
    }
}


