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

package io.entgra.device.mgt.core.device.mgt.core.metadata.mgt;

import com.google.gson.Gson;
import io.entgra.device.mgt.core.device.mgt.common.Base64File;
import io.entgra.device.mgt.core.device.mgt.common.FileResponse;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.DeviceManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.NotFoundException;
import io.entgra.device.mgt.core.device.mgt.common.exceptions.TransactionManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.*;
import io.entgra.device.mgt.core.device.mgt.core.common.util.HttpUtil;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.metadata.mgt.MetaDataConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.config.metadata.mgt.whitelabel.WhiteLabelConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.internal.DeviceManagementDataHolder;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataDAO;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOException;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.MetadataManagementDAOFactory;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.dao.util.MetadataConstants;
import io.entgra.device.mgt.core.device.mgt.core.metadata.mgt.util.WhiteLabelStorageUtil;
import io.entgra.device.mgt.core.device.mgt.core.util.DeviceManagerUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

/**
 * This class implements the MetadataManagementService.
 */
public class WhiteLabelManagementServiceImpl implements WhiteLabelManagementService {

    private static final Log log = LogFactory.getLog(WhiteLabelManagementServiceImpl.class);

    private final MetadataDAO metadataDAO;

    public WhiteLabelManagementServiceImpl() throws MetadataManagementException {
        this.metadataDAO = MetadataManagementDAOFactory.getMetadataDAO();
        initializeWhiteLabelThemes();
    }

