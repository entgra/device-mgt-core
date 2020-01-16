package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

/**
 * Java class for uiParams complex type.
 *
 * The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <xs:element name="Panels">
 *   <xs:complexType>
 *     <xs:sequence>
 *       <xs:element name="Panel" type="{}Panels"/>
 *     </xs:sequence>
 *   </xs:complexType>
 * </xs:element>
 * </pre>
 *
 */
public class DataPanels {

    @XmlElement(name = "Panel")
    private List<DataPanel> dataPanel;

    public List<DataPanel> getPanels() {
        return this.dataPanel;
    }

    public void setPanels(List<DataPanel> dataPanel) {
        this.dataPanel = dataPanel;
    }
}
