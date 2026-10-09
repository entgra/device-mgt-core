/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package io.entgra.device.mgt.core.policy.mgt.common;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Policy;

import java.util.Set;

/**
 * A policy evaluation point that accepts the policies which initiated an apply-changes request.
 *
 * <p>Simple evaluation still determines the effective policy from every applicable policy. The supplied IDs
 * identify the changes being processed and are used by enforcement to decide whether an unchanged winner needs
 * to be re-applied.</p>
 */
public interface PolicySelectionEvaluationPoint extends PolicyEvaluationPoint {

    Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier, Set<Integer> policyIds)
            throws PolicyEvaluationException;
}
