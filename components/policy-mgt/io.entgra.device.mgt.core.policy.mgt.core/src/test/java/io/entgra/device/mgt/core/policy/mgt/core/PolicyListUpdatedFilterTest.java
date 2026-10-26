/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package io.entgra.device.mgt.core.policy.mgt.core;

import io.entgra.device.mgt.core.device.mgt.common.PolicyPaginationRequest;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;
import io.entgra.device.mgt.core.policy.mgt.core.dao.PolicyDAO;
import io.entgra.device.mgt.core.policy.mgt.core.dao.PolicyManagementDAOFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.base.MultitenantConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PolicyListUpdatedFilterTest extends BasePolicyManagementDAOTest {

    private static final String ACTIVE = "active";
    private static final String ACTIVE_UPDATED = "active-updated";
    private static final String INACTIVE = "inactive";
    private static final String INACTIVE_UPDATED = "inactive-updated";

    private PolicyDAO policyDAO;

    @BeforeClass
    public void initializePolicyData() throws Exception {
        initDatSource();
        initSQLScript();
        initiatePrivilegedCaronContext();
        policyDAO = PolicyManagementDAOFactory.getPolicyDAO();

        int tenantId = MultitenantConstants.SUPER_TENANT_ID;
        insertPolicy(ACTIVE, tenantId, "android", "GENERAL", true, false, 1);
        insertPolicy(ACTIVE_UPDATED, tenantId, "android", "GENERAL", true, true, 2);
        insertPolicy(INACTIVE_UPDATED, tenantId, "android", "GENERAL", false, true, 3);
        insertPolicy(INACTIVE, tenantId, "windows", "CORRECTIVE", false, false, 4);
        insertPolicy("other-tenant-updated", 7, "android", "GENERAL", false, true, 1);
    }

    @Test
    public void testActiveStatusIncludesUpdatedPolicies() throws Exception {
        PolicyPaginationRequest request = request("ACTIVE", true, 0, 10);
        assertPolicyNames(getPolicies(request), ACTIVE, ACTIVE_UPDATED, INACTIVE_UPDATED);
        Assert.assertEquals(getCount(request), 3);
    }

    @Test
    public void testInactiveStatusIncludesUpdatedPolicies() throws Exception {
        PolicyPaginationRequest request = request("INACTIVE", true, 0, 10);
        assertPolicyNames(getPolicies(request), ACTIVE_UPDATED, INACTIVE, INACTIVE_UPDATED);
        Assert.assertEquals(getCount(request), 3);
    }

    @Test
    public void testUpdatedWithoutStatusReturnsOnlyUpdatedPolicies() throws Exception {
        PolicyPaginationRequest request = request(null, true, 0, 10);
        assertPolicyNames(getPolicies(request), ACTIVE_UPDATED, INACTIVE_UPDATED);
        Assert.assertEquals(getCount(request), 2);
    }

    @Test
    public void testUpdatedFalseKeepsExistingStatusBehavior() throws Exception {
        PolicyPaginationRequest request = request("ACTIVE", false, 0, 10);
        assertPolicyNames(getPolicies(request), ACTIVE, ACTIVE_UPDATED);
        Assert.assertEquals(getCount(request), 2);

        PolicyPaginationRequest unfiltered = request(null, false, 0, 10);
        Assert.assertEquals(getPolicies(unfiltered).size(), 4);
        Assert.assertEquals(getCount(unfiltered), 4);
    }

    @Test
    public void testOtherFiltersApplyToStatusUpdatedUnion() throws Exception {
        PolicyPaginationRequest request = request("INACTIVE", true, 0, 10);
        request.setName("updated");
        request.setType("GENERAL");
        request.setDeviceType("android");
        assertPolicyNames(getPolicies(request), ACTIVE_UPDATED, INACTIVE_UPDATED);
        Assert.assertEquals(getCount(request), 2);
    }

    @Test
    public void testFilteredCountIsIndependentOfPageSize() throws Exception {
        PolicyPaginationRequest request = request(null, true, 0, 1);
        Assert.assertEquals(getPolicies(request).size(), 1);
        Assert.assertEquals(getCount(request), 2);
    }

    private PolicyPaginationRequest request(String status, boolean includeUpdated, int offset, int limit) {
        PolicyPaginationRequest request = new PolicyPaginationRequest(offset, limit);
        request.setStatus(status);
        request.setIncludeUpdated(includeUpdated);
        return request;
    }

    private List<Policy> getPolicies(PolicyPaginationRequest request) throws Exception {
        try {
            PolicyManagementDAOFactory.openConnection();
            return policyDAO.getAllPolicies(request);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    private int getCount(PolicyPaginationRequest request) throws Exception {
        try {
            PolicyManagementDAOFactory.openConnection();
            return policyDAO.getPolicyCount(request);
        } finally {
            PolicyManagementDAOFactory.closeConnection();
        }
    }

    private void assertPolicyNames(List<Policy> policies, String... expectedNames) {
        Set<String> actualNames = new HashSet<>();
        for (Policy policy : policies) {
            actualNames.add(policy.getPolicyName());
        }
        Set<String> expected = new HashSet<>();
        java.util.Collections.addAll(expected, expectedNames);
        Assert.assertEquals(actualNames, expected);
        Assert.assertEquals(policies.size(), expected.size(), "Policies must not be duplicated");
    }

    private void insertPolicy(String name, int tenantId, String deviceType, String policyType,
                              boolean active, boolean updated, int priority) throws Exception {
        try (Connection connection = getDataSource().getConnection()) {
            int profileId;
            String profileSql = "INSERT INTO DM_PROFILE (PROFILE_NAME, TENANT_ID, DEVICE_TYPE, CREATED_TIME, " +
                    "UPDATED_TIME) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(profileSql, Statement.RETURN_GENERATED_KEYS)) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                statement.setString(1, name + "-profile");
                statement.setInt(2, tenantId);
                statement.setString(3, deviceType);
                statement.setTimestamp(4, now);
                statement.setTimestamp(5, now);
                statement.executeUpdate();
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    Assert.assertTrue(keys.next());
                    profileId = keys.getInt(1);
                }
            }

            String policySql = "INSERT INTO DM_POLICY (NAME, DESCRIPTION, TENANT_ID, PROFILE_ID, " +
                    "OWNERSHIP_TYPE, COMPLIANCE, PRIORITY, ACTIVE, UPDATED, POLICY_TYPE) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(policySql)) {
                statement.setString(1, name);
                statement.setString(2, name);
                statement.setInt(3, tenantId);
                statement.setInt(4, profileId);
                statement.setString(5, "ANY");
                statement.setString(6, "enforce");
                statement.setInt(7, priority);
                statement.setInt(8, active ? 1 : 0);
                statement.setInt(9, updated ? 1 : 0);
                statement.setString(10, policyType);
                statement.executeUpdate();
            }
        }
    }
}
