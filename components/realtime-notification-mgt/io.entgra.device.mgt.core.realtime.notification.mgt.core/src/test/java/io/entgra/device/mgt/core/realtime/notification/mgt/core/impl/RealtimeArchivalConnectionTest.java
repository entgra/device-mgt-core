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

package io.entgra.device.mgt.core.realtime.notification.mgt.core.impl;

import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationArchivalException;
import io.entgra.device.mgt.core.realtime.notification.mgt.common.exception.RealtimeNotificationArchivalDAOException;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.RealtimeNotificationArchivalDAO;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.RealtimeNotificationManagementDAO;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.RealtimeNotificationManagementDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.archive.RealtimeNotificationArchivalDestDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.dao.factory.archive.RealtimeNotificationArchivalSourceDAOFactory;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeNotificationEventBroker;
import io.entgra.device.mgt.core.realtime.notification.mgt.core.util.RealtimeNotificationListener;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class RealtimeArchivalConnectionTest {
    @DataProvider(name = "cases")
    public Object[][] cases() {
        List<Object[]> cases = new ArrayList<>();
        for (boolean all : new boolean[]{false, true}) {
            for (String failure : new String[]{"none", "sse", "count", "source", "destination", "dao"}) {
                cases.add(new Object[]{all, failure});
            }
        }
        return cases.toArray(new Object[0][]);
    }

    @Test(dataProvider = "cases")
    public void archivalReleasesConnectionsBeforeDelivery(boolean all, String failure) throws Exception {
        Class<?>[] factories = {RealtimeNotificationArchivalDestDAOFactory.class,
                RealtimeNotificationArchivalSourceDAOFactory.class, RealtimeNotificationManagementDAOFactory.class};
        Object[][] previous = new Object[3][3];
        Connection[] connections = new Connection[3];
        DataSource[] sources = new DataSource[3];
        List<ThreadLocal<Connection>> currents = new ArrayList<>();
        Field listenersField = field(RealtimeNotificationEventBroker.class, "listeners");
        @SuppressWarnings("unchecked")
        List<RealtimeNotificationListener> listeners = (List<RealtimeNotificationListener>) listenersField.get(null);
        List<RealtimeNotificationListener> previousListeners = new ArrayList<>(listeners);
        try {
            listeners.clear();
            for (int i = 0; i < factories.length; i++) {
                Field source = field(factories[i], "dataSource");
                Field product = field(factories[i], "productName");
                @SuppressWarnings("unchecked")
                ThreadLocal<Connection> current = (ThreadLocal<Connection>)
                        field(factories[i], "currentConnection").get(null);
                previous[i] = new Object[]{source.get(null), product.get(null), current.get()};
                currents.add(current);
                current.remove();
                connections[i] = mock(Connection.class);
                sources[i] = mock(DataSource.class);
                when(sources[i].getConnection()).thenReturn(connections[i]);
                source.set(null, sources[i]);
                product.set(null, "H2");
            }
            if ("destination".equals(failure)) {
                when(sources[0].getConnection()).thenThrow(new SQLException("Destination unavailable"));
            } else if ("source".equals(failure)) {
                when(sources[1].getConnection()).thenThrow(new SQLException("Source unavailable"));
            } else if ("count".equals(failure)) {
                when(sources[2].getConnection()).thenThrow(new SQLException("Count unavailable"));
            }
            // Skip the constructor's server-configured DAO creation; exercise real service methods below.
            RealtimeNotificationManagementServiceImpl service = mock(RealtimeNotificationManagementServiceImpl.class, CALLS_REAL_METHODS);
            RealtimeNotificationArchivalDAO archiveDAO = mock(RealtimeNotificationArchivalDAO.class);
            RealtimeNotificationManagementDAO notificationDAO = mock(RealtimeNotificationManagementDAO.class);
            field(RealtimeNotificationManagementServiceImpl.class, "notificationArchiveDAO").set(service, archiveDAO);
            field(RealtimeNotificationManagementServiceImpl.class, "notificationDAO").set(service, notificationDAO);
            Map<String, List<Integer>> expected = Collections.singletonMap("archived", Collections.singletonList(1));
            when(archiveDAO.archiveUserNotifications(anyList(), anyString())).thenReturn(expected);
            if ("dao".equals(failure)) {
                doThrow(new RealtimeNotificationArchivalDAOException("Archive failed"))
                        .when(archiveDAO).archiveAllUserNotifications(anyString());
                when(archiveDAO.archiveUserNotifications(anyList(), anyString()))
                        .thenThrow(new RealtimeNotificationArchivalDAOException("Archive failed"));
            }
            when(notificationDAO.getUnreadNotificationCountForUser("user")).thenAnswer(invocation -> {
                verify(connections[0]).close();
                verify(connections[1]).close();
                return 2;
            });
            RealtimeNotificationListener listener = mock(RealtimeNotificationListener.class);
            doAnswer(invocation -> {
                for (int i = 0; i < connections.length; i++) {
                    verify(connections[i]).close();
                    Assert.assertNull(currents.get(i).get());
                }
                if ("sse".equals(failure)) {
                    throw new IllegalStateException("Delivery failed");
                }
                return null;
            }).when(listener).onMessage(anyString(), anyList());
            RealtimeNotificationEventBroker.registerListener(listener);
            boolean archivalFailure = "source".equals(failure) || "destination".equals(failure)
                    || "dao".equals(failure);
            try {
                if (all) {
                    service.archiveAllUserNotifications("user");
                } else {
                    Assert.assertSame(service.archiveUserNotifications(Collections.singletonList(1), "user"), expected);
                }
                Assert.assertFalse(archivalFailure, "Archival failure must be propagated");
            } catch (RealtimeNotificationArchivalException e) {
                Assert.assertTrue(archivalFailure, "Post-commit refresh must not fail archival");
            }
            verify(listener, times(archivalFailure || "count".equals(failure) ? 0 : 1))
                    .onMessage(anyString(), anyList());
            for (int i = 0; i < connections.length; i++) {
                boolean acquired = !"destination".equals(failure)
                        && (i == 0 || !"source".equals(failure))
                        && (i != 2 || (!archivalFailure && !"count".equals(failure)));
                verify(connections[i], times(acquired ? 1 : 0)).close();
                Assert.assertNull(currents.get(i).get());
                if (i < 2 && !archivalFailure) {
                    verify(connections[i]).commit();
                    verify(connections[i], never()).rollback();
                }
                if (i < 2 && archivalFailure && acquired) {
                    verify(connections[i]).rollback();
                }
            }
        } finally {
            listeners.clear();
            listeners.addAll(previousListeners);
            for (int i = 0; i < currents.size(); i++) {
                field(factories[i], "dataSource").set(null, previous[i][0]);
                field(factories[i], "productName").set(null, previous[i][1]);
                currents.get(i).remove();
                if (previous[i][2] != null) {
                    currents.get(i).set((Connection) previous[i][2]);
                }
            }
        }
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
