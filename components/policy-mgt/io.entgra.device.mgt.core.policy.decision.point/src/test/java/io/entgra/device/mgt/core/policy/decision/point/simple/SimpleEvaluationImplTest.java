/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package io.entgra.device.mgt.core.policy.decision.point.simple;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.policy.decision.point.internal.PolicyDecisionPointDataHolder;
import io.entgra.device.mgt.core.policy.mgt.common.PIPDevice;
import io.entgra.device.mgt.core.policy.mgt.common.PolicyInformationPoint;
import io.entgra.device.mgt.core.policy.mgt.core.PolicyManagerService;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class SimpleEvaluationImplTest {

    @AfterMethod
    public void tearDown() {
        PolicyDecisionPointDataHolder.getInstance().setPolicyManagerService(null);
    }

    @Test
    public void testOnlySelectedPoliciesCompeteByPriority() throws Exception {
        PolicyManagerService policyManagerService = Mockito.mock(PolicyManagerService.class);
        PolicyInformationPoint policyInformationPoint = Mockito.mock(PolicyInformationPoint.class);
        PIPDevice pipDevice = new PIPDevice();
        DeviceIdentifier identifier = new DeviceIdentifier("device-1", "android");

        Policy unselectedHighestPriority = policy(1, 1);
        Policy selectedWinner = policy(2, 2);
        Policy selectedLowerPriority = policy(3, 3);

        Mockito.when(policyManagerService.getPIP()).thenReturn(policyInformationPoint);
        Mockito.when(policyInformationPoint.getDeviceData(identifier)).thenReturn(pipDevice);
        Mockito.when(policyInformationPoint.getRelatedPolicies(pipDevice)).thenReturn(
                Arrays.asList(unselectedHighestPriority, selectedLowerPriority, selectedWinner));
        PolicyDecisionPointDataHolder.getInstance().setPolicyManagerService(policyManagerService);

        Policy result = new SimpleEvaluationImpl().getEffectivePolicy(identifier,
                new HashSet<>(Arrays.asList(2, 3)));

        Assert.assertEquals(result.getId(), 2);
        Mockito.verify(policyManagerService, Mockito.never()).getPAP();
    }

    @Test
    public void testNoSelectedApplicablePolicyReturnsNull() throws Exception {
        PolicyManagerService policyManagerService = Mockito.mock(PolicyManagerService.class);
        PolicyInformationPoint policyInformationPoint = Mockito.mock(PolicyInformationPoint.class);
        PIPDevice pipDevice = new PIPDevice();
        DeviceIdentifier identifier = new DeviceIdentifier("device-1", "android");

        Mockito.when(policyManagerService.getPIP()).thenReturn(policyInformationPoint);
        Mockito.when(policyInformationPoint.getDeviceData(identifier)).thenReturn(pipDevice);
        Mockito.when(policyInformationPoint.getRelatedPolicies(pipDevice)).thenReturn(
                Collections.singletonList(policy(1, 1)));
        PolicyDecisionPointDataHolder.getInstance().setPolicyManagerService(policyManagerService);

        Assert.assertNull(new SimpleEvaluationImpl().getEffectivePolicy(identifier,
                Collections.singleton(2)));
    }

    private Policy policy(int id, int priority) {
        Policy policy = new Policy();
        policy.setId(id);
        policy.setPriorityId(priority);
        return policy;
    }
}
