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

package io.entgra.device.mgt.core.notification.mgt.core.task;

import io.entgra.device.mgt.core.notification.mgt.core.config.NotificationConfigurationManager;
import io.entgra.device.mgt.core.notification.mgt.core.exception.NotificationArchivalTaskManagerException;
import io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementDataHolder;
import io.entgra.device.mgt.core.notification.mgt.core.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.internal.TasksDSComponent;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.ntask.core.service.impl.TaskServiceImpl;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.lang.reflect.Field;

public class NotificationArchivalTaskManagerImplTest {

    private static final Log log = LogFactory.getLog(NotificationArchivalTaskManagerImplTest.class);
    private TaskService mockTaskService;
    private NotificationArchivalTaskManager notificationArchTaskManager;

    @BeforeClass
    public void init() throws Exception {
        NotificationConfigurationManager.getInstance().initConfig();
        log.info("Initializing Device Task Manager Service Test Suite");
        this.mockTaskService = new TestTaskServiceImpl();
        NotificationManagementDataHolder.getInstance().setTaskService(this.mockTaskService);
        Field mockTaskServiceField = TasksDSComponent.class.getDeclaredField("taskService");
        mockTaskServiceField.setAccessible(true);
        mockTaskServiceField.set(null, Mockito.mock(TaskServiceImpl.class, Mockito.RETURNS_MOCKS));
        this.notificationArchTaskManager = new NotificationArchivalTaskManagerImpl();
    }

    @Test(groups = "Notification Archive Task Schedule Service Test Group")
    public void testStartTask() {
        try {
            log.debug("Attempting to start task from testStartTask");
            this.notificationArchTaskManager.startTask();
            TaskManager taskManager = this.mockTaskService
                    .getTaskManager(Constants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
            Assert.assertEquals(this.mockTaskService.getRegisteredTaskTypes().size(), 1);
            Assert.assertNotNull(taskManager.getTask(Constants.NOTIFICATION_ARCHIVAL_TASK_NAME + "_"
                    + MultitenantConstants.SUPER_TENANT_ID));
            log.debug("Task Successfully started");
        } catch (NotificationArchivalTaskManagerException | TaskException e) {
            Assert.fail("Exception occurred when starting the task", e);
        }
    }

    @Test(groups = "Notification Archive Task Schedule Service Test Group")
    public void testStopTask() {
        log.debug("Attempting to stop task from testStopTask");
        try {
            this.notificationArchTaskManager.stopTask();
            TaskManager taskManager = this.mockTaskService
                    .getTaskManager(Constants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
            Assert.assertEquals(taskManager.getAllTasks().size(), 1);
        } catch (NotificationArchivalTaskManagerException | TaskException e) {
            Assert.fail("Exception occurred when stopping the task", e);
        }
    }

    @Test(groups = "Notification Archive Task Schedule Service Test Group",
            expectedExceptions = {NotificationArchivalTaskManagerException.class })
    public void testStartTaskWhenUnableToRetrieveTaskManager()
            throws NotificationArchivalTaskManagerException, TaskException {
        TaskService mockTaskService = Mockito.mock(TestTaskServiceImpl.class);
        Mockito.doThrow(new TaskException("Unable to get TaskManager",
                        TaskException.Code.UNKNOWN)).when(mockTaskService)
                .getTaskManager(Constants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
        NotificationManagementDataHolder.getInstance().setTaskService(mockTaskService);
        this.notificationArchTaskManager.startTask();
    }
    
    @Test(groups = "Notification Archive Task Schedule Service Test Group",
            expectedExceptions = {NotificationArchivalTaskManagerException.class })
    public void testStartTaskWhenFailedToRegisterTaskType()
            throws NotificationArchivalTaskManagerException, TaskException {
        TaskService mockTaskService = Mockito.mock(TestTaskServiceImpl.class);
        Mockito.doThrow(new TaskException("Unable to register task type",
                        TaskException.Code.UNKNOWN)).when(mockTaskService)
                .registerTaskType(Constants.NOTIFICATION_ARCHIVAL_TASK_TYPE);
        NotificationManagementDataHolder.getInstance().setTaskService(mockTaskService);
        this.notificationArchTaskManager.startTask();
    }

    @Test(groups = "Notification Archive Task Schedule Service Test Group",
            expectedExceptions = {NotificationArchivalTaskManagerException.class })
    public void testStartTaskWhenFailedToRegisterTask()
            throws NotificationArchivalTaskManagerException, TaskException {
        TestTaskServiceImpl mockTaskService = new TestTaskServiceImpl();
        TaskManager taskManager = Mockito.mock(TestTaskManagerImpl.class);
        mockTaskService.setTaskManager(taskManager);
        Mockito.doThrow(new TaskException("Unable to register task",
                        TaskException.Code.UNKNOWN)).when(taskManager)
                .registerTask(Mockito.any(TaskInfo.class));
        NotificationManagementDataHolder.getInstance().setTaskService(mockTaskService);
        this.notificationArchTaskManager.startTask();
    }
}
