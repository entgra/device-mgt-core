package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for Feature complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * <xs:element name="Policy">
 *   <xs:complexType>
 *     <xs:sequence>
 *       <xs:element name="Name" type="xs:string" />
 *       <xs:element name="Description" type="xs:string" />
 *     </xs:sequence>
 *   </xs:complexType>
 * </xs:element>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Policy", propOrder = {
        "name",
        "description"
})
public class Policy {
    @XmlElement(name = "Name", required = true)
    protected String name;

    @XmlElement(name = "Description", required = true)
    protected String description;

    @XmlElement(name = "Panels")
    private DataPanels dataPanels;

    /**
     * Gets the value of the name property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setDescription(String value) {
        this.description = value;
    }

    public DataPanels getDataPanels() {
        return dataPanels;
    }

    public void setDataPanels(DataPanels dataPanels) {
        this.uiParameters = dataPanels;
    }

}
