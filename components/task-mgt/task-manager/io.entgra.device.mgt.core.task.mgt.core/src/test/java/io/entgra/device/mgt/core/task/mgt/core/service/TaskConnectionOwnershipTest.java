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

package io.entgra.device.mgt.core.task.mgt.core.service;

import io.entgra.device.mgt.core.task.mgt.core.dao.common.TaskManagementDAOFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class TaskConnectionOwnershipTest {
    @DataProvider(name = "scenarios")
    public Object[][] scenarios() {
        return new Object[][]{{"acquisition"}, {"active"}, {"success"}, {"daoFailure"}};
    }

    @Test(dataProvider = "scenarios")
    public void cleanupRespectsConnectionOwnership(String scenario) throws Exception {
        DataSource source = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        SQLException acquisitionFailure = new SQLException("Pool exhausted");
        when(source.getConnection()).thenReturn(connection);
        if ("acquisition".equals(scenario)) {
            when(source.getConnection()).thenThrow(acquisitionFailure);
        }
        Field sourceField = TaskManagementDAOFactory.class.getDeclaredField("dataSource");
        Field engineField = TaskManagementDAOFactory.class.getDeclaredField("databaseEngine");
        Field currentField = TaskManagementDAOFactory.class.getDeclaredField("currentConnection");
        sourceField.setAccessible(true);
        engineField.setAccessible(true);
        currentField.setAccessible(true);
        Object previousSource = sourceField.get(null);
        Object previousEngine = engineField.get(null);
        @SuppressWarnings("unchecked")
        ThreadLocal<Connection> current = (ThreadLocal<Connection>) currentField.get(null);
        Connection previousConnection = current.get();
        try {
            sourceField.set(null, source);
            engineField.set(null, "H2");
            current.remove();
            if ("active".equals(scenario)) {
                current.set(connection);
            }
            TaskManagementServiceImpl service = new TaskManagementServiceImpl();
            io.entgra.device.mgt.core.task.mgt.core.dao.DynamicTaskDAO dao =
                    mock(io.entgra.device.mgt.core.task.mgt.core.dao.DynamicTaskDAO.class);
            Field daoField = TaskManagementServiceImpl.class.getDeclaredField("dynamicTaskDAO");
            daoField.setAccessible(true);
            daoField.set(service, dao);
            io.entgra.device.mgt.core.task.mgt.common.exception.TaskManagementDAOException daoFailure =
                    new io.entgra.device.mgt.core.task.mgt.common.exception.TaskManagementDAOException("Query failed");
            if ("daoFailure".equals(scenario)) {
                when(dao.getAllDynamicTasks()).thenThrow(daoFailure);
            } else {
                when(dao.getAllDynamicTasks()).thenReturn(java.util.Collections.emptyList());
            }
            Exception thrown = null;
            try {
                service.getDynamicTasksForAllTenants();
            } catch (Exception e) {
                thrown = e;
            }
            if ("active".equals(scenario)) {
                Assert.assertNotNull(thrown);
                Assert.assertTrue(thrown.getMessage().contains("already active"));
                Assert.assertSame(current.get(), connection);
                verify(source, never()).getConnection();
                verify(connection, never()).close();
            } else if ("acquisition".equals(scenario)) {
                Assert.assertNotNull(thrown);
                Throwable root = thrown;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                Assert.assertSame(root, acquisitionFailure);
                Assert.assertNull(current.get());
                verify(connection, never()).close();
            } else {
                if ("daoFailure".equals(scenario)) {
                    Assert.assertNotNull(thrown);
                    Assert.assertSame(thrown.getCause(), daoFailure);
                } else {
                    Assert.assertNull(thrown);
                }
                verify(connection).close();
                Assert.assertNull(current.get());
            }
        } finally {
            sourceField.set(null, previousSource);
            engineField.set(null, previousEngine);
            current.remove();
            if (previousConnection != null) {
                current.set(previousConnection);
            }
        }
    }
}
