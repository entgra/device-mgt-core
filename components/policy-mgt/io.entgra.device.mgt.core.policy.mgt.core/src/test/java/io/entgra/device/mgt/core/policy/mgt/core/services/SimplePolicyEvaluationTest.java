/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package io.entgra.device.mgt.core.policy.mgt.core.services;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.ProfileFeature;
import io.entgra.device.mgt.core.policy.mgt.common.*;
import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService;
import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.List;

public class
SimplePolicyEvaluationTest implements PolicyEvaluationPoint {

    private static final Log log = LogFactory.getLog(SimplePolicyEvaluationTest.class);
    public static final String DEVICE2 = "device2"; // assuming this device does not have valid policy

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {
        Policy policy = new Policy();
        List<Policy> policyList;
        PolicyAdministratorPoint policyAdministratorPoint;
        PolicyInformationPoint policyInformationPoint;
        PolicyManagerService policyManagerService = new PolicyManagerServiceImpl();
        try {
            if (policyManagerService != null) {

                policyInformationPoint = policyManagerService.getPIP();
                PIPDevice pipDevice = policyInformationPoint.getDeviceData(deviceIdentifier);
                policyList = policyInformationPoint.getRelatedPolicies(pipDevice);
                policyAdministratorPoint = policyManagerService.getPAP();
                for(Policy pol : policyList) {
                    log.debug("Policy used in evaluation -  Name  : " + pol.getPolicyName() );
                }

                sortPolicies(policyList);
                if(!policyList.isEmpty()) {
                    policy = policyList.get(0);
                } else {
                    policyAdministratorPoint.removePolicyUsed(deviceIdentifier);
                    return null;
                }
                policyAdministratorPoint.setPolicyUsed(deviceIdentifier, policy);
            }

        } catch (PolicyManagementException e) {
            String msg = "Error occurred when retrieving the policy related data from policy management service.";
            log.error(msg, e);
            throw new PolicyEvaluationException(msg, e);
        }
        return policy;
    }

    @Override
    public List<ProfileFeature> getEffectiveFeatures(DeviceIdentifier deviceIdentifier) throws PolicyEvaluationException {
        if(DEVICE2.equals(deviceIdentifier.getId())) {
            throw new PolicyEvaluationException();
        }else {
            return getEffectivePolicy(deviceIdentifier).getProfile().getProfileFeaturesList();
        }
    }

    @Override
    public String getName() {
        return "SimplePolicy";
    }

    public void sortPolicies(List<Policy> policyList) throws PolicyEvaluationException {
        Collections.sort(policyList);
    }
}