    /**
     * Initializes white label theme for a tenant by retrieving white label metadata and updating it if necessary.
     * If the white label metadata is found and the DocUrl is missing,it updates the metadata with the default value
     * for DocUrl.
     *
     * @throws MetadataManagementException if an error occurs while managing metadata or transactions.
     */
    private void initializeWhiteLabelThemes() throws MetadataManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        WhiteLabelTheme defaultTheme = getDefaultWhiteLabelTheme();
        Metadata whiteLabelMetadata = getWhiteLabelMetaData(tenantId);
        if (whiteLabelMetadata != null) {
            WhiteLabelTheme whiteLabelTheme = new Gson().fromJson(whiteLabelMetadata.getMetaValue(),
                    WhiteLabelTheme.class);
            if (whiteLabelTheme.getDocUrl() == null) {
                whiteLabelTheme.setDocUrl(defaultTheme.getDocUrl());
                Metadata updatedMetadata = constructWhiteLabelThemeMetadata(whiteLabelTheme);
                try {
                    MetadataManagementDAOFactory.beginTransaction();
                    metadataDAO.updateMetadata(tenantId, updatedMetadata);
                    MetadataManagementDAOFactory.commitTransaction();
                    if (log.isDebugEnabled()) {
                        log.debug("WhiteLabel theme's DocUrl was missing and has been updated to the default value " +
                                "for tenant: " + tenantId);
                    }
                } catch (MetadataManagementDAOException e) {
                    MetadataManagementDAOFactory.rollbackTransaction();
                    String msg = "Error occurred while fetching white label metadata for tenant: " + tenantId;
                    log.error(msg, e);
                    throw new MetadataManagementException(msg, e);
                } catch (TransactionManagementException e) {
                    String msg = "Transaction failed while updating white label theme for tenant: "
                            + tenantId;
                    log.error(msg, e);
                    throw new MetadataManagementException(msg, e);
                } finally {
                    MetadataManagementDAOFactory.closeConnection();
                }
            }
        } else {
            addDefaultWhiteLabelThemeIfNotExist(tenantId);
        }
    }

    @Override
    public FileResponse getWhiteLabelFavicon(String tenantDomain) throws MetadataManagementException, NotFoundException {
        try {
            WhiteLabelTheme whiteLabelTheme = getWhiteLabelTheme(tenantDomain);
            return getImageFileResponse(whiteLabelTheme.getFaviconImage(), WhiteLabelImage.ImageName.FAVICON, tenantDomain);
        } catch (IOException e) {
            String msg = "Error occurred while getting byte content of favicon";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting tenant details of favicon";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }

    @Override
    public FileResponse getWhiteLabelLogo(String tenantDomain) throws MetadataManagementException, NotFoundException {
        try {
            WhiteLabelTheme whiteLabelTheme = getWhiteLabelTheme(tenantDomain);
            return getImageFileResponse(whiteLabelTheme.getLogoImage(), WhiteLabelImage.ImageName.LOGO, tenantDomain);
        } catch (IOException e) {
            String msg = "Error occurred while getting byte content of logo";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting tenant details of logo";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }

    @Override
    public FileResponse getWhiteLabelLogoIcon(String tenantDomain) throws MetadataManagementException, NotFoundException {
        try {
            WhiteLabelTheme whiteLabelTheme = getWhiteLabelTheme(tenantDomain);
            return getImageFileResponse(whiteLabelTheme.getLogoIconImage(), WhiteLabelImage.ImageName.LOGO_ICON, tenantDomain);
        } catch (IOException e) {
            String msg = "Error occurred while getting byte content of logo";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting tenant details of icon";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        }
    }

    /**
     * Useful to get white label image file response for provided {@link WhiteLabelImage.ImageName}
     */
    private FileResponse getImageFileResponse(WhiteLabelImage image, WhiteLabelImage.ImageName imageName, String tenantDomain) throws
            IOException, MetadataManagementException, NotFoundException, DeviceManagementException {
        if (image.getImageLocationType() == WhiteLabelImage.ImageLocationType.URL) {
            return getImageFileResponseFromUrl(image.getImageLocation());
        }
        return WhiteLabelStorageUtil.getWhiteLabelImageStream(image, imageName, tenantDomain);
    }

    /**
     * Useful to get white label image file response from provided url
     */
    private FileResponse getImageFileResponseFromUrl(String url) throws IOException, NotFoundException {
        FileResponse fileResponse = new FileResponse();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet imageGetRequest = new HttpGet(url);
            HttpResponse response = client.execute(imageGetRequest);
            InputStream imageStream = response.getEntity().getContent();
            if (imageStream == null) {
                String msg = "Failed to retrieve the image from url: " + url;
                log.error(msg);
                throw new NotFoundException(msg);
            }
            byte[] fileContent = IOUtils.toByteArray(imageStream);
            fileResponse.setFileContent(fileContent);
            String mimeType = HttpUtil.getContentType(response);
            fileResponse.setMimeType(mimeType);
            return fileResponse;
        }
    }


    @Override
    public void addDefaultWhiteLabelThemeIfNotExist(int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.beginTransaction();
            if (!metadataDAO.isExist(tenantId, MetadataConstants.WHITELABEL_META_KEY)) {
                WhiteLabelTheme whiteLabelTheme = getDefaultWhiteLabelTheme();
                Metadata metadata = constructWhiteLabelThemeMetadata(whiteLabelTheme);
                metadataDAO.addMetadata(tenantId, metadata);
                if (log.isDebugEnabled()) {
                    log.debug("White label metadata entry has inserted successfully");
                }
            }
            MetadataManagementDAOFactory.commitTransaction();
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while inserting default whitelabel metadata entry.";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }

    }

    @Override
    public void resetToDefaultWhiteLabelTheme() throws MetadataManagementException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        WhiteLabelTheme whiteLabelTheme = getDefaultWhiteLabelTheme();
        Metadata metadata = constructWhiteLabelThemeMetadata(whiteLabelTheme);
        DeviceManagementDataHolder.getInstance().getMetadataManagementService().updateMetadata(metadata);
        WhiteLabelStorageUtil.deleteWhiteLabelImageForTenantIfExists(tenantId);
    }

    /**
     * Get default metaDataConfiguration DocUrl from config
     */
    private String getDefaultDocUrl() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        return whiteLabelConfiguration.getDocUrl();
    }

    /**
     * Construct and return default whitelabel detail bean {@link WhiteLabelImage}
     */
    private WhiteLabelTheme getDefaultWhiteLabelTheme() {
        String footerText = getDefaultFooterText();
        String appTitle = getDefaultAppTitle();
        WhiteLabelImage favicon = constructDefaultFaviconImage();
        WhiteLabelImage logo = constructDefaultLogoImage();
        WhiteLabelImage logoIcon = constructDefaultLogoIconImage();
        WhiteLabelTheme defaultTheme = new WhiteLabelTheme();
        String docUrl = getDefaultDocUrl();
        defaultTheme.setFooterText(footerText);
        defaultTheme.setAppTitle(appTitle);
        defaultTheme.setLogoImage(logo);
        defaultTheme.setLogoIconImage(logoIcon);
        defaultTheme.setFaviconImage(favicon);
        defaultTheme.setDocUrl(docUrl);
        return defaultTheme;
    }

    /**
     * Get default whitelabel label page title from config
     */
    private String getDefaultAppTitle() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        return whiteLabelConfiguration.getAppTitle();
    }

    /**
     * Get default whitelabel label footer from config
     */
    private String getDefaultFooterText() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        return whiteLabelConfiguration.getFooterText();
    }

    /**
     * This is useful to construct and get the default favicon whitelabel image
     *
     * @return {@link WhiteLabelImage}
     */
    private WhiteLabelImage constructDefaultFaviconImage() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        WhiteLabelImage favicon = new WhiteLabelImage();
        favicon.setImageLocation(whiteLabelConfiguration.getWhiteLabelImages().getDefaultFaviconName());
        setDefaultWhiteLabelImageCommonProperties(favicon);
        return favicon;
    }

    /**
     * This is useful to construct and get the default logo whitelabel image
     *
     * @return {@link WhiteLabelImage}
     */
    private WhiteLabelImage constructDefaultLogoImage() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        WhiteLabelImage logo = new WhiteLabelImage();
        logo.setImageLocation(whiteLabelConfiguration.getWhiteLabelImages().getDefaultLogoName());
        setDefaultWhiteLabelImageCommonProperties(logo);
        return logo;
    }

    /**
     * This is useful to construct and get the default logo whitelabel image
     *
     * @return {@link WhiteLabelImage}
     */
    private WhiteLabelImage constructDefaultLogoIconImage() {
        MetaDataConfiguration metaDataConfiguration = DeviceConfigurationManager.getInstance().
                getDeviceManagementConfig().getMetaDataConfiguration();
        WhiteLabelConfiguration whiteLabelConfiguration = metaDataConfiguration.getWhiteLabelConfiguration();
        WhiteLabelImage logoIcon = new WhiteLabelImage();
        logoIcon.setImageLocation(whiteLabelConfiguration.getWhiteLabelImages().getDefaultLogoIconName());
        setDefaultWhiteLabelImageCommonProperties(logoIcon);
        return logoIcon;
    }

    /**
     * This is useful to set common properties such as DEFAULT_FILE type for {@link WhiteLabelImage.ImageLocationType}
     * for default white label image bean{@link WhiteLabelImage}
     */
    private void setDefaultWhiteLabelImageCommonProperties(WhiteLabelImage image) {
        image.setImageLocationType(WhiteLabelImage.ImageLocationType.DEFAULT_FILE);
    }

    @Override
    public WhiteLabelTheme updateWhiteLabelTheme(WhiteLabelThemeCreateRequest createWhiteLabelTheme)
            throws MetadataManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Creating Metadata : [" + createWhiteLabelTheme.toString() + "]");
        }
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        File existingFaviconImage = null;
        File existingLogoImage = null;
        File existingLogoIconImage = null;
        try {
            WhiteLabelTheme theme = getWhiteLabelTheme(tenantDomain);
            if (theme.getFaviconImage().getImageLocationType() == WhiteLabelImage.ImageLocationType.CUSTOM_FILE) {
                existingFaviconImage = WhiteLabelStorageUtil.getWhiteLabelImageFile(theme.getFaviconImage(), WhiteLabelImage.ImageName.FAVICON, tenantDomain);
            }
            if (theme.getLogoImage().getImageLocationType() == WhiteLabelImage.ImageLocationType.CUSTOM_FILE) {
                existingLogoImage = WhiteLabelStorageUtil.getWhiteLabelImageFile(theme.getLogoImage(), WhiteLabelImage.ImageName.LOGO, tenantDomain);
            }
            if (theme.getLogoIconImage().getImageLocationType() == WhiteLabelImage.ImageLocationType.CUSTOM_FILE) {
                existingLogoIconImage = WhiteLabelStorageUtil.getWhiteLabelImageFile(theme.getLogoIconImage(), WhiteLabelImage.ImageName.LOGO_ICON, tenantDomain);
            }
            storeWhiteLabelImageIfRequired(createWhiteLabelTheme.getFavicon(), WhiteLabelImage.ImageName.FAVICON, tenantId);
            storeWhiteLabelImageIfRequired(createWhiteLabelTheme.getLogo(), WhiteLabelImage.ImageName.LOGO, tenantId);
            storeWhiteLabelImageIfRequired(createWhiteLabelTheme.getLogoIcon(), WhiteLabelImage.ImageName.LOGO_ICON, tenantId);
            WhiteLabelTheme whiteLabelTheme = constructWhiteLabelTheme(createWhiteLabelTheme);
            Metadata metadataWhiteLabelTheme = constructWhiteLabelThemeMetadata(whiteLabelTheme);
            try {
                MetadataManagementDAOFactory.beginTransaction();
                metadataDAO.updateMetadata(tenantId, metadataWhiteLabelTheme);
                MetadataManagementDAOFactory.commitTransaction();
                if (log.isDebugEnabled()) {
                    log.debug("Metadata entry created successfully. " + createWhiteLabelTheme);
                }
                return whiteLabelTheme;
            } catch (MetadataManagementDAOException e) {
                MetadataManagementDAOFactory.rollbackTransaction();
                restoreWhiteLabelImages(existingFaviconImage, existingLogoImage, existingLogoIconImage, tenantId);
                String msg = "Error occurred while creating the metadata entry. " + createWhiteLabelTheme;
                log.error(msg, e);
                throw new MetadataManagementException(msg, e);
            } catch (TransactionManagementException e) {
                restoreWhiteLabelImages(existingFaviconImage, existingLogoImage, existingLogoIconImage, tenantId);
                String msg = "Error occurred while opening a connection to the data source";
                log.error(msg, e);
                throw new MetadataManagementException("Error occurred while creating metadata record", e);
            } finally {
                MetadataManagementDAOFactory.closeConnection();
            }
        } catch (DeviceManagementException e) {
            String msg = "Error occurred while getting tenant details of white label";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);

        }
    }

    /**
     * This is method is useful to restore provided existing white label images (i.e: favicon/logo).
     * For example if any exception occurred white updating/deleting white label, this method can be used to
     * restore the existing images in any case. Note that the existing images should be first loaded so that
     * those can be passed to this method in order to restore.
     *
     * @param existingFavicon existing favicon image file
     * @param existingLogo    existing logo image file
     */
    private void restoreWhiteLabelImages(File existingFavicon, File existingLogo, File existingLogoIcon, int tenantId)
            throws MetadataManagementException {
        WhiteLabelStorageUtil.deleteWhiteLabelImageForTenantIfExists(tenantId);
        if (existingFavicon != null) {
            WhiteLabelStorageUtil.storeWhiteLabelImage(existingFavicon, WhiteLabelImage.ImageName.FAVICON, tenantId);
        }
        if (existingLogo != null) {
            WhiteLabelStorageUtil.storeWhiteLabelImage(existingLogo, WhiteLabelImage.ImageName.LOGO, tenantId);
        }
        if (existingLogoIcon != null) {
            WhiteLabelStorageUtil.storeWhiteLabelImage(existingLogoIcon, WhiteLabelImage.ImageName.LOGO_ICON, tenantId);
        }
    }

    /**
     * This handles storing provided white label image if required.
     * For example if the provided white label image is of URL type it doesn't need to be stored
     *
     * @param whiteLabelImage image to be stored
     * @param imageName       (i.e: FAVICON)
     */
    private void storeWhiteLabelImageIfRequired(WhiteLabelImageRequestPayload whiteLabelImage,
                                                WhiteLabelImage.ImageName imageName, int tenantId)
            throws MetadataManagementException {
        if (whiteLabelImage.getImageType() == WhiteLabelImageRequestPayload.ImageType.BASE64) {
            Base64File imageBase64 = new Gson().fromJson(whiteLabelImage.getImage(), Base64File.class);
            WhiteLabelStorageUtil.updateWhiteLabelImage(imageBase64, imageName, tenantId);
        }
    }

    /**
     * Generate {@link WhiteLabelTheme} from provided {@link WhiteLabelThemeCreateRequest}
     */
    private WhiteLabelTheme constructWhiteLabelTheme(WhiteLabelThemeCreateRequest whiteLabelThemeCreateRequest) {
        WhiteLabelTheme whiteLabelTheme = new WhiteLabelTheme();
        WhiteLabelImageRequestPayload faviconPayload = whiteLabelThemeCreateRequest.getFavicon();
        WhiteLabelImageRequestPayload logoPayload = whiteLabelThemeCreateRequest.getLogo();
        WhiteLabelImageRequestPayload logoIconPayload = whiteLabelThemeCreateRequest.getLogoIcon();
        WhiteLabelImage faviconImage = constructWhiteLabelImageDTO(faviconPayload);
        WhiteLabelImage logoImage = constructWhiteLabelImageDTO(logoPayload);
        WhiteLabelImage logoIconImage = constructWhiteLabelImageDTO(logoIconPayload);
        whiteLabelTheme.setFaviconImage(faviconImage);
        whiteLabelTheme.setLogoImage(logoImage);
        whiteLabelTheme.setLogoIconImage(logoIconImage);
        whiteLabelTheme.setFooterText(whiteLabelThemeCreateRequest.getFooterText());
        whiteLabelTheme.setAppTitle(whiteLabelThemeCreateRequest.getAppTitle());
        whiteLabelTheme.setDocUrl(whiteLabelThemeCreateRequest.getDocUrl());
        return whiteLabelTheme;
    }

    /**
     * Generate {@link WhiteLabelImage} from provided {@link WhiteLabelImageRequestPayload}
     */
    private WhiteLabelImage constructWhiteLabelImageDTO(WhiteLabelImageRequestPayload image) {
        WhiteLabelImage imageResponse = new WhiteLabelImage();
        WhiteLabelImage.ImageLocationType imageLocationType = image.getImageType().getDTOImageLocationType();
        imageResponse.setImageLocationType(imageLocationType);
        String imageLocation;
        if (image.getImageType() == WhiteLabelImageRequestPayload.ImageType.BASE64) {
            Base64File imageBase64 = image.getImageAsBase64File();
            imageLocation = imageBase64.getName();
        } else {
            imageLocation = image.getImageAsUrl();
        }
        imageResponse.setImageLocation(imageLocation);
        return imageResponse;
    }

    /**
     * Generate {@link Metadata} from provided {@link WhiteLabelImage}
     */
    private Metadata constructWhiteLabelThemeMetadata(WhiteLabelTheme whiteLabelTheme) {
        String whiteLabelThemeJsonString = new Gson().toJson(whiteLabelTheme);
        Metadata metadata = new Metadata();
        metadata.setMetaKey(MetadataConstants.WHITELABEL_META_KEY);
        metadata.setMetaValue(whiteLabelThemeJsonString);
        return metadata;
    }

    /**
     * updates the given WhiteLabelTheme with default value for docUrl
     *
     * @param whiteLabelTheme the WhiteLabelTheme to be updated with defaults if necessary.
     * @param tenantId        the ID of the tenant whose metadata is being updated.
     * @throws MetadataManagementException exception for an error occurs during the update or transaction commit.
     */
    private void updateWhiteLabelThemeWithDefaults(WhiteLabelTheme whiteLabelTheme, int tenantId)
            throws MetadataManagementException {
        WhiteLabelTheme defaultTheme = getDefaultWhiteLabelTheme();
        if (whiteLabelTheme.getDocUrl() == null) {
            whiteLabelTheme.setDocUrl(defaultTheme.getDocUrl());
        }
        Metadata updatedMetadata = constructWhiteLabelThemeMetadata(whiteLabelTheme);
        try {
            MetadataManagementDAOFactory.beginTransaction();
            metadataDAO.updateMetadata(tenantId, updatedMetadata);
            MetadataManagementDAOFactory.commitTransaction();
        } catch (MetadataManagementDAOException e) {
            MetadataManagementDAOFactory.rollbackTransaction();
            String msg = "Error occurred while updating metadata for tenant: " + tenantId;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (TransactionManagementException e) {
            String msg = "Error occurred while committing the transaction for tenant: " + tenantId;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }

    @Override
    public WhiteLabelTheme getWhiteLabelTheme(String tenantDomain) throws MetadataManagementException, DeviceManagementException {
        int tenantId = DeviceManagerUtil.getTenantId(tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug("Retrieving whitelabel theme for tenant: " + tenantId);
        }
        Metadata metadata = getWhiteLabelMetaData(tenantId);
        if (metadata == null) {
            addDefaultWhiteLabelThemeIfNotExist(tenantId);
            metadata = getWhiteLabelMetaData(tenantId);
            if (metadata == null) {
                String msg = "Whitelabel theme not found for tenant: " + tenantId + ". Further, Default White Label " +
                        "Theming Adding step failed.";
                log.error(msg);
                throw new MetadataManagementException(msg);
            }
        }
        WhiteLabelTheme whiteLabelTheme = new Gson().fromJson(metadata.getMetaValue(), WhiteLabelTheme.class);
        if (whiteLabelTheme.getDocUrl() == null) {
            updateWhiteLabelThemeWithDefaults(whiteLabelTheme, tenantId);
        }
        return whiteLabelTheme;
    }

    /**
     * Load White label Meta Data for given tenant Id.
     *
     * @param tenantId Id of the tenant
     * @return {@link Metadata}
     * @throws MetadataManagementException if an error occurred while getting Meta-Data info from Database for a
     *                                     given tenant ID.
     */
    private Metadata getWhiteLabelMetaData(int tenantId) throws MetadataManagementException {
        try {
            MetadataManagementDAOFactory.openConnection();
            return metadataDAO.getMetadata(tenantId, MetadataConstants.WHITELABEL_META_KEY);
        } catch (MetadataManagementDAOException e) {
            String msg = "Error occurred while retrieving white label theme for tenant:" + tenantId;
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } catch (SQLException e) {
            String msg = "Error occurred while opening a connection to the data source";
            log.error(msg, e);
            throw new MetadataManagementException(msg, e);
        } finally {
            MetadataManagementDAOFactory.closeConnection();
        }
    }
}
