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

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.InvalidDeviceException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Activity;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.Operation;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManager;
import io.entgra.device.mgt.core.device.mgt.common.spi.DeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.core.TestDeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.core.TestTaskServiceImpl;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.common.TestDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.CommandOperation;
import io.entgra.device.mgt.core.device.mgt.core.operation.mgt.OperationManagerImpl;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.task.DeviceTaskManagerService;
import io.entgra.device.mgt.core.device.mgt.core.task.impl.DeviceTaskManagerServiceImpl;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntask.core.service.impl.TaskServiceImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static io.entgra.device.mgt.core.device.mgt.core.operation.OperationManagementTests.getOperation;

/**
 * This class tests the tasks based operations of {@link OperationManager}
 */
public class ScheduledTaskOperationTests extends BaseDeviceManagementTest {
    private static final String DEVICE_TYPE = "OP_SCHEDULE_TEST_TYPE";
    private static final String DEVICE_ID_PREFIX = "OP-SCHEDULED_TEST-DEVICE-ID-";
    private static final String COMMAND_OPERATON_CODE = "COMMAND-TEST";
    private static final int NO_OF_DEVICES = 5;
    private static final String DS_TASK_COMPONENT_FIELD = "taskService";
    private static final String CDM_CONFIG_LOCATION = "src" + File.separator + "test" + File.separator + "resources" +
            File.separator + "config" + File.separator + "operation" + File.separator + "cdm-config.xml";

    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private OperationManager operationMgtService;

    @BeforeClass
    public void init() throws Exception {
        for (int i = 0; i < NO_OF_DEVICES; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        DeviceManagementProviderService deviceMgtService = DeviceManagementDataHolder.getInstance().getDeviceManagementProvider();
        initTaskService();
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, COMMAND_OPERATON_CODE));
        for (Device device : devices) {
            deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE);
        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
        DeviceConfigurationManager.getInstance().initConfig(CDM_CONFIG_LOCATION);
        DeviceManagementService deviceManagementService
                = new TestDeviceManagementService(DEVICE_TYPE, MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        this.operationMgtService = PowerMockito.spy(new OperationManagerImpl(DEVICE_TYPE, deviceManagementService));
        PowerMockito.when(this.operationMgtService, "getNotificationStrategy")
                .thenReturn(new TestNotificationStrategy());
    }


    private void initTaskService() throws NoSuchFieldException, IllegalAccessException {
        TaskService taskService = new TestTaskServiceImpl();
        DeviceManagementDataHolder.getInstance().setTaskService(taskService);
        DeviceTaskManagerService deviceTaskManager = new DeviceTaskManagerServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(deviceTaskManager);
        Field taskServiceField = TasksDSComponent.class.getDeclaredField(DS_TASK_COMPONENT_FIELD);
        taskServiceField.setAccessible(true);
        taskServiceField.set(null, Mockito.mock(TaskServiceImpl.class, Mockito.RETURNS_MOCKS));

    }

    @Test
    public void addCommandOperation() throws DeviceManagementException, OperationManagementException,
            InvalidDeviceException, NoSuchFieldException {
        Activity activity = this.operationMgtService.addOperation(getOperation(new CommandOperation(), Operation.Type.COMMAND, COMMAND_OPERATON_CODE),
                this.deviceIds);
        Assert.assertEquals(activity.getActivityStatus(), null);
        Assert.assertEquals(activity.getType(), Activity.Type.COMMAND);
    }



}
