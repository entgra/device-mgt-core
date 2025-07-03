/*
 *  Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.notification.mgt.core.dao;

import io.entgra.device.mgt.core.notification.mgt.common.dto.Notification;
import io.entgra.device.mgt.core.notification.mgt.common.dto.UserNotificationAction;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.core.dao.factory.NotificationManagementDAOFactory;
import io.entgra.device.mgt.core.notification.mgt.core.dao.impl.GenericNotificationManagementDAOImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.powermock.reflect.Whitebox;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@PrepareForTest({LogFactory.class, NotificationManagementDAOFactory.class, PrivilegedCarbonContext.class})
public class GenericNotificationManagementDAOTest extends PowerMockTestCase {

    private static final Log log = LogFactory.getLog(GenericNotificationManagementDAOTest.class);

    private GenericNotificationManagementDAOImpl genericNotificationManagementDAO;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private Log mockLog;

    @Mock
    private PrivilegedCarbonContext mockPrivilegedCarbonContext;

    private final String TEST_USERNAME = "testUser";
    private final int TEST_TENANT_ID = -1234;
    private final int TEST_NOTIFICATION_CONFIG_ID = 1;
    private final String TEST_NOTIFICATION_TYPE = "INFO";
    private final String TEST_DESCRIPTION = "Test notification description.";

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        Whitebox.setInternalState(GenericNotificationManagementDAOImpl.class,
                "log", mockLog);
        PowerMockito.mockStatic(LogFactory.class);
        when(LogFactory.getLog(any(Class.class))).thenReturn(mockLog);
        when(mockLog.isDebugEnabled()).thenReturn(true);
        when(mockLog.isErrorEnabled()).thenReturn(true);
        PowerMockito.mockStatic(NotificationManagementDAOFactory.class);
        when(NotificationManagementDAOFactory.getConnection()).thenReturn(mockConnection);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        when(PrivilegedCarbonContext.getThreadLocalCarbonContext())
                .thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getTenantId()).thenReturn(TEST_TENANT_ID);
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), anyInt()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        genericNotificationManagementDAO = new GenericNotificationManagementDAOImpl();
    }

    @Test
    public void testGetLatestNotifications_success() throws Exception {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("NOTIFICATION_ID")).thenReturn(1, 2);
        when(mockResultSet.getInt("NOTIFICATION_CONFIG_ID"))
                .thenReturn(TEST_NOTIFICATION_CONFIG_ID,
                        TEST_NOTIFICATION_CONFIG_ID + 1);
        when(mockResultSet.getInt("TENANT_ID")).thenReturn(TEST_TENANT_ID, TEST_TENANT_ID);
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Desc1", "Desc2");
        when(mockResultSet.getInt("PRIORITY")).thenReturn(0, 1);
        when(mockResultSet.getTimestamp("CREATED_TIMESTAMP"))
                .thenReturn(now, new Timestamp(now.getTime() - 1000));
        List<Notification> result = genericNotificationManagementDAO
                .getLatestNotifications(0, 5);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getNotificationId(), 1);
        Assert.assertEquals(result.get(1).getNotificationId(), 2);
        verify(mockPreparedStatement, times(1)).setInt(1, 5);
        verify(mockPreparedStatement, times(1)).setInt(2, 0);
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testGetLatestNotifications_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("DB Connection error"));
        try {
            genericNotificationManagementDAO.getLatestNotifications(0, 5);
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error occurred while retrieving notifications from DB"),
                            any(SQLException.class));
        }
    }

    @Test
    public void testGetNotificationsByIds_success() throws Exception {
        List<Integer> notificationIds = Arrays.asList(10, 20);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("NOTIFICATION_ID")).thenReturn(10, 20);
        when(mockResultSet.getString("DESCRIPTION"))
                .thenReturn("Notification 10", "Notification 20");
        when(mockResultSet.getString("TYPE")).thenReturn("Alert", "Info");
        when(mockResultSet.getTimestamp("CREATED_TIMESTAMP"))
                .thenReturn(now, new Timestamp(now.getTime() - 5000));
        List<Notification> result = genericNotificationManagementDAO
                .getNotificationsByIds(notificationIds);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getNotificationId(), 10);
        Assert.assertEquals(result.get(1).getNotificationId(), 20);
        verify(mockPreparedStatement, times(1))
                .setInt(1, TEST_TENANT_ID);
        verify(mockPreparedStatement, times(1))
                .setInt(2, 10);
        verify(mockPreparedStatement, times(1))
                .setInt(3, 20);
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testGetNotificationsByIds_emptyList() throws Exception {
        List<Integer> notificationIds = Collections.emptyList();
        List<Notification> result = genericNotificationManagementDAO
                .getNotificationsByIds(notificationIds);
        Assert.assertTrue(result.isEmpty());
        verify(mockConnection, never()).prepareStatement(anyString());
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testGetNotificationsByIds_sqlException() throws Exception {
        List<Integer> notificationIds = Arrays.asList(10);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        try {
            genericNotificationManagementDAO.getNotificationsByIds(notificationIds);
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error occurred while retrieving notifications by IDs from DB"),
                            any(SQLException.class));
        }
    }

    @Test
    public void testGetNotificationActionsByUser_success_withIsRead() throws Exception {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("NOTIFICATION_ID")).thenReturn(101);
        when(mockResultSet.getInt("ACTION_ID")).thenReturn(1);
        when(mockResultSet.getBoolean("IS_READ")).thenReturn(true);
        List<UserNotificationAction> result = genericNotificationManagementDAO
                .getNotificationActionsByUser(TEST_USERNAME, 10, 0, true);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getNotificationId(), 101);
        Assert.assertTrue(result.get(0).isRead());
        verify(mockPreparedStatement, times(1))
                .setString(1, TEST_USERNAME);
        verify(mockPreparedStatement, times(1))
                .setBoolean(2, true);
        verify(mockPreparedStatement, times(1))
                .setInt(3, 10);
        verify(mockPreparedStatement, never()).setInt(4, 0);
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testGetNotificationActionsByUser_success_withoutIsRead() throws Exception {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("NOTIFICATION_ID")).thenReturn(102);
        when(mockResultSet.getInt("ACTION_ID")).thenReturn(2);
        when(mockResultSet.getBoolean("IS_READ")).thenReturn(false);
        List<UserNotificationAction> result = genericNotificationManagementDAO
                .getNotificationActionsByUser(TEST_USERNAME, 0, 0, null);
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.get(0).getNotificationId(), 102);
        Assert.assertFalse(result.get(0).isRead());
        verify(mockPreparedStatement, times(1))
                .setString(1, TEST_USERNAME);
        verify(mockPreparedStatement, never()).setBoolean(anyInt(), anyBoolean());
        verify(mockPreparedStatement, never()).setInt(anyInt(), anyInt());
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testGetNotificationActionsByUser_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        try {
            genericNotificationManagementDAO
                    .getNotificationActionsByUser(TEST_USERNAME, 10, 0, true);
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error occurred while retrieving notification actions for user: "
                            + TEST_USERNAME), any(SQLException.class));
        }
    }

    @Test
    public void testUpdateNotificationAction_read_success() throws Exception {
        List<Integer> notificationIds = Arrays.asList(1, 2);
        when(mockPreparedStatement.executeUpdate()).thenReturn(2);
        genericNotificationManagementDAO
                .updateNotificationAction(notificationIds, TEST_USERNAME, "READ");
        verify(mockPreparedStatement, times(1))
                .setBoolean(1, true);
        verify(mockPreparedStatement, times(1))
                .setString(2, TEST_USERNAME);
        verify(mockPreparedStatement, times(1))
                .setInt(3, 1);
        verify(mockPreparedStatement, times(1))
                .setInt(4, 2);
        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testUpdateNotificationAction_unread_success() throws Exception {
        List<Integer> notificationIds = Collections.singletonList(3);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        genericNotificationManagementDAO
                .updateNotificationAction(notificationIds, TEST_USERNAME, "UNREAD");
        verify(mockPreparedStatement, times(1))
                .setBoolean(1, false);
        verify(mockPreparedStatement, times(1))
                .setString(2, TEST_USERNAME);
        verify(mockPreparedStatement, times(1))
                .setInt(3, 3);
        verify(mockPreparedStatement, times(1))
                .executeUpdate();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testUpdateNotificationAction_emptyList() throws Exception {
        genericNotificationManagementDAO
                .updateNotificationAction(Collections.emptyList(), TEST_USERNAME, "READ");
        verify(mockConnection, never()).prepareStatement(anyString());
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class, 
            expectedExceptionsMessageRegExp = "Invalid action type: INVALID_ACTION")
    public void testUpdateNotificationAction_invalidActionType() throws Exception {
        genericNotificationManagementDAO
                .updateNotificationAction(Collections.singletonList(1), TEST_USERNAME,
                        "INVALID_ACTION");
        verify(mockConnection, never()).prepareStatement(anyString());
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testUpdateNotificationAction_sqlException() throws Exception {
        List<Integer> notificationIds = Collections.singletonList(1);
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        try {
            genericNotificationManagementDAO
                    .updateNotificationAction(notificationIds, TEST_USERNAME, "READ");
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error occurred while updating notification actions for user: "
                            + TEST_USERNAME), any(SQLException.class));
        }
    }

    @Test
    public void testGetAllNotificationUserActions_success() throws Exception {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getInt("NOTIFICATION_ID")).thenReturn(10, 20);
        when(mockResultSet.getInt("ACTION_ID")).thenReturn(1, 2);
        when(mockResultSet.getBoolean("IS_READ")).thenReturn(false, true);
        when(mockResultSet.getString("USERNAME")).thenReturn("userA", "userB");
        when(mockResultSet.getTimestamp("ACTION_TIMESTAMP"))
                .thenReturn(now, new Timestamp(now.getTime() - 1000));
        List<UserNotificationAction> result = genericNotificationManagementDAO
                .getAllNotificationUserActions();
        Assert.assertFalse(result.isEmpty());
        Assert.assertEquals(result.size(), 2);
        Assert.assertEquals(result.get(0).getUsername(), "userA");
        Assert.assertEquals(result.get(1).getUsername(), "userB");
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testGetAllNotificationUserActions_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        try {
            genericNotificationManagementDAO.getAllNotificationUserActions();
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error occurred while retrieving all notification user actions."), 
                            any(SQLException.class));
        }
    }

    @Test
    public void testGetNotificationActionsCountByUser_success_withIsRead() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(5);
        int count = genericNotificationManagementDAO
                .getNotificationActionsCountByUser(TEST_USERNAME, true);
        Assert.assertEquals(count, 5);
        verify(mockPreparedStatement, times(1))
                .setString(1, TEST_USERNAME);
        verify(mockPreparedStatement, times(1))
                .setBoolean(2, true);
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testGetNotificationActionsCountByUser_success_withoutIsRead() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(10);
        int count = genericNotificationManagementDAO
                .getNotificationActionsCountByUser(TEST_USERNAME, null);
        Assert.assertEquals(count, 10);
        verify(mockPreparedStatement, times(1))
                .setString(1, TEST_USERNAME);
        verify(mockPreparedStatement, never()).setBoolean(anyInt(), anyBoolean());
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testGetNotificationActionsCountByUser_noResults() throws Exception {
        when(mockResultSet.next()).thenReturn(false); 
        int count = genericNotificationManagementDAO
                .getNotificationActionsCountByUser(TEST_USERNAME, false);
        Assert.assertEquals(count, 0);
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testGetNotificationActionsCountByUser_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        try {
            genericNotificationManagementDAO
                    .getNotificationActionsCountByUser(TEST_USERNAME, true);
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error counting user notifications"), any(SQLException.class));
        }
    }

    @Test
    public void testGetUnreadNotificationCountForUser_success() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("UNREAD_COUNT")).thenReturn(3);
        int count = genericNotificationManagementDAO.
                getUnreadNotificationCountForUser(TEST_USERNAME);
        Assert.assertEquals(count, 3);
        verify(mockPreparedStatement, times(1))
                .setString(1, TEST_USERNAME);
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testGetUnreadNotificationCountForUser_noUnread() throws Exception {
        when(mockResultSet.next()).thenReturn(false);
        int count = genericNotificationManagementDAO
                .getUnreadNotificationCountForUser(TEST_USERNAME);
        Assert.assertEquals(count, 0);
        verify(mockPreparedStatement, times(1))
                .setString(1, TEST_USERNAME);
        verify(mockPreparedStatement, times(1)).executeQuery();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testGetUnreadNotificationCountForUser_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB error"));
        try {
            genericNotificationManagementDAO.getUnreadNotificationCountForUser(TEST_USERNAME);
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error retrieving unread notification count for user: "
                                    + TEST_USERNAME), any(SQLException.class));
        }
    }

    @Test
    public void testInsertNotification_success() throws Exception {
        int expectedGeneratedId = 123;
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(expectedGeneratedId);
        int returnedId = genericNotificationManagementDAO
                .insertNotification(TEST_TENANT_ID, 
                        TEST_NOTIFICATION_CONFIG_ID, 
                        TEST_NOTIFICATION_TYPE, 
                        TEST_DESCRIPTION);
        Assert.assertEquals(returnedId, expectedGeneratedId);
        verify(mockPreparedStatement, times(1))
                .setInt(1, TEST_NOTIFICATION_CONFIG_ID);
        verify(mockPreparedStatement, times(1))
                .setInt(2, TEST_TENANT_ID);
        verify(mockPreparedStatement, times(1))
                .setString(3, TEST_DESCRIPTION);
        verify(mockPreparedStatement, times(1))
                .setString(4, TEST_NOTIFICATION_TYPE);
        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockPreparedStatement, times(1)).getGeneratedKeys();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testInsertNotification_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString(), 
                anyInt())).thenThrow(new SQLException("Insert failed"));
        try {
            genericNotificationManagementDAO
                    .insertNotification(TEST_TENANT_ID, 
                            TEST_NOTIFICATION_CONFIG_ID, 
                            TEST_NOTIFICATION_TYPE, 
                            TEST_DESCRIPTION);
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error inserting notification"), any(SQLException.class));
        }
    }

    @Test
    public void testInsertNotificationUserActions_success() throws Exception {
        int notificationId = 100;
        List<String> usernames = Arrays.asList("user1", "user2");
        when(mockPreparedStatement.executeBatch()).thenReturn(new int[]{1, 1});
        genericNotificationManagementDAO.insertNotificationUserActions(notificationId, usernames);
        verify(mockPreparedStatement, times(2)).setInt(1, notificationId);
        verify(mockPreparedStatement, times(1)).setString(2, "user1");
        verify(mockPreparedStatement, times(1)).setString(2, "user2");
        verify(mockPreparedStatement, times(2)).setBoolean(3, false);
        verify(mockPreparedStatement, times(2)).addBatch();
        verify(mockPreparedStatement, times(1)).executeBatch();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testInsertNotificationUserActions_emptyUsernames() throws Exception {
        int notificationId = 100;
        List<String> usernames = Collections.emptyList();
        genericNotificationManagementDAO.insertNotificationUserActions(notificationId, usernames);
        verify(mockConnection, never()).prepareStatement(anyString());
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    } // Don't proceed if there are no usernames

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testInsertNotificationUserActions_sqlException() throws Exception {
        int notificationId = 100;
        List<String> usernames = Arrays.asList("user1");
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Batch insert failed"));
        try {
            genericNotificationManagementDAO.insertNotificationUserActions(notificationId, usernames);
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error inserting notification user actions"),
                            any(SQLException.class));
        }
    }

    @Test
    public void testDeleteUserNotifications_success() throws Exception {
        List<Integer> notificationIds = Arrays.asList(1, 2);
        when(mockPreparedStatement.executeUpdate()).thenReturn(2);
        genericNotificationManagementDAO.deleteUserNotifications(notificationIds, TEST_USERNAME);
        verify(mockPreparedStatement, times(1)).setString(1, TEST_USERNAME);
        verify(mockPreparedStatement, times(1)).setInt(2, 1);
        verify(mockPreparedStatement, times(1)).setInt(3, 2);
        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test
    public void testDeleteUserNotifications_emptyList() throws Exception {
        genericNotificationManagementDAO
                .deleteUserNotifications(Collections.emptyList(), TEST_USERNAME);
        verify(mockConnection, never()).prepareStatement(anyString());
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testDeleteUserNotifications_sqlException() throws Exception {
        List<Integer> notificationIds = Collections.singletonList(1);
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Delete failed"));
        try {
            genericNotificationManagementDAO.deleteUserNotifications(notificationIds, TEST_USERNAME);
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error occurred while deleting notifications for user: "
                                    + TEST_USERNAME), any(SQLException.class));
        }
    }

    @Test
    public void testDeleteAllUserNotifications_success() throws Exception {
        when(mockPreparedStatement.executeUpdate()).thenReturn(5);
        genericNotificationManagementDAO.deleteAllUserNotifications(TEST_USERNAME);
        verify(mockPreparedStatement, times(1)).setString(1, TEST_USERNAME);
        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(mockLog, never()).error(anyString(), any(Throwable.class));
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testDeleteAllUserNotifications_sqlException() throws Exception {
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Delete all failed"));
        try {
            genericNotificationManagementDAO.deleteAllUserNotifications(TEST_USERNAME);
        } finally {
            verify(mockLog, times(1))
                    .error(eq("Error occurred while deleting all notifications for user: "
                                    + TEST_USERNAME), any(SQLException.class));
        }
    }
}
