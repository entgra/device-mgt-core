/*
 * Copyright (c) 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.entgra.device.mgt.core.certificate.mgt.core.dao;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CertificateManagementDAOFactoryTest {

    @DataProvider(name = "metadataFailures")
    public Object[][] metadataFailures() {
        return new Object[][]{{"none"}, {"metadata"}, {"productName"}};
    }

    @Test(dataProvider = "metadataFailures")
    public void initializationClosesEveryBorrowedConnection(String failure) throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metadata = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(metadata);
        when(metadata.getDatabaseProductName()).thenReturn("H2");
        if ("metadata".equals(failure)) {
            when(connection.getMetaData()).thenThrow(new SQLException("Metadata unavailable"));
        } else if ("productName".equals(failure)) {
            when(metadata.getDatabaseProductName()).thenThrow(new SQLException("Product name unavailable"));
        }

        // Restore shared factory state so these checks do not affect other certificate tests.
        Field sourceField = CertificateManagementDAOFactory.class.getDeclaredField("dataSource");
        Field engineField = CertificateManagementDAOFactory.class.getDeclaredField("databaseEngine");
        sourceField.setAccessible(true);
        engineField.setAccessible(true);
        Object previousSource = sourceField.get(null);
        Object previousEngine = engineField.get(null);
        try {
            for (int i = 0; i < 25; i++) {
                CertificateManagementDAOFactory.init(dataSource);
            }
            verify(dataSource, times(25)).getConnection();
            verify(connection, times(25)).close();
        } finally {
            sourceField.set(null, previousSource);
            engineField.set(null, previousEngine);
        }
    }
}
