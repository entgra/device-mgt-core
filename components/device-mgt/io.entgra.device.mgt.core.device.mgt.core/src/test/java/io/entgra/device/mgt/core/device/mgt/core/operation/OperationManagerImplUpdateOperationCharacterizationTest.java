/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.operation;

import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.MonitoringOperation;
import io.entgra.device.mgt.core.device.mgt.common.OperationMonitoringTaskConfig;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.CommandOperation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationDAO;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.dao.OperationManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockObjectFactory;
import org.testng.Assert;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Characterization tests for {@link OperationManagerImpl#updateOperation(int, Operation, DeviceIdentifier)},
 * added as a before/after safety net for the Extract Method refactor of that method. These cover two paths
 * that had no prior test coverage: the SYSTEM-initiator response-persistence skip, and the
 * retry-then-succeed status update path.
 */
@PrepareForTest(OperationManagementDAOFactory.class)
@PowerMockIgnore({"org.mockito.*", "io.entgra.device.mgt.core.device.mgt.core.dao.*",
        "javax.management.*", "javax.script.*", "javax.xml.*", "org.xml.*", "org.w3c.*",
        "com.sun.org.apache.xerces.*", "org.apache.axiom.*", "org.wso2.carbon.*"})
public class OperationManagerImplUpdateOperationCharacterizationTest extends BaseDeviceManagementTest {

    private static final String DEVICE_TYPE = "CHAR_TEST_TYPE";
    private static final String SYSTEM_INITIATOR = "system";

    private OperationDAO mockOperationDAO;
    private OperationManagerImpl operationManager;
    private DeviceManagementProviderService originalProvider;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new PowerMockObjectFactory();
    }

    @BeforeClass
    public void init() throws Exception {
        // No class-level fixtures beyond what BaseDeviceManagementTest's @BeforeSuite already sets up
        // (datasource + DAO factory init); per-test mocking happens in setUp() below.
    }

    @BeforeMethod
    public void setUp() {
        PowerMockito.mockStatic(OperationManagementDAOFactory.class);
        mockOperationDAO = Mockito.mock(OperationDAO.class);
        PowerMockito.when(OperationManagementDAOFactory.getOperationDAO()).thenReturn(mockOperationDAO);
        operationManager = new OperationManagerImpl();
        originalProvider = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider();
    }

    @AfterMethod
    public void tearDown() {
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(originalProvider);
    }

    @Test(description = "SYSTEM-initiator responses must be skipped without ever persisting a response")
    public void systemInitiatorSkipsResponsePersistence() throws Exception {
        int enrolmentId = 1001;
        String operationCode = "SYSTEM-SKIP-TEST";
        DeviceIdentifier deviceId = new DeviceIdentifier("CHAR-DEVICE-1", DEVICE_TYPE);

        CommandOperation operation = new CommandOperation();
        operation.setId(501);
        operation.setCode(operationCode);
        operation.setStatus(Operation.Status.COMPLETED);
        operation.setOperationResponse("some response payload");

        Mockito.when(mockOperationDAO.updateOperationStatus(Mockito.eq(enrolmentId), Mockito.eq(operation.getId()),
                        Mockito.eq(io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation.Status.COMPLETED)))
                .thenReturn(true);
        Mockito.when(mockOperationDAO.getDeviceOperationDetails(enrolmentId, operation.getId())).thenReturn(null);

        io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation operationDto =
                new io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation();
        operationDto.setInitiatedBy(SYSTEM_INITIATOR);
        Mockito.when(mockOperationDAO.getOperation(operation.getId())).thenReturn(operationDto);

        MonitoringOperation monitoringOperation = new MonitoringOperation();
        monitoringOperation.setTaskName(operationCode);
        monitoringOperation.setResponsePersistence(false);
        OperationMonitoringTaskConfig monitoringTaskConfig = new OperationMonitoringTaskConfig();
        monitoringTaskConfig.setMonitoringOperation(Collections.singletonList(monitoringOperation));

        DeviceManagementProviderService mockProvider = Mockito.mock(DeviceManagementProviderService.class);
        Mockito.when(mockProvider.getDeviceMonitoringConfig(DEVICE_TYPE)).thenReturn(monitoringTaskConfig);
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(mockProvider);

        operationManager.updateOperation(enrolmentId, operation, deviceId);

        Mockito.verify(mockOperationDAO, Mockito.never())
                .addOperationResponse(Mockito.anyInt(), Mockito.any(Operation.class), Mockito.anyString());
    }

    @Test(description = "A transient DAO failure on the first attempt must retry and succeed, committing only once")
    public void retryThenSucceedCommitsOnce() throws Exception {
        int enrolmentId = 1002;
        DeviceIdentifier deviceId = new DeviceIdentifier("CHAR-DEVICE-2", DEVICE_TYPE);

        CommandOperation operation = new CommandOperation();
        operation.setId(502);
        operation.setCode("RETRY-TEST");
        operation.setStatus(Operation.Status.COMPLETED);
        // No operation response set: the method returns after the status-update step,
        // so this test isolates the retry/commit behaviour of that step alone.

        Mockito.when(mockOperationDAO.updateOperationStatus(Mockito.eq(enrolmentId), Mockito.eq(operation.getId()),
                        Mockito.eq(io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation.Status.COMPLETED)))
                .thenThrow(new OperationManagementDAOException("simulated transient failure"))
                .thenReturn(true);
        Mockito.when(mockOperationDAO.getDeviceOperationDetails(enrolmentId, operation.getId())).thenReturn(null);

        operationManager.updateOperation(enrolmentId, operation, deviceId);

        Mockito.verify(mockOperationDAO, Mockito.times(2)).updateOperationStatus(Mockito.eq(enrolmentId),
                Mockito.eq(operation.getId()),
                Mockito.eq(io.entgra.device.mgt.core.device.mgt.core.dto.operation.mgt.Operation.Status.COMPLETED));
        PowerMockito.verifyStatic(OperationManagementDAOFactory.class, Mockito.times(1));
        OperationManagementDAOFactory.commitTransaction();
    }
}
