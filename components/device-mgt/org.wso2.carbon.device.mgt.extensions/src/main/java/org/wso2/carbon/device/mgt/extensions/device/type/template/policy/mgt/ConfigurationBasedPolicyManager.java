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
package org.wso2.carbon.device.mgt.extensions.device.type.template.policy.mgt;

import org.wso2.carbon.device.mgt.common.Policy;
import org.wso2.carbon.device.mgt.common.PolicyConfigurationManager;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DataPanel;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DataPanels;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PanelItem;


import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationBasedPolicyManager implements PolicyConfigurationManager {
    private List<Policy> policies = new ArrayList<org.wso2.carbon.device.mgt.common.Policy>();

    public ConfigurationBasedPolicyManager(List<org.wso2.carbon.device.mgt.extensions.device.type.template.config.Policy> policies){
        for (org.wso2.carbon.device.mgt.extensions.device.type.template.config.Policy policy : policies) {
            org.wso2.carbon.device.mgt.common.Policy policyConfiguration = new org.wso2.carbon.device.mgt.common.Policy();
            policyConfiguration.setName(policy.getName());
            if(policy.getPanels() != null){
                List<org.wso2.carbon.device.mgt.common.Policy.DataPanels> panel = new ArrayList<>();

                for(DataPanel panelData: policy.getPanels()){
                    org.wso2.carbon.device.mgt.common.Policy.DataPanels panelDataEntry = new org.wso2.carbon.device.mgt.common.Policy.DataPanels();
                    panelDataEntry.setPanel(panelData);
                    panel.add(panelDataEntry);

                }
                policyConfiguration.setPanels(panel);

            }

            this.policies.add(policyConfiguration);
        }

    }

    @Override
    public List<org.wso2.carbon.device.mgt.common.Policy> getPolicies() throws DeviceManagementException {
        return policies;
    }
}
