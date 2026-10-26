/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package io.entgra.device.mgt.core.policy.mgt.core.enforcement;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyAdministratorPoint;
import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService;
import io.entgra.device.mgt.core.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

public class PolicyEnforcementDelegatorImplTest {

    private PolicyAdministratorPoint pap;

    @BeforeMethod
    public void setUp() throws Exception {
        pap = Mockito.mock(PolicyAdministratorPoint.class);
        PolicyManagerService service = Mockito.mock(PolicyManagerService.class);
        Mockito.when(service.getPAP()).thenReturn(pap);
        PolicyManagementDataHolder.getInstance().setPolicyManagerService(service);
    }

    @AfterMethod
    public void tearDown() {
        PolicyManagementDataHolder.getInstance().setPolicyManagerService(null);
    }

    @Test
    public void testSelectedSameIdIsReapplied() throws Exception {
        RecordingDelegator delegator = new RecordingDelegator(policy(7), policy(7), 7);
        delegator.delegate();

        Assert.assertFalse(delegator.revokeQueued);
        Assert.assertTrue(delegator.applyQueued);
        Mockito.verify(pap).setPolicyUsed(Mockito.any(DeviceIdentifier.class), Mockito.argThat(
                policy -> policy.getId() == 7));
    }

    @Test
    public void testDifferentSelectedWinnerRevokesAndReplacesAppliedPolicy() throws Exception {
        RecordingDelegator delegator = new RecordingDelegator(policy(7), policy(8), 8);
        delegator.delegate();

        Assert.assertTrue(delegator.revokeQueued);
        Assert.assertTrue(delegator.applyQueued);
        Mockito.verify(pap).setPolicyUsed(Mockito.any(DeviceIdentifier.class), Mockito.argThat(
                policy -> policy.getId() == 8));
    }

    @Test
    public void testUnrelatedAppliedPolicyIsPreservedWhenNoSelectedPolicyApplies() throws Exception {
        RecordingDelegator delegator = new RecordingDelegator(policy(9), null, 7);
        delegator.delegate();

        Assert.assertFalse(delegator.revokeQueued);
        Assert.assertFalse(delegator.applyQueued);
        Mockito.verifyZeroInteractions(pap);
    }

    @Test
    public void testSelectedAppliedPolicyIsRevokedWhenNoSelectedPolicyApplies() throws Exception {
        RecordingDelegator delegator = new RecordingDelegator(policy(7), null, 7);
        delegator.delegate();

        Assert.assertTrue(delegator.revokeQueued);
        Assert.assertFalse(delegator.applyQueued);
        Mockito.verify(pap).removePolicyUsed(Mockito.any(DeviceIdentifier.class));
    }

    private Policy policy(int id) {
        Policy policy = new Policy();
        policy.setId(id);
        return policy;
    }

    private static class RecordingDelegator extends PolicyEnforcementDelegatorImpl {

        private final Policy appliedPolicy;
        private final Policy effectivePolicy;
        private boolean revokeQueued;
        private boolean applyQueued;

        RecordingDelegator(Policy appliedPolicy, Policy effectivePolicy, int selectedPolicyId) {
            super(Collections.singletonList(device()), Collections.singleton(selectedPolicyId));
            this.appliedPolicy = appliedPolicy;
            this.effectivePolicy = effectivePolicy;
        }

        @Override
        public Policy getAppliedPolicyToDevice(Device device) {
            return appliedPolicy;
        }

        @Override
        public Policy getEffectivePolicy(DeviceIdentifier identifier) {
            return effectivePolicy;
        }

        @Override
        public void markPreviousPolicyBundlesRepeated(Device device) {
            // No operation manager is required for this unit test.
        }

        @Override
        public void addPolicyRevokeOperation(List<DeviceIdentifier> deviceIdentifiers) {
            revokeQueued = true;
        }

        @Override
        public void addPolicyOperation(List<DeviceIdentifier> deviceIdentifiers, Policy policy) {
            applyQueued = true;
        }

        private static Device device() {
            Device device = new Device();
            device.setDeviceIdentifier("device-1");
            device.setType("android");
            return device;
        }
    }
}
