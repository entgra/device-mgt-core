/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package io.entgra.device.mgt.core.policy.mgt.common;

/**
 * Signals that a selective policy apply request cannot be evaluated.
 */
public class InvalidPolicySelectionException extends PolicyManagementException {

    public InvalidPolicySelectionException(String message) {
        super(message);
    }

    public InvalidPolicySelectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
