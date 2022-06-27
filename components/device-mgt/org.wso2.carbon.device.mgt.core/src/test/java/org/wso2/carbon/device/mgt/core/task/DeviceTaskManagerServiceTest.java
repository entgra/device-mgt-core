/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.core.TestTaskServiceImpl;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.task.impl.DeviceTaskManagerServiceImpl;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntask.core.service.impl.TaskServiceImpl;

import java.lang.reflect.Field;

public class DeviceTaskManagerServiceTest {
    private static final Log log = LogFactory.getLog(DeviceTaskManagerService.class);
    private static final String TASK_TYPE = "DEVICE_MONITORING";
    private DeviceTaskManagerService deviceTaskManagerService;
    private TaskService taskService;

    @BeforeClass
    public void init() throws Exception {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing Device Task Manager Service Test Suite");
        this.taskService = new TestTaskServiceImpl();
        DeviceManagementDataHolder.getInstance().setTaskService(this.taskService);
        this.deviceTaskManagerService = new DeviceTaskManagerServiceImpl();
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(this.deviceTaskManagerService);
        Field taskServiceField = TasksDSComponent.class.getDeclaredField("taskService");
        taskServiceField.setAccessible(true);
        taskServiceField.set(null, Mockito.mock(TaskServiceImpl.class, Mockito.RETURNS_MOCKS));
    }

    @Test(groups = "Device Task Manager Service Test Group")
    public void testStartTask() {
        try {
            log.debug("Attempting to start task from testStartTask");
            this.deviceTaskManagerService.registerTask(TestDataHolder.TEST_DEVICE_TYPE,
                    TestDataHolder.generateMonitoringTaskConfig(true, 60000, 1));
            TaskManager taskManager = this.taskService.getTaskManager(TASK_TYPE);
            Assert.assertEquals(this.taskService.getRegisteredTaskTypes().size(), 1);
            Assert.assertNotNull(taskManager
                    .getTask(TestDataHolder.TEST_DEVICE_TYPE +
                            String.valueOf(TestDataHolder.SUPER_TENANT_ID)));
            log.debug("Task Successfully started");
        } catch (DeviceMgtTaskException | TaskException e) {
            Assert.fail("Exception occurred when starting the task", e);
        }
    }

    @Test(groups = "Device Task Manager Service Test Group", dependsOnMethods = "testStartTask")
    public void testUpdateTask() {
        try {
            log.debug("Attempting to update task from testStartTask");
            TaskManager taskManager = this.taskService.getTaskManager(TASK_TYPE);
            this.deviceTaskManagerService.updateTask(TestDataHolder.TEST_DEVICE_TYPE,
                    TestDataHolder.generateMonitoringTaskConfig(true, 30000, 1));
            Assert.assertEquals(this.taskService.getRegisteredTaskTypes().size(), 1);
            Assert.assertEquals(taskManager.getAllTasks().size(), 1);
            log.debug("Task Successfully updated");
        } catch (DeviceMgtTaskException | TaskException e) {
            Assert.fail("Exception occurred when updating the task", e);
        }
    }

    @Test(groups = "Device Task Manager Service Test Group", dependsOnMethods = "testUpdateTask")
    public void testStopTask() {
        log.debug("Attempting to stop task from testStopTask");
        try {
            this.deviceTaskManagerService.stopTaskIfScheduled(TestDataHolder.TEST_DEVICE_TYPE + TestDataHolder.SUPER_TENANT_ID);
            TaskManager taskManager = this.taskService.getTaskManager(TASK_TYPE);
            Assert.assertEquals(taskManager.getAllTasks().size(), 0);
        } catch (TaskException e) {
            Assert.fail("Exception occurred when stopping the task", e);
        }
    }

    @Test(groups = "Device Task Manager Service Test Group", dependsOnMethods = "testStopTask")
    public void testUpdateUnscheduledTask() throws DeviceMgtTaskException {
        log.debug("Attempting to update unscheduled task");
        this.deviceTaskManagerService.updateTask(TestDataHolder.TEST_DEVICE_TYPE,
                TestDataHolder.generateMonitoringTaskConfig(true, 60000, 1));
    }

    @Test(groups = "Device Task Manager Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask", expectedExceptions = {
            DeviceMgtTaskException.class })
    public void testStartTaskWhenUnableToRetrieveTaskManager()
            throws DeviceMgtTaskException, TaskException {
        TaskService taskService = Mockito.mock(TestTaskServiceImpl.class);
        Mockito.doThrow(new TaskException("Unable to get TaskManager", TaskException.Code.UNKNOWN)).when(taskService)
                .getTaskManager(TASK_TYPE);
        DeviceManagementDataHolder.getInstance().setTaskService(taskService);
        this.deviceTaskManagerService.startTasks(TestDataHolder.TEST_DEVICE_TYPE,
                TestDataHolder.generateMonitoringTaskConfig(true, 60000, 2));
    }

    @Test(groups = "Device Task Manager Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask", expectedExceptions = {
            DeviceMgtTaskException.class })
    public void testUpdateTaskWhenUnableToRetrieveTaskManager()
            throws DeviceMgtTaskException, TaskException {
        TaskService taskService = Mockito.mock(TestTaskServiceImpl.class);
        Mockito.doThrow(new TaskException("Unable to get TaskManager", TaskException.Code.UNKNOWN)).when(taskService)
                .getTaskManager(TASK_TYPE);
        DeviceManagementDataHolder.getInstance().setTaskService(taskService);
        this.deviceTaskManagerService.updateTask(TestDataHolder.TEST_DEVICE_TYPE,
                TestDataHolder.generateMonitoringTaskConfig(true, 60000, 2));
    }

    @Test(groups = "Device Task Manager Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {DeviceMgtTaskException.class })
    public void testStartTaskWhenFailedToRegisterTaskType()
            throws DeviceMgtTaskException, TaskException {
        TaskService taskService = Mockito.mock(TestTaskServiceImpl.class);
        Mockito.doThrow(new TaskException("Unable to register task type", TaskException.Code.UNKNOWN)).when(taskService)
                .registerTaskType(TASK_TYPE);
        DeviceManagementDataHolder.getInstance().setTaskService(taskService);
        this.deviceTaskManagerService.startTasks(TestDataHolder.TEST_DEVICE_TYPE,
                TestDataHolder.generateMonitoringTaskConfig(true, 60000, 2));
    }

    @Test(groups = "Device Task Manager Service Test Group", dependsOnMethods = "testUpdateUnscheduledTask",
            expectedExceptions = {DeviceMgtTaskException.class })
    public void testStartTaskWhenFailedToRegisterTask()
            throws DeviceMgtTaskException, TaskException {
        TestTaskServiceImpl taskService = new TestTaskServiceImpl();
        TaskManager taskManager = Mockito.mock(TestTaskManagerImpl.class);
        taskService.setTaskManager(taskManager);
        Mockito.doThrow(new TaskException("Unable to register task", TaskException.Code.UNKNOWN)).when(taskManager)
                .registerTask(Mockito.any(TaskInfo.class));
        DeviceManagementDataHolder.getInstance().setTaskService(taskService);
        this.deviceTaskManagerService.startTasks(TestDataHolder.TEST_DEVICE_TYPE,
                TestDataHolder.generateMonitoringTaskConfig(true, 60000, 2));
    }

}
