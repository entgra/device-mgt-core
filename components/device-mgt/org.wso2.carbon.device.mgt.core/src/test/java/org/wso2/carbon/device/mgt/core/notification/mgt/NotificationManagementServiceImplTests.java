/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.device.mgt.core.notification.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.device.mgt.common.Device;
import org.wso2.carbon.device.mgt.common.DeviceIdentifier;
import org.wso2.carbon.device.mgt.common.exceptions.EntityDoesNotExistException;
import org.wso2.carbon.device.mgt.common.PaginationRequest;
import org.wso2.carbon.device.mgt.common.PaginationResult;
import org.wso2.carbon.device.mgt.common.notification.mgt.Notification;
import org.wso2.carbon.device.mgt.common.notification.mgt.NotificationManagementException;
import org.wso2.carbon.device.mgt.core.TestDeviceManagementService;
import org.wso2.carbon.device.mgt.core.authorization.DeviceAccessAuthorizationServiceImpl;
import org.wso2.carbon.device.mgt.core.common.TestDataHolder;
import org.wso2.carbon.device.mgt.core.config.DeviceConfigurationManager;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementServiceComponent;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderService;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.wso2.carbon.device.mgt.core.service.GroupManagementProviderServiceImpl;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.internal.RegistryDataHolder;
import org.wso2.carbon.registry.core.jdbc.realm.InMemoryRealmService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the test class for {@link NotificationManagementServiceImpl}
 */
public class NotificationManagementServiceImplTests {

    private static final Log log = LogFactory.getLog(NotificationManagementServiceImplTests.class);
    private static final String DEVICE_TYPE = "NOTIFICATION_TEST_DEVICE";
    private static final String DEVICE_ID_PREFIX = "NOTIFICATION-TEST-DEVICE-ID-";
    private static final int NO_OF_DEVICES = 10;
    private static final int NO_OF_NOTIFICATIONS = 10;
    private List<DeviceIdentifier> deviceIds = new ArrayList<>();
    private NotificationManagementServiceImpl notificationManagementService;
    private static final String TEST_NOTIFICATION_DESCRIPTION = "test notification";
    private static final int NOTIFICATION_OPERATION_ID = 1;

    @BeforeClass
    public void init() throws Exception {
        DeviceConfigurationManager.getInstance().initConfig();
        log.info("Initializing");
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            deviceIds.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        List<Device> devices = TestDataHolder.generateDummyDeviceData(this.deviceIds);
        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        DeviceManagementDataHolder.getInstance().setRegistryService(getRegistryService());
        DeviceManagementDataHolder.getInstance().setDeviceAccessAuthorizationService(new DeviceAccessAuthorizationServiceImpl());
        DeviceManagementDataHolder.getInstance().setGroupManagementProviderService(new GroupManagementProviderServiceImpl());
        DeviceManagementDataHolder.getInstance().setDeviceTaskManagerService(null);
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        for (Device device : devices) {
            Assert.assertTrue(deviceMgtService.enrollDevice(device), "Device with Identifier - " +
                    device.getDeviceIdentifier() + " is not enrolled.");
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE);

        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
        notificationManagementService = new NotificationManagementServiceImpl();
    }

    private RegistryService getRegistryService() throws RegistryException {
        RealmService realmService = new InMemoryRealmService();
        RegistryDataHolder.getInstance().setRealmService(realmService);
        DeviceManagementDataHolder.getInstance().setRealmService(realmService);
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("carbon-home/repository/conf/registry.xml");
        RegistryContext context = RegistryContext.getBaseInstance(is, realmService);
        context.setSetup(true);
        return context.getEmbeddedRegistryService();
    }

