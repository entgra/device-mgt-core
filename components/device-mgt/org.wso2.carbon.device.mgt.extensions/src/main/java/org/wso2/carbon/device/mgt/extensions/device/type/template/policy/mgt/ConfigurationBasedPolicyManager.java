package org.wso2.carbon.device.mgt.extensions.device.type.template.policy.mgt;

import org.wso2.carbon.device.mgt.common.ui.policy.mgt.Policy;
import org.wso2.carbon.device.mgt.common.ui.policy.mgt.PolicyConfigurationManager;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.DataPanel;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationBasedPolicyManager implements PolicyConfigurationManager {
    private List<Policy> policies = new ArrayList<>();

    public ConfigurationBasedPolicyManager(List<org.wso2.carbon.device.mgt.extensions.device.type.template.config.Policy> policies){
        for (org.wso2.carbon.device.mgt.extensions.device.type.template.config.Policy policy : policies) {
            Policy policyConfiguration = new Policy();
            policyConfiguration.setName(policy.getName());
            if(policy.getPanels() != null){
                List<Policy.DataPanels> panel = new ArrayList<>();

                for(DataPanel panelData: policy.getPanels()){
                    Policy.DataPanels panelDataEntry = new Policy.DataPanels();
                    panelDataEntry.setPanel(panelData);
                    panel.add(panelDataEntry);

                }
                policyConfiguration.setPanels(panel);
            }
            this.policies.add(policyConfiguration);
        }
    }

    @Override
    public List<Policy> getPolicies() {
        return policies;
    }
}