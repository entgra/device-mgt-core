/*
 * Copyright (c) 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.device.mgt.extensions.device.type.template.dao;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class DeviceTypeDAOHandlerTransactionTest {
    @DataProvider(name = "scenarios")
    public Object[][] scenarios() {
        return new Object[][]{{"idle"}, {"active"}, {"success"}, {"acquisition"}, {"setup"}};
    }

    @Test(dataProvider = "scenarios")
    public void beginTransactionRespectsExistingConnection(String scenario) throws Exception {
        Object target = mock(DeviceTypeDAOHandler.class, CALLS_REAL_METHODS);
        Field sourceField = DeviceTypeDAOHandler.class.getDeclaredField("dataSource");
        Field currentField = DeviceTypeDAOHandler.class.getDeclaredField("currentConnection");
        sourceField.setAccessible(true);
        currentField.setAccessible(true);
        currentField.set(target, new ThreadLocal<Connection>());
        @SuppressWarnings("unchecked")
        ThreadLocal<Connection> current = (ThreadLocal<Connection>) currentField.get(target);
        Object previousSource = sourceField.get(target);
        Connection previousConnection = current.get();
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        SQLException failure = new SQLException("Connection setup failed");
        when(source.getConnection()).thenReturn(connection);
        when(connection.getAutoCommit()).thenReturn(!"active".equals(scenario));
        if ("acquisition".equals(scenario)) {
            when(source.getConnection()).thenThrow(failure);
        } else if ("setup".equals(scenario)) {
            doThrow(failure).when(connection).setAutoCommit(false);
        }
        try {
            current.remove();
            sourceField.set(target, source);
            boolean existing = "idle".equals(scenario) || "active".equals(scenario);
            if (existing) {
                current.set(connection);
            }
            Throwable thrown = null;
            try {
                DeviceTypeDAOHandler.class.getMethod("beginTransaction").invoke(target);
            } catch (InvocationTargetException e) {
                thrown = e.getCause();
            }
            if (existing) {
                Assert.assertNotNull(thrown);
                Assert.assertEquals(thrown.getClass().getSimpleName(), "IllegalTransactionStateException");
                Assert.assertSame(current.get(), connection);
                verify(source, never()).getConnection();
                verify(connection, never()).setAutoCommit(false);
                verify(connection, never()).close();
                verify(connection, never()).rollback();
            } else if ("success".equals(scenario)) {
                Assert.assertNull(thrown);
                Assert.assertSame(current.get(), connection);
                verify(connection).setAutoCommit(false);
                verify(connection, never()).close();
            } else {
                Assert.assertNotNull(thrown);
                Assert.assertSame(thrown.getCause(), failure);
                Assert.assertNull(current.get());
                verify(connection, times("setup".equals(scenario) ? 1 : 0)).close();
            }
        } finally {
            sourceField.set(target, previousSource);
            current.remove();
            if (previousConnection != null) {
                current.set(previousConnection);
            }
        }
    }
}
