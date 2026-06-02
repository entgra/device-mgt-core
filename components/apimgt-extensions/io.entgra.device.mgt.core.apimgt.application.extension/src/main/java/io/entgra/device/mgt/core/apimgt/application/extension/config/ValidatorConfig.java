package io.entgra.device.mgt.core.apimgt.application.extension.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Validator")
public class ValidatorConfig {

    private String metadataKey;
    private String apiSearchQuery;

    @XmlElement(name = "MetadataKey")
    public String getMetadataKey() {
        return metadataKey;
    }

    public void setMetadataKey(String metadataKey) {
        this.metadataKey = metadataKey;
    }

    @XmlElement(name = "ApiSearchQuery")
    public String getApiSearchQuery() {
        return apiSearchQuery;
    }

    public void setApiSearchQuery(String apiSearchQuery) {
        this.apiSearchQuery = apiSearchQuery;
    }
}
