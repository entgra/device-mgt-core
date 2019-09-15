/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.device.application.mgt.common.services;

import org.wso2.carbon.device.application.mgt.common.ApplicationInstaller;
import org.wso2.carbon.device.application.mgt.common.dto.ApplicationReleaseDTO;
import org.wso2.carbon.device.application.mgt.common.exception.ApplicationStorageManagementException;
import org.wso2.carbon.device.application.mgt.common.exception.ResourceManagementException;

import java.io.InputStream;
import java.util.List;

/**
 * This manages all the storage related requirements of ApplicationDTO.
 */
public interface ApplicationStorageManager {
    /**
     * To upload image artifacts related with an ApplicationDTO.
     *
     * @param applicationRelease ApplicationReleaseDTO Object
     * @param iconFile        Icon File input stream
     * @param bannerFile      Banner File input stream
     * @throws ResourceManagementException Resource Management Exception.
     */
    ApplicationReleaseDTO uploadImageArtifacts(ApplicationReleaseDTO applicationRelease,
            InputStream iconFile, InputStream bannerFile, List<InputStream> screenshots) throws ResourceManagementException;

    ApplicationInstaller getAppInstallerData(InputStream binaryFile, String deviceType)
            throws ApplicationStorageManagementException;


    /**
     * To upload release artifacts for an Application.
     *
     * @param applicationRelease ApplicationDTO Release Object.
     * @param deviceType Compatible device type of the application.
     * @param binaryFile      Binary File for the release.
     * @throws ResourceManagementException if IO Exception occured while saving the release artifacts in the server.
     */
    void uploadReleaseArtifact(ApplicationReleaseDTO applicationRelease, String deviceType, InputStream binaryFile)
            throws ResourceManagementException;

    /**
     * To upload release artifacts for an ApplicationDTO.
     *
     * @param applicationReleaseDTO applicationRelease ApplicationDTO release of a particular application.
     * @param deletingAppHashValue Hash value of the deleting application release.
     * @throws ApplicationStorageManagementException Resource Management Exception.
     */
    void copyImageArtifactsAndDeleteInstaller(String deletingAppHashValue,
            ApplicationReleaseDTO applicationReleaseDTO) throws ApplicationStorageManagementException;

    /**
     * To delete the artifacts related with particular ApplicationDTO Release.
     *
     * @throws ApplicationStorageManagementException Not Found Exception.
     */
    void deleteAppReleaseArtifact(String appReleaseHashVal, String folderName, String fileName) throws ApplicationStorageManagementException;

    /**
     * To delete all release artifacts related with particular ApplicationDTO Release.
     *
     * @param directoryPaths Hash values of the ApplicationDTO.
     * @throws ApplicationStorageManagementException ApplicationDTO Storage Management Exception
     */
    void deleteAllApplicationReleaseArtifacts(List<String> directoryPaths) throws ApplicationStorageManagementException;

    /***
     * Get the InputStream of the file which is located in filePath
     * @param hashVal Hash Value of the application release.
     * @return {@link InputStream}
     * @throws ApplicationStorageManagementException throws if an error occurs when accessing the file.
     */
    InputStream getFileStream(String hashVal, String folderName, String fileName) throws ApplicationStorageManagementException;
}
