/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
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
package org.wso2.carbon.device.application.mgt.core.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.testng.annotations.Test;
import org.wso2.carbon.device.application.mgt.common.*;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.RequestValidatingException;
import org.wso2.carbon.device.application.mgt.common.response.Application;
import org.wso2.carbon.device.application.mgt.common.response.ApplicationRelease;
import org.wso2.carbon.device.application.mgt.common.response.Category;
import org.wso2.carbon.device.application.mgt.common.response.Tag;
import org.wso2.carbon.device.application.mgt.common.services.ApplicationManager;
import org.wso2.carbon.device.application.mgt.common.wrapper.EntAppReleaseWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationUpdateWrapper;
import org.wso2.carbon.device.application.mgt.common.wrapper.ApplicationWrapper;
import org.wso2.carbon.device.application.mgt.core.BaseTestCase;
import org.wso2.carbon.device.application.mgt.core.dao.ApplicationDAO;
import org.wso2.carbon.device.application.mgt.core.dao.common.ApplicationManagementDAOFactory;
import org.wso2.carbon.device.application.mgt.core.dto.ApplicationsDTO;
import org.wso2.carbon.device.application.mgt.core.impl.ApplicationManagerImpl;
import org.wso2.carbon.device.application.mgt.core.util.ConnectionManagerUtil;
import org.wso2.carbon.device.mgt.common.exceptions.DeviceManagementException;
import org.wso2.carbon.device.mgt.core.dto.DeviceType;
import org.wso2.carbon.device.mgt.core.dto.DeviceTypeVersion;
import org.wso2.carbon.device.mgt.core.service.DeviceManagementProviderServiceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationManagementTest extends BaseTestCase {

    private static final Log log = LogFactory.getLog(ApplicationManagementTest.class);

    @Test
    public void testAddApplication() throws Exception {

        ApplicationDAO applicationDAO = ApplicationManagementDAOFactory.getApplicationDAO();
        ConnectionManagerUtil.beginDBTransaction();
        applicationDAO.createApplication(ApplicationsDTO.getApp1(), -1234);
        ConnectionManagerUtil.commitDBTransaction();
        ConnectionManagerUtil.closeDBConnection();
    }

    @Test(dependsOnMethods = ("addApplicationCategories"))
    public void createApplication() throws Exception {

        log.debug("Creating the first application ....!");

        ApplicationWrapper applicationWrapper = new ApplicationWrapper();

        List<String> categories = new ArrayList<>();
        categories.add("Test Category");
        applicationWrapper.setCategories(categories);

        applicationWrapper.setDescription("Test Description");
        applicationWrapper.setDeviceType("android");
        applicationWrapper.setName("Test Application");
        applicationWrapper.setSubMethod("Test Sub type");

        List<String> tags = new ArrayList<>();
        tags.add("abc");
        tags.add("pqr");
        tags.add("xyz");
        applicationWrapper.setTags(tags);
        applicationWrapper.setPaymentCurrency("USD");

        List<EntAppReleaseWrapper> entAppReleaseWrappers = new ArrayList<>();
        EntAppReleaseWrapper releaseWrapper = new EntAppReleaseWrapper();
        releaseWrapper.setDescription("First release");
        releaseWrapper.setIsSharedWithAllTenants(false);
        releaseWrapper.setMetaData("Just meta data");
        releaseWrapper.setReleaseType("free");
        releaseWrapper.setPrice(5.7);
        releaseWrapper.setSupportedOsVersions("4.0-7.0");
        entAppReleaseWrappers.add(releaseWrapper);

        applicationWrapper.setEntAppReleaseWrappers(entAppReleaseWrappers);

        ApplicationArtifact applicationArtifact = new ApplicationArtifact();
        applicationArtifact.setBannerName("My First Banner");
        File banner = new File("src/test/resources/samples/app1/banner1.jpg");
        InputStream bannerStream = new FileInputStream(banner);
        applicationArtifact.setBannerStream(bannerStream);
        applicationArtifact.setIconName("My First Icon");
        applicationArtifact.setIconStream(new FileInputStream(new File("src/test/resources/samples/app1/icon.png")));
        applicationArtifact.setInstallerName("Test Android App");
        applicationArtifact.setInstallerStream(new FileInputStream(new File("src/test/resources/samples/app1/sample.apk")));

        Map<String, InputStream> screenshots = new HashMap<>();
        screenshots.put("shot1", new FileInputStream(new File("src/test/resources/samples/app1/shot1.png")));
        screenshots.put("shot2", new FileInputStream(new File("src/test/resources/samples/app1/shot2.png")));
        screenshots.put("shot3", new FileInputStream(new File("src/test/resources/samples/app1/shot3.png")));

        applicationArtifact.setScreenshots(screenshots);

        ApplicationManager manager = new ApplicationManagerImpl();
        manager.createEntApp(applicationWrapper, applicationArtifact);
    }

    @Test
    public void updateApplication(int applicationId, ApplicationUpdateWrapper applicationUpdateWrapper) throws ApplicationManagementException {

    }

    @Test
    public void deleteApplication(int applicationId) throws ApplicationManagementException {

    }

    @Test
    public void retireApplication(int applicationId) throws ApplicationManagementException {

    }

    @Test
    public void deleteApplicationRelease(String releaseUuid) throws ApplicationManagementException {

    }

    @Test
    public ApplicationList getApplications(Filter filter) throws ApplicationManagementException {
        return null;
    }

    @Test
    public Application getApplicationById(int id, String state) throws ApplicationManagementException {
        return null;
    }

    @Test
    public ApplicationRelease getApplicationReleaseByUUID(String uuid) throws ApplicationManagementException {
        return null;
    }

    @Test
    public ApplicationDTO getApplicationByUuid(String uuid, String state) throws ApplicationManagementException {
        return null;
    }

    @Test
    public ApplicationDTO getApplicationByRelease(String appReleaseUUID) throws ApplicationManagementException {
        return null;
    }

    @Test
    public List<LifecycleState> getLifecycleStateChangeFlow(String releaseUuid) throws ApplicationManagementException {
        return null;
    }

    @Test
    public void changeLifecycleState(String releaseUuid, String stateName) throws ApplicationManagementException {

    }

    @Test
    public void updateApplicationImageArtifact(String uuid, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {

    }

    @Test
    public void updateApplicationArtifact(String deviceType, String appType, String uuid, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {

    }

    @Test
    public ApplicationRelease createRelease(int applicationId, EntAppReleaseWrapper entAppReleaseWrapper, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {
        return null;
    }

    @Test
    public boolean updateRelease(String deviceType, String applicationType, String releaseUuid, EntAppReleaseWrapper entAppReleaseWrapper, ApplicationArtifact applicationArtifact) throws ApplicationManagementException {
        return false;
    }

    @Test
    public void validateAppCreatingRequest(ApplicationWrapper applicationWrapper) throws RequestValidatingException {

    }

    @Test
    public void validateReleaseCreatingRequest(EntAppReleaseWrapper entAppReleaseWrapper, String applicationType) throws RequestValidatingException {

    }

    @Test
    public void validateImageArtifacts(Attachment iconFile, Attachment bannerFile, List<Attachment> attachmentList) throws RequestValidatingException {

    }

    @Test
    public void validateBinaryArtifact(Attachment binaryFile, String applicationType) throws RequestValidatingException {

    }

    @Test(dependsOnMethods = ("addDeviceVersions"))
    public void addApplicationCategories() throws ApplicationManagementException {
        List<String> categories = new ArrayList<>();
        categories.add("Test Category");
        categories.add("Test Category2");
        ApplicationManager manager = new ApplicationManagerImpl();
        manager.addApplicationCategories(categories);

    }

    @Test
    public void addDeviceVersions() throws ApplicationManagementException {
        List<DeviceTypeVersion> deviceTypeVersions = new ArrayList<>();
        List<String> supportingVersions = new ArrayList<>();

        //add supporting versions
        supportingVersions.add("4.0");
        supportingVersions.add("5.0");
        supportingVersions.add("6.0");
        supportingVersions.add("7.0");
        supportingVersions.add("8.0");

        DeviceManagementProviderServiceImpl deviceManagementProviderService = new DeviceManagementProviderServiceImpl();
        try {
            List<DeviceType> deviceTypes = deviceManagementProviderService.getDeviceTypes();

            for (DeviceType deviceType: deviceTypes){
                for (String version : supportingVersions){
                    DeviceTypeVersion deviceTypeVersion = new DeviceTypeVersion();
                    deviceTypeVersion.setDeviceTypeId(deviceType.getId());
                    deviceTypeVersion.setVersionName(version);
                    deviceTypeVersions.add(deviceTypeVersion);
                }
            }

            for (DeviceTypeVersion deviceTypeVersion : deviceTypeVersions){
                deviceManagementProviderService.addDeviceTypeVersion(deviceTypeVersion);
            }
        } catch (DeviceManagementException e) {
            String msg = "Error Occured while adding device type versions";
            log.error(msg);
            throw new ApplicationManagementException(msg);
        }
    }

    @Test
    public List<Tag> getRegisteredTags() throws ApplicationManagementException {
        return null;
    }

    @Test
    public List<Category> getRegisteredCategories() throws ApplicationManagementException {
        return null;
    }

    @Test
    public void deleteTagMapping(int appId, String tagName) throws ApplicationManagementException {

    }
}
