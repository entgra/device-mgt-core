package org.wso2.carbon.device.mgt.core.status.task.io.entgra.ticketing.common.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "Gateway")
public class TicketingGateway {

    private String name;
    private String extensionClass;
    private boolean isDefault;
    private List<Property> properties;

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "extensionClass")
    public String getExtensionClass() {
        return extensionClass;
    }

    public void setExtensionClass(String extensionClass) {
        this.extensionClass = extensionClass;
    }

    @XmlAttribute(name = "isDefault")
    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @XmlElementWrapper(name = "Properties")
    @XmlElement(name = "Property")
    public List<Property> getProperties() {
        return properties;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    /**
     * Retrives the Property based on the provided property name
     * @param propertyName has the name of the Property to be retrieved
     * @return retrieved {@link Property}
     */
    public Property getPropertyByName(String propertyName) {
        for (Property property : properties) {
            if (propertyName.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }
}

