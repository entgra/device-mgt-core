/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.impl;

import io.entgra.device.mgt.core.device.mgt.api.jaxrs.service.api.PolicyManagementService;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class PolicyManagementServiceUpdatedFilterTest {

    @Test
    public void testUpdatedQueryParameterContract() throws Exception {
        Method method = PolicyManagementService.class.getMethod("getPolicyList", String.class, String.class,
                String.class, boolean.class, String.class, String.class, int.class, int.class);
        Annotation[] updatedAnnotations = method.getParameterAnnotations()[3];

        QueryParam queryParam = findAnnotation(updatedAnnotations, QueryParam.class);
        DefaultValue defaultValue = findAnnotation(updatedAnnotations, DefaultValue.class);
        Assert.assertNotNull(queryParam);
        Assert.assertEquals(queryParam.value(), "updated");
        Assert.assertNotNull(defaultValue);
        Assert.assertEquals(defaultValue.value(), "false");
    }

    private <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> type) {
        for (Annotation annotation : annotations) {
            if (type.isInstance(annotation)) {
                return type.cast(annotation);
            }
        }
        return null;
    }
}
