/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package io.entgra.device.mgt.core.policy.mgt.core.enforcement;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.EnrolmentInfo;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyAdministratorPoint;
import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService;
import io.entgra.device.mgt.core.policy.mgt.core.internal.PolicyManagementDataHolder;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
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

    @Test
    public void testRemovedOrUnenrolledDevicesAreNotApplied() throws Exception {
        Device removed = RecordingDelegator.device("removed-device", EnrolmentInfo.Status.REMOVED);
        Device missingEnrolment = RecordingDelegator.device("no-enrolment", null);
        Device active = RecordingDelegator.device("device-1", EnrolmentInfo.Status.ACTIVE);
        RecordingDelegator delegator = new RecordingDelegator(
                Arrays.asList(removed, missingEnrolment, active), policy(7), policy(7), 7);

        delegator.delegate();

        Assert.assertEquals(delegator.appliedDeviceIds, Collections.singletonList("device-1"));
        Mockito.verify(pap).setPolicyUsed(Mockito.any(DeviceIdentifier.class), Mockito.argThat(
                policy -> policy.getId() == 7));
    }

    @Test
    public void testDeviceReloadFailureDoesNotStopRemainingDevices() throws Exception {
        Device failing = RecordingDelegator.device("missing-device", EnrolmentInfo.Status.ACTIVE);
        Device healthy = RecordingDelegator.device("device-1", EnrolmentInfo.Status.ACTIVE);
        RecordingDelegator delegator = new RecordingDelegator(
                Arrays.asList(failing, healthy), policy(7), policy(7), 7) {
            @Override
            public Policy getEffectivePolicy(DeviceIdentifier identifier) throws PolicyDelegationException {
                if ("missing-device".equals(identifier.getId())) {
                    throw new PolicyDelegationException("Device details cannot be null.");
                }
                return super.getEffectivePolicy(identifier);
            }
        };

        delegator.delegate();

        Assert.assertEquals(delegator.appliedDeviceIds, Collections.singletonList("device-1"));
    }

    private Policy policy(int id) {
        Policy policy = new Policy();
        policy.setId(id);
        return policy;
    }

    private static class RecordingDelegator extends PolicyEnforcementDelegatorImpl {

        private final Policy appliedPolicy;
        private final Policy effectivePolicy;
        private final List<String> appliedDeviceIds = new ArrayList<>();
        private boolean revokeQueued;
        private boolean applyQueued;

        RecordingDelegator(Policy appliedPolicy, Policy effectivePolicy, int selectedPolicyId) {
            this(Collections.singletonList(device("device-1")), appliedPolicy, effectivePolicy, selectedPolicyId);
        }

        RecordingDelegator(List<Device> devices, Policy appliedPolicy, Policy effectivePolicy, int selectedPolicyId) {
            super(devices, Collections.singleton(selectedPolicyId));
            this.appliedPolicy = appliedPolicy;
            this.effectivePolicy = effectivePolicy;
        }

        @Override
        public Policy getAppliedPolicyToDevice(Device device) {
            return appliedPolicy;
        }

        @Override
        public Policy getEffectivePolicy(DeviceIdentifier identifier) throws PolicyDelegationException {
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
            appliedDeviceIds.add(deviceIdentifiers.get(0).getId());
        }

        private static Device device(String deviceId) {
            return device(deviceId, EnrolmentInfo.Status.ACTIVE);
        }

        private static Device device(String deviceId, EnrolmentInfo.Status status) {
            Device device = new Device();
            device.setDeviceIdentifier(deviceId);
            device.setType("android");
            if (status != null) {
                EnrolmentInfo enrolmentInfo = new EnrolmentInfo();
                enrolmentInfo.setStatus(status);
                device.setEnrolmentInfo(enrolmentInfo);
            }
            return device;
        }
    }
}
