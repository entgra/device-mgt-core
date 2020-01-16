package org.wso2.carbon.device.mgt.extensions.device.type.template.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import java.util.List;

public class OptionalData {
    @XmlElement(name = "Checked")
    private boolean ischecked;

    @XmlElementWrapper(name = "Options")
    @XmlElement(name = "Option")
    private List<Option> option;

    @XmlElement(name = "SubPanel")
    private SubPanel subPanel;

    @XmlElement(name = "Placeholder")
    private String placeholder;

    @XmlElement(name = "Rules")
    private ValidationRules rules;

    public boolean getChecked() {
        return ischecked;
    }

    public void setChecked(boolean hidden) {
        this.ischecked = hidden;
    }

    public List<Option> getOptions() {
        return option;
    }

    public void setOptions(List<Option> option) {
        this.option = option;
    }

    public SubPanel getSubPanels(){
        return subPanel;
    }

    public void setSubPanels(SubPanel subPanel){
        this.subPanel = subPanel;
    }

    public String getPlaceholders(){
        return placeholder;
    }

    public void setPlaceholders(String placeholder){
        this.placeholder = placeholder;
    }

    public ValidationRules getRule(){
        return rules;
    }

    public void setRule(ValidationRules rules){
        this.rules = rules;
    }

}
