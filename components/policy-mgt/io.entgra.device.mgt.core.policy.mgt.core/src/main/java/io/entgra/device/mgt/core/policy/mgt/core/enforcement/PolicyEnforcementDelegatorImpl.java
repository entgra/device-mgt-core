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
package io.entgra.device.mgt.core.policy.mgt.core.enforcement;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.InvalidDeviceException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.CommandOperation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationMgtConstants;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.PolicyOperation;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyEvaluationException;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyManagementException;
import io.entgra.device.mgt.core.policy.mgt.common.PolicySelectionEvaluationPoint;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyTransformException;
import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService;
import io.entgra.device.mgt.core.policy.mgt.core.internal.PolicyManagementDataHolder;
import io.entgra.device.mgt.core.policy.mgt.core.util.PolicyManagerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PolicyEnforcementDelegatorImpl implements PolicyEnforcementDelegator{

    private static final Log log = LogFactory.getLog(PolicyEnforcementDelegatorImpl.class);

    private final List<Device> devices;
    private final Set<Integer> selectedPolicyIds;

    public PolicyEnforcementDelegatorImpl(List<Device> devices, Set<Integer> selectedPolicyIds) {

        log.info("Policy re-enforcing stared due to change of the policies.");

        if (log.isDebugEnabled()) {
            for (Device device : devices) {
                log.debug("Policy re-enforcing for device :" + device.getDeviceIdentifier() + " - Type : "
                        + device.getType());
            }
        }
        this.devices = devices;
        this.selectedPolicyIds = selectedPolicyIds;
    }

    @Override
    public void delegate() throws PolicyDelegationException {
        for (Device device : devices) {
            DeviceIdentifier identifier = new DeviceIdentifier();
            identifier.setId(device.getDeviceIdentifier());
            identifier.setType(device.getType());

            Policy devicePolicy = this.getAppliedPolicyToDevice(device);
            Policy policy = this.getEffectivePolicy(identifier);
            List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
            deviceIdentifiers.add(identifier);
            if (policy != null) {
                // A selected winner is always re-applied. This covers edits to a policy with the same ID.
                this.markPreviousPolicyBundlesRepeated(device);
                if (devicePolicy != null && devicePolicy.getId() != policy.getId()) {
                    this.addPolicyRevokeOperation(deviceIdentifiers);
                }
                this.addPolicyOperation(deviceIdentifiers, policy);
                this.setAppliedPolicy(identifier, policy);
            } else if (devicePolicy != null && selectedPolicyIds.contains(devicePolicy.getId())) {
                // No selected policy applies. Revoke only if the currently assigned policy was selected.
                this.markPreviousPolicyBundlesRepeated(device);
                this.addPolicyRevokeOperation(deviceIdentifiers);
                this.removeAppliedPolicy(identifier);
            }
        }
    }

    @Override
    public Policy getEffectivePolicy(DeviceIdentifier identifier) throws PolicyDelegationException {
        try {
            PolicyManagerService policyManagerService = PolicyManagementDataHolder.getInstance()
                    .getPolicyManagerService();
            if (!(policyManagerService.getPEP() instanceof PolicySelectionEvaluationPoint)) {
                throw new PolicyEvaluationException(
                        "Selected policy evaluation requires the Simple evaluation point");
            }
            return ((PolicySelectionEvaluationPoint) policyManagerService.getPEP())
                    .getEffectivePolicy(identifier, selectedPolicyIds);
        } catch (PolicyEvaluationException | PolicyManagementException e) {
            String msg = "Error occurred while retrieving the effective policy for devices.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        }
    }

    private void setAppliedPolicy(DeviceIdentifier identifier, Policy policy) throws PolicyDelegationException {
        try {
            PolicyManagementDataHolder.getInstance().getPolicyManagerService().getPAP()
                    .setPolicyUsed(identifier, policy);
        } catch (PolicyManagementException e) {
            throw new PolicyDelegationException("Policy operation was queued but the applied-policy record " +
                    "could not be updated", e);
        }
    }

    private void removeAppliedPolicy(DeviceIdentifier identifier) throws PolicyDelegationException {
        try {
            PolicyManagementDataHolder.getInstance().getPolicyManagerService().getPAP()
                    .removePolicyUsed(identifier);
        } catch (PolicyManagementException e) {
            throw new PolicyDelegationException("Policy revoke was queued but the applied-policy record " +
                    "could not be removed", e);
        }
    }

    @Override
    public void addPolicyOperation(List<DeviceIdentifier> deviceIdentifiers, Policy policy) throws
            PolicyDelegationException {
        try {
            String type = null;
            if (!deviceIdentifiers.isEmpty()) {
                type = deviceIdentifiers.get(0).getType();
            }
            PolicyManagementDataHolder.getInstance().getDeviceManagementService().addOperation(type,
                                                          PolicyManagerUtil.transformPolicy(policy), deviceIdentifiers);
        } catch (InvalidDeviceException e) {
            String msg = "Invalid DeviceIdentifiers found.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while adding the operation to device.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        } catch (PolicyTransformException e) {
            String msg = "Error occurred while transforming policy object to operation object type for policy " +
                         policy.getId() + " - " + policy.getPolicyName() + " for devices " +
                         deviceIdentifiers.toString();
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        }
    }

    @Override
    public void addPolicyRevokeOperation(List<DeviceIdentifier> deviceIdentifiers) throws PolicyDelegationException {
        try {
            String type = null;
            if (!deviceIdentifiers.isEmpty()) {
                type = deviceIdentifiers.get(0).getType();
            }
            PolicyManagementDataHolder.getInstance().getDeviceManagementService().addOperation(type,
                                                                   this.getPolicyRevokeOperation(), deviceIdentifiers);
        } catch (InvalidDeviceException e) {
            String msg = "Invalid DeviceIdentifiers found.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        } catch (OperationManagementException e) {
            String msg = "Error occurred while adding the operation to device.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        }
    }

    private Operation getPolicyRevokeOperation() {
        CommandOperation policyRevokeOperation = new CommandOperation();
        policyRevokeOperation.setEnabled(true);
        policyRevokeOperation.setCode(OperationMgtConstants.OperationCodes.POLICY_REVOKE);
        policyRevokeOperation.setType(Operation.Type.COMMAND);
        return policyRevokeOperation;
    }

    /**
     * Provides the applied policy for give device
     *
     * @param device Device
     * @return Applied Policy
     * @throws PolicyDelegationException exception throws when retrieving applied policy for given device
     */
    public Policy getAppliedPolicyToDevice(Device device) throws PolicyDelegationException {
        try {
            PolicyManagerService policyManagerService = PolicyManagementDataHolder.getInstance()
                    .getPolicyManagerService();
            return policyManagerService.getAppliedPolicyToDevice(device);
        } catch (PolicyManagementException e) {
            String msg = "Error occurred while retrieving the applied policy for devices.";
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        }
    }

    /**
     * Update the previous pending policy operation's status as REPEATED
     * @param device Device
     * @throws PolicyDelegationException throws when getting pending operations
     */
    public void markPreviousPolicyBundlesRepeated(Device device) throws PolicyDelegationException {
        DeviceManagementProviderService deviceManagerService = PolicyManagementDataHolder.getInstance().
                getDeviceManagementService();
        try {
            List<? extends Operation> operations = deviceManagerService.getPendingOperations(device);
            for(Operation operation : operations) {
                String operationCode = operation.getCode();
                if(PolicyOperation.POLICY_OPERATION_CODE.equals(operationCode) ||
                        OperationMgtConstants.OperationCodes.POLICY_REVOKE.equals(operationCode)) {
                    operation.setStatus(Operation.Status.REPEATED);
                    deviceManagerService.updateOperation(device, operation);
                }
            }
        } catch (OperationManagementException e) {
            String msg = "Error occurred while retrieving pending operations of device id "+device.getId();
            log.error(msg, e);
            throw new PolicyDelegationException(msg, e);
        }
    }
}
