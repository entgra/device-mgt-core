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


package io.entgra.device.mgt.core.certificate.mgt.core.util;

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import io.entgra.device.mgt.core.certificate.mgt.core.config.CertificateConfigurationManager;
import io.entgra.device.mgt.core.certificate.mgt.core.config.CertificateManagementConfig;
import io.entgra.device.mgt.core.certificate.mgt.core.config.datasource.DataSourceConfig;
import io.entgra.device.mgt.core.certificate.mgt.core.config.datasource.JNDILookupDefinition;
import io.entgra.device.mgt.core.certificate.mgt.core.dao.CertificateManagementDAOUtil;
import io.entgra.device.mgt.core.certificate.mgt.core.exception.CertificateManagementException;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.sql.DataSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Hashtable;
import java.util.List;

public class CertificateManagerUtil {

    public static final String GENERAL_CONFIG_RESOURCE_PATH = "general";
    public static final String MONITORING_FREQUENCY = "notifierFrequency";
    private static MetadataManagementService metadataManagementService;
    private static final Log log = LogFactory.getLog(CertificateManagerUtil.class);

    public static Document convertToDocument(File file) throws CertificateManagementException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            return docBuilder.parse(file);
        } catch (Exception e) {
            throw new CertificateManagementException("Error occurred while parsing file, while converting " +
                                                     "to a org.w3c.dom.Document : " + e.getMessage(), e);
        }
    }

    /**
     * Resolve data source from the data source definition
     *
     * @param config data source configuration
     * @return data source resolved from the data source definition
     */
    public static DataSource resolveDataSource(DataSourceConfig config) {
        DataSource dataSource = null;
        if (config == null) {
            throw new RuntimeException("Device Management Repository data source configuration " +
                                       "is null and thus, is not initialized");
        }
        JNDILookupDefinition jndiConfig = config.getJndiLookupDefinition();
        if (jndiConfig != null) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing Device Management Repository data source using the JNDI " +
                          "Lookup Definition");
            }
            List<JNDILookupDefinition.JNDIProperty> jndiPropertyList =
                    jndiConfig.getJndiProperties();
            if (jndiPropertyList != null) {
                Hashtable<Object, Object> jndiProperties = new Hashtable<Object, Object>();
                for (JNDILookupDefinition.JNDIProperty prop : jndiPropertyList) {
                    jndiProperties.put(prop.getName(), prop.getValue());
                }
                dataSource =
                        CertificateManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), jndiProperties);
            } else {
                dataSource = CertificateManagementDAOUtil.lookupDataSource(jndiConfig.getJndiName(), null);
            }
        }
        return dataSource;
    }

    public static int validateCertificateListPageSize(int limit) throws CertificateManagementException {
        if (limit == 0) {
            CertificateManagementConfig certificateManagementConfig = CertificateConfigurationManager.getInstance().
                    getCertificateManagementConfig();
            if (certificateManagementConfig != null) {
                return certificateManagementConfig.getDefaultPageSize();
            } else {
                throw new CertificateManagementException("Certificate-Mgt configuration has not initialized. Please check the " +
                                                    "certificate-config.xml file.");
            }
        }
        return limit;
    }

    /**
     * Initializing and accessing method for MetadataManagementService.
     *
     * @return MetadataManagementService instance
     * @throws IllegalStateException if metadataManagementService cannot be initialized
     */
    public static MetadataManagementService getMetadataManagementService() {
        if (metadataManagementService == null) {
            synchronized (CertificateManagerUtil.class) {
                if (metadataManagementService == null) {
                    PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                    metadataManagementService = (MetadataManagementService) ctx.getOSGiService(
                            MetadataManagementService.class, null);
                    if (metadataManagementService == null) {
                        throw new IllegalStateException("Metadata Management service not initialized.");
                    }
                }
            }
        }
        return metadataManagementService;
    }

}