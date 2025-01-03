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
package io.entgra.device.mgt.core.device.mgt.core.search;

import io.entgra.device.mgt.core.device.mgt.common.Device;
import io.entgra.device.mgt.core.device.mgt.common.DeviceIdentifier;
import io.entgra.device.mgt.core.device.mgt.common.search.Condition;
import io.entgra.device.mgt.core.device.mgt.common.search.SearchContext;
import io.entgra.device.mgt.core.device.mgt.core.TestDeviceManagementService;
import io.entgra.device.mgt.core.device.mgt.core.common.BaseDeviceManagementTest;
import io.entgra.device.mgt.core.device.mgt.core.common.TestDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementServiceComponent;
import io.entgra.device.mgt.core.device.mgt.core.search.mgt.InvalidOperatorException;
import io.entgra.device.mgt.core.device.mgt.core.search.mgt.SearchMgtException;
import io.entgra.device.mgt.core.device.mgt.core.search.mgt.impl.ProcessorImpl;
import io.entgra.device.mgt.core.device.mgt.core.search.util.Utils;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderService;
import io.entgra.device.mgt.core.device.mgt.core.service.DeviceManagementProviderServiceImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds unit test cases for io.entgra.device.mgt.core.device.mgt.core.search.mgt.impl.ProcessorImpl
 */
public class ProcessorImplTest extends BaseDeviceManagementTest {

    private static List<DeviceIdentifier> deviceIdentifiers = new ArrayList<>();
    private static final String DEVICE_ID_PREFIX = "SEARCH-DEVICE-ID-";
    private static final String DEVICE_TYPE = "SEARCH_TYPE";

    @BeforeClass
    public void init() throws Exception {
        for (int i = 0; i < 5; i++) {
            deviceIdentifiers.add(new DeviceIdentifier(DEVICE_ID_PREFIX + i, DEVICE_TYPE));
        }
        DeviceManagementProviderService deviceMgtService = new DeviceManagementProviderServiceImpl();
        DeviceManagementServiceComponent.notifyStartupListeners();
        DeviceManagementDataHolder.getInstance().setDeviceManagementProvider(deviceMgtService);
        deviceMgtService.registerDeviceType(new TestDeviceManagementService(DEVICE_TYPE,
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        List<Device> devices = TestDataHolder.generateDummyDeviceData(deviceIdentifiers);
        for (Device device : devices) {
            device.setDeviceInfo(Utils.getDeviceInfo());
            deviceMgtService.enrollDevice(device);
        }
        List<Device> returnedDevices = deviceMgtService.getAllDevices(DEVICE_TYPE, true);
        for (Device device : returnedDevices) {
            if (!device.getDeviceIdentifier().startsWith(DEVICE_ID_PREFIX)) {
                throw new Exception("Incorrect device with ID - " + device.getDeviceIdentifier() + " returned!");
            }
        }
    }

    @Test (description = "Search for device with and condition")
    public void testSearchDevicesWIthAndCondition() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition condition = new Condition();
        condition.setKey("IMEI");
        condition.setOperator("=");
        condition.setValue("e6f236ac82537a8e");
        condition.setState(Condition.State.AND);
        conditions.add(condition);

        context.setConditions(conditions);
        ProcessorImpl processor = new ProcessorImpl();
        List<Device> devices = processor.execute(context);
        Assert.assertEquals(devices.size(), 5, "There should be exactly 5 devices with matching search criteria");
    }

    @Test (description = "Search for device with or condition")
    public void testSearchDevicesWIthORCondition() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition condition = new Condition();
        condition.setKey("IMSI");
        condition.setOperator("=");
        condition.setValue("432659632123654845");
        condition.setState(Condition.State.OR);
        conditions.add(condition);

        context.setConditions(conditions);
        ProcessorImpl processor = new ProcessorImpl();
        List<Device> devices = processor.execute(context);
        Assert.assertEquals(devices.size(), 5, "There should be exactly 5 devices with matching search criteria");
    }

    @Test (description = "Search for device with wrong condition")
    public void testSearchDevicesWIthWrongCondition() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition condition = new Condition();
        condition.setKey("IMSI");
        condition.setOperator("=");
        condition.setValue("43265963212378466");
        condition.setState(Condition.State.OR);
        conditions.add(condition);

        context.setConditions(conditions);
        ProcessorImpl processor = new ProcessorImpl();
        List<Device> devices = processor.execute(context);
        Assert.assertEquals(0, devices.size(), "There should be no devices with matching search criteria");
    }

    @Test(description = "Test for invalid state")
    public void testInvalidState() throws SearchMgtException {
        SearchContext context = new SearchContext();
        List<Condition> conditions = new ArrayList<>();

        Condition cond = new Condition();
        cond.setKey("batteryLevel");
        cond.setOperator("=");
        cond.setValue("40");
        cond.setState(Condition.State.AND);
        conditions.add(cond);

        Condition cond2 = new Condition();
        cond2.setKey("LOCATION");
        cond2.setOperator("=");
        cond2.setValue("Karandeniya");
        cond2.setState(Condition.State.AND);
        conditions.add(cond2);

        Condition cond3 = new Condition();
        cond3.setKey("batteryLevel");
        cond3.setOperator("=");
        cond3.setValue("23.0");
        cond3.setState(Condition.State.AND);
        conditions.add(cond3);

        context.setConditions(conditions);
        ProcessorImpl processor = new ProcessorImpl();
        try {
            processor.execute(context);
        } catch (SearchMgtException e) {
            if (!(e.getCause() instanceof InvalidOperatorException)) {
                throw e;
            }
        }
    }
}
