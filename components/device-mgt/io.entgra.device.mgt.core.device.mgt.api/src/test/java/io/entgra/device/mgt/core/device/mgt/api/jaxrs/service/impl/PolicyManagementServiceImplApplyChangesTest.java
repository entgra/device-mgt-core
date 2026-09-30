/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashSet;

public class PolicyManagementServiceImplApplyChangesTest {

    private final PolicyManagementServiceImpl service = new PolicyManagementServiceImpl();

    @Test
    public void testMissingPolicyIdsAreRejected() {
        Assert.assertEquals(service.applyChanges(null).getStatus(),
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testEmptyPolicyIdsAreRejected() {
        Assert.assertEquals(service.applyChanges(Collections.emptySet()).getStatus(),
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testNonPositivePolicyIdsAreRejected() {
        Assert.assertEquals(service.applyChanges(new HashSet<>(Collections.singletonList(0))).getStatus(),
                Response.Status.BAD_REQUEST.getStatusCode());
    }
}
