/*
 * Copyright (c) 2020, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
