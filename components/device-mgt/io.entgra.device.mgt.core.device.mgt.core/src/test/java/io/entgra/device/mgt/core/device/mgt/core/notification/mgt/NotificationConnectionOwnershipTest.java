package io.entgra.device.mgt.core.device.mgt.core.notification.mgt;

import io.entgra.device.mgt.core.device.mgt.core.notification.mgt.dao.NotificationManagementDAOFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class NotificationConnectionOwnershipTest {
    @DataProvider(name = "scenarios")
    public Object[][] scenarios() {
        return new Object[][]{{"acquisition"}, {"active"}};
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
        Field sourceField = NotificationManagementDAOFactory.class.getDeclaredField("dataSource");
        Field engineField = NotificationManagementDAOFactory.class.getDeclaredField("databaseEngine");
        Field currentField = NotificationManagementDAOFactory.class.getDeclaredField("currentConnection");
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
            NotificationManagementServiceImpl service = new NotificationManagementServiceImpl();
            Exception thrown = null;
            try {
                service.getAllNotifications();
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
