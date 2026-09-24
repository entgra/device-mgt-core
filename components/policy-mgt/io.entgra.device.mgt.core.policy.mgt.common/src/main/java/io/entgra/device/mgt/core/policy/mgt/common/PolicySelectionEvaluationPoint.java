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
 * A policy evaluation point that can restrict evaluation to an explicit set of policies.
 */
public interface PolicySelectionEvaluationPoint extends PolicyEvaluationPoint {

    Policy getEffectivePolicy(DeviceIdentifier deviceIdentifier, Set<Integer> policyIds)
            throws PolicyEvaluationException;
}
