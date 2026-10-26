/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */

package io.entgra.device.mgt.core.subtype.mgt.dto;

import com.google.gson.JsonElement;

public class DeviceTypeDefinition {

    private JsonElement registerMapping;
    private JsonElement supportedOperations;

    public JsonElement getRegisterMapping() {
        return registerMapping;
    }

    public void setRegisterMapping(JsonElement registerMapping) {
        this.registerMapping = registerMapping;
    }

    public JsonElement getSupportedOperations() {
        return supportedOperations;
    }

    public void setSupportedOperations(JsonElement supportedOperations) {
        this.supportedOperations = supportedOperations;
    }
}
