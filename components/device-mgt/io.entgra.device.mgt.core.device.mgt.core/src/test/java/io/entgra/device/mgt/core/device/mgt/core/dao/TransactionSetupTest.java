/*
 * Copyright (c) 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.core.archival.dao.ArchivalDestinationDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.archival.dao.ArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.notification.mgt.dao.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class TransactionSetupTest {
    @DataProvider(name = "factoriesAndScenarios")
    public Object[][] factoriesAndScenarios() {
        List<Object[]> cases = new ArrayList<>();
        Class<?>[] factories = {DeviceManagementDAOFactory.class, GroupManagementDAOFactory.class,
                TrackerManagementDAOFactory.class, EventManagementDAOFactory.class,
                DeviceFeatureOperationsDAOFactory.class, MetadataManagementDAOFactory.class,
                OperationManagementDAOFactory.class, NotificationManagementDAOFactory.class,
                ArchivalSourceDAOFactory.class, ArchivalDestinationDAOFactory.class};
        for (Class<?> factory : factories) {
            for (String scenario : new String[]{"success", "acquisition", "setup", "close", "active"}) {
                cases.add(new Object[]{factory, scenario});
            }
        }
        return cases.toArray(new Object[0][]);
    }

    @Test(dataProvider = "factoriesAndScenarios")
    public void transactionSetupPreservesConnectionOwnership(Class<?> factory, String scenario) throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        SQLException failure = new SQLException("Setup failure");
        SQLException closeFailure = new SQLException("Close failure");
        when(source.getConnection()).thenReturn(connection);
        if ("acquisition".equals(scenario)) {
            when(source.getConnection()).thenThrow(failure);
        } else if ("setup".equals(scenario) || "close".equals(scenario)) {
            doThrow(failure).when(connection).setAutoCommit(false);
        }
        if ("close".equals(scenario)) {
            doThrow(closeFailure).when(connection).close();
        }
        Field sourceField = factory.getDeclaredField("dataSource");
        Field currentField = factory.getDeclaredField("currentConnection");
        sourceField.setAccessible(true);
        currentField.setAccessible(true);
        Object previousSource = sourceField.get(null);
        @SuppressWarnings("unchecked")
        ThreadLocal<Connection> current = (ThreadLocal<Connection>) currentField.get(null);
        Connection previousConnection = current.get();
        try {
            sourceField.set(null, source);
            current.remove();
            if ("active".equals(scenario)) {
                current.set(connection);
            }
            Throwable thrown = null;
            try {
                factory.getMethod("beginTransaction").invoke(null);
            } catch (InvocationTargetException e) {
                thrown = e.getCause();
            }
            if ("success".equals(scenario)) {
                Assert.assertNull(thrown);
                Assert.assertSame(current.get(), connection);
                verify(connection).setAutoCommit(false);
                verify(connection, never()).close();
            } else if ("active".equals(scenario)) {
                Assert.assertNotNull(thrown);
                Assert.assertSame(current.get(), connection);
                verify(source, never()).getConnection();
                verify(connection, never()).close();
            } else {
                Assert.assertNotNull(thrown);
                Assert.assertSame(thrown.getCause(), failure);
                Assert.assertNull(current.get());
                verify(connection, times("acquisition".equals(scenario) ? 0 : 1)).close();
                Assert.assertEquals(failure.getSuppressed().length, "close".equals(scenario) ? 1 : 0);
                if ("close".equals(scenario)) {
                    Assert.assertSame(failure.getSuppressed()[0], closeFailure);
                }
            }
        } finally {
            sourceField.set(null, previousSource);
            current.remove();
            if (previousConnection != null) {
                current.set(previousConnection);
            }
        }
    }
}
