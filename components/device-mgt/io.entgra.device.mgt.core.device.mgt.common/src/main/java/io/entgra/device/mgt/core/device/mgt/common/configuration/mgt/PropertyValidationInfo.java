package io.entgra.device.mgt.core.device.mgt.common.configuration.mgt;

import io.swagger.annotations.ApiModelProperty;

public class PropertyValidationInfo {

    @ApiModelProperty(name = "propertyName", value = "Name of the validation property")
    private String propertyName;

    @ApiModelProperty(name = "propertyValue", value = "Value of the validation property")
    private String propertyValue;

    @ApiModelProperty(name = "isMatch", value = "Value indicating whether the property value is validated against the value in server")
    private boolean isMatch;

    @ApiModelProperty(name = "isExtensiveValidationPassed", value = "Value indicating the extensive validation criteria is passed or not")
    private boolean isExtensiveValidationPassed;

    @ApiModelProperty(name = "extensiveValidationCriteria", value = "Criteria used for extensive validation")
    private String extensiveValidationCriteria;


    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public boolean isMatch() {
        return isMatch;
    }

    public void setMatch(boolean match) {
        isMatch = match;
    }

    public boolean isExtensiveValidationPassed() {
        return isExtensiveValidationPassed;
    }

    public void setExtensiveValidationPassed(boolean extensiveValidationPassed) {
        isExtensiveValidationPassed = extensiveValidationPassed;
    }

    public String getExtensiveValidationCriteria() {
        return extensiveValidationCriteria;
    }

    public void setExtensiveValidationCriteria(String extensiveValidationCriteria) {
        this.extensiveValidationCriteria = extensiveValidationCriteria;
    }
}
