/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.device.application.mgt.core.config;

import org.wso2.carbon.device.application.mgt.common.config.MDMConfig;
import org.wso2.carbon.device.application.mgt.common.config.RatingConfiguration;
import org.wso2.carbon.device.application.mgt.common.config.UIConfiguration;
import org.wso2.carbon.device.application.mgt.common.config.LifecycleState;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the Application Management Configuration.
 */
@XmlRootElement(name = "ApplicationManagementConfiguration")
public class Configuration {

    private String datasourceName;

    private List<Extension> extensions;

    private List<LifecycleState> lifecycleStates;

    private UIConfiguration uiConfiguration;

    private List<String> appCategories;

    private RatingConfiguration ratingConfiguration;

    private MDMConfig mdmConfig;

    @XmlElement(name = "DatasourceName", required = true)
    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    @XmlElementWrapper(name = "Extensions")
    @XmlElement(name = "Extension")
    public List<Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<Extension> extensions) {
        this.extensions = extensions;
    }

    @XmlElementWrapper(name = "LifecycleStates")
    @XmlElement(name = "LifecycleState")
    public List<LifecycleState> getLifecycleStates() {
        return lifecycleStates;
    }

    public void setLifecycleStates(List<LifecycleState> lifecycleStates) {
        this.lifecycleStates = lifecycleStates;
    }

    @XmlElement(name = "UIConfigs")
    public UIConfiguration getUiConfiguration() {
        return uiConfiguration;
    }

    public void setUiConfiguration(UIConfiguration uiConfiguration) {
        this.uiConfiguration = uiConfiguration;
    }

    @XmlElement(name = "RatingConfig")
    public RatingConfiguration getRatingConfiguration() { return ratingConfiguration; }

    public void setRatingConfiguration(
            RatingConfiguration ratingConfiguration) { this.ratingConfiguration = ratingConfiguration; }

    @XmlElementWrapper(name = "AppCategories")
    @XmlElement(name = "Category")
    public List<String> getAppCategories() {
        return appCategories;
    }

    public void setAppCategories(List<String> appCategories) {
        this.appCategories = appCategories;
    }

    @XmlElement(name = "MDMConfig", required = true)
    public MDMConfig getMdmConfig() { return mdmConfig; }

    public void setMdmConfig(MDMConfig mdmConfig) {
        this.mdmConfig = mdmConfig;
    }
}