    @Test(description = "Add notifications using addNotification method and check whether it returns true.")
    public void addNotification() throws NotificationManagementException {
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            DeviceIdentifier testDeviceIdentifier = new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE);
            Notification notification = TestDataHolder.getNotification(i, Notification.Status.NEW.toString(),
                    testDeviceIdentifier.toString(), TEST_NOTIFICATION_DESCRIPTION, DEVICE_ID_PREFIX + i,
                    NOTIFICATION_OPERATION_ID, DEVICE_TYPE);
            Assert.assertTrue(notificationManagementService.addNotification(testDeviceIdentifier, notification),
                    "Adding notification failed for [" + notification.toString() + "]");
        }
    }

    @Test(expectedExceptions = EntityDoesNotExistException.class, description = "AddNotification method is checked" +
            " whether it returns EntityDoesNotExistException when the device not registered is added notification")
    public void addNotificationExceptions() throws NotificationManagementException {
        DeviceIdentifier testDeviceIdentifier = new DeviceIdentifier(DEVICE_ID_PREFIX + 123, DEVICE_TYPE);
        Notification notification = TestDataHolder.getNotification(1, Notification.Status.NEW.toString(),
                testDeviceIdentifier.toString(), TEST_NOTIFICATION_DESCRIPTION, DEVICE_ID_PREFIX + 123,
                NOTIFICATION_OPERATION_ID, DEVICE_TYPE);
        notificationManagementService.addNotification(new DeviceIdentifier(DEVICE_ID_PREFIX + 123,
                DEVICE_TYPE), notification);
    }

    @Test(expectedExceptions = NotificationManagementException.class, description = "This tests the method getDevice which" +
            " is called internally in addNotification for DeviceManagementException exception passing null device Id.")
    public void getDevice() throws NotificationManagementException {
        DeviceIdentifier testDeviceIdentifier = new DeviceIdentifier(DEVICE_ID_PREFIX + 123, DEVICE_TYPE);
        Notification notification = TestDataHolder.getNotification(1, Notification.Status.NEW.toString(),
                testDeviceIdentifier.toString(), TEST_NOTIFICATION_DESCRIPTION, DEVICE_ID_PREFIX + 123,
                NOTIFICATION_OPERATION_ID, DEVICE_TYPE);
        notificationManagementService.addNotification(null, notification);
    }

    @Test(dependsOnMethods = "addNotification", description = "This tests the updateNotification Method" +
            " and check whether it returns true ( got updated )")
    public void updateNotification() throws NotificationManagementException {
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            DeviceIdentifier testDeviceIdentifier = new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE);
            Notification notification = TestDataHolder.getNotification(i, Notification.Status.CHECKED.toString(),
                    testDeviceIdentifier.toString(), TEST_NOTIFICATION_DESCRIPTION, DEVICE_ID_PREFIX + i,
                    NOTIFICATION_OPERATION_ID, DEVICE_TYPE);
            Assert.assertTrue(notificationManagementService.updateNotification(notification), "Notification " +
                    "update failed for [" + notification.toString() + "]");
        }
    }

    @Test(dependsOnMethods = "updateNotification", description = "This method update notification status " +
            "and check whether it got updated")
    public void updateNotificationStatus() throws NotificationManagementException {
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            Assert.assertTrue(notificationManagementService.updateNotificationStatus(i, Notification.Status.CHECKED),
                    "Notification update status failed for notification id:- " + i);
        }
    }

    @Test(dependsOnMethods = "addNotification", description = "this tests getAllNotifications" +
            " method by listing down all the notifications.")
    public void getAllNotifications() throws NotificationManagementException {
        List<Notification> returnedNotifications = notificationManagementService.getAllNotifications();
        Assert.assertEquals(returnedNotifications.size(), NO_OF_DEVICES, "No. of notifications added is not " +
                "equal to no. of notifications retrieved.");
    }

    @Test(dependsOnMethods = "updateNotificationStatus", description = "this method retries notification by id" +
            " and checks it")
    public void getNotification() throws NotificationManagementException {
        for (int i = 1; i <= NO_OF_DEVICES; i++) {
            Notification returnedNotification = notificationManagementService.getNotification(i);
            Assert.assertEquals(returnedNotification.getNotificationId(), i, "Returned notification ID is not " +
                    "same as added notification Id.");
            Assert.assertEquals(returnedNotification.getStatus(), Notification.Status.CHECKED, "Returned " +
                    "notification status is not same as added notification status.");
            Assert.assertEquals(returnedNotification.getDescription(), TEST_NOTIFICATION_DESCRIPTION, "Returned" +
                    " notification description is not same as added notification description.");
            Assert.assertEquals(returnedNotification.getOperationId(), NOTIFICATION_OPERATION_ID, "Returned " +
                    "notification operation ID is not same as added notification operation Id.");
        }
    }

    @Test(dependsOnMethods = "updateNotificationStatus", description = "this method gets all notification by status checked")
    public void getNotificationsByStatus() throws NotificationManagementException {
        List<Notification> returnedNotifications = notificationManagementService.getNotificationsByStatus(Notification.
                Status.CHECKED);
        Assert.assertEquals(returnedNotifications.size(), NO_OF_NOTIFICATIONS, "Returned no. of notification is " +
                "not same as added no. of notifications.");
    }

    @Test(dependsOnMethods = "addNotification", description = "this tests for getAllNotification method by passing " +
            "pagination request and validates the no. of total records and filtered records. ")
    public void getAllNotificationsWithPaginationRequest() throws NotificationManagementException {
        PaginationRequest request = new PaginationRequest(1, 2);
        PaginationResult result = notificationManagementService.getAllNotifications(request);
        Assert.assertEquals(result.getRecordsFiltered(), NO_OF_NOTIFICATIONS, "Returned filtered records is " +
                "not same as added filtered records.");
        Assert.assertEquals(result.getRecordsTotal(), NO_OF_NOTIFICATIONS, "Returned no. of records is not " +
                "same as added no. of records.");
    }

    @Test(dependsOnMethods = "updateNotificationStatus", description = "this tests for getAllNotification method by" +
            " passing pagination request & status and validates the no. of total records and filtered records. ")
    public void getAllNotificationsWithPaginationRequestAndStatus() throws NotificationManagementException {
        PaginationRequest request = new PaginationRequest(1, 2);
        PaginationResult result = notificationManagementService.getNotificationsByStatus(Notification.Status.CHECKED,
                request);
        Assert.assertEquals(result.getRecordsFiltered(), NO_OF_NOTIFICATIONS, "Returned filtered records is not " +
                "same as added filtered records.");
        Assert.assertEquals(result.getRecordsTotal(), NO_OF_NOTIFICATIONS, "Returned no. of records is not same" +
                " as added no. of records.");
    }

    @Test(dependsOnMethods = "updateNotificationStatus", description = "this tries to " +
            "update the status of all notifications")
    public void updateStatusOfAllNotifications() throws NotificationManagementException {
        Assert.assertTrue(notificationManagementService.updateAllNotifications(Notification
                .Status.CHECKED, -1234));
    }

}

