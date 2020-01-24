package org.wso2.carbon.device.mgt.extensions.device.type.template.policy.mgt;

import org.wso2.carbon.device.mgt.common.Policy;
import org.wso2.carbon.device.mgt.common.PolicyConfigurationManager;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DataPanel;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.PanelItem;


import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationBasedPolicyManager implements PolicyConfigurationManager {
    private List<Policy> policies = new ArrayList<>();

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
    public List<org.wso2.carbon.device.mgt.common.Policy> getPolicies() {
        return policies;
    }
}
