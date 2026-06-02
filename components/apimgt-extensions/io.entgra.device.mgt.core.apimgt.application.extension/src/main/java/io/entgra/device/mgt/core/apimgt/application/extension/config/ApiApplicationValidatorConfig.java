package io.entgra.device.mgt.core.apimgt.application.extension.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "ApiApplicationValidatorConfig")
public class ApiApplicationValidatorConfig {

    private List<ValidatorConfig> validators;

    @XmlElementWrapper(name = "Validators")
    @XmlElement(name = "Validator")
    public List<ValidatorConfig> getValidators() {
        return validators;
    }

    public void setValidators(List<ValidatorConfig> validators) {
        this.validators = validators;
    }
}
