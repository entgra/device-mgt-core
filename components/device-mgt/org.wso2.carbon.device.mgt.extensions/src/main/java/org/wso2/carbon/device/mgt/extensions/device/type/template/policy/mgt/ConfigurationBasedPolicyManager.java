package org.wso2.carbon.device.mgt.extensions.device.type.template.policy.mgt;

import org.wso2.carbon.device.mgt.common.PolicyConfigurationManager;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.extensions.device.type.template.config.Policy;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationBasedPolicyManager implements PolicyConfigurationManager {
    private List<org.wso2.carbon.device.mgt.common.Policy> policies = new ArrayList<org.wso2.carbon.device.mgt.common.Policy>();

    public ConfigurationBasedPolicyManager(List<Policy> policies){
        for (org.wso2.carbon.device.mgt.extensions.device.type.template.config.Policy policy : policies) {
            org.wso2.carbon.device.mgt.common.Policy policyConfiguration = new org.wso2.carbon.device.mgt.common.Policy();
            policyConfiguration.setName(policy.getName());
            policyConfiguration.setDescription(policy.getDescription());
            this.policies.add(policyConfiguration);
        }

    }

    @Override
    public List<org.wso2.carbon.device.mgt.common.Policy> getPolicies() throws DeviceManagementException {
        return policies;
    }
}
