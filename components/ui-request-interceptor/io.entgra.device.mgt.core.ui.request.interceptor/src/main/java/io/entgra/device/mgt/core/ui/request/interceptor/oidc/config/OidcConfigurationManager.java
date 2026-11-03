/*
 * Copyright (c) 2018 - 2026, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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
package io.entgra.device.mgt.core.ui.request.interceptor.oidc.config;

import io.entgra.device.mgt.core.ui.request.interceptor.oidc.exception.OidcSessionManagementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads the UI request interceptor's OIDC settings from its own configuration file.
 */
public final class OidcConfigurationManager {
    private static final Log log = LogFactory.getLog(OidcConfigurationManager.class);
    private static final String CONFIG_FILE = "ui-request-interceptor.xml";
    private static final String BACK_CHANNEL_LOGOUT = "BackChannelLogout";
    private static final Pattern SYSTEM_PROPERTY = Pattern.compile("\\$\\{([^}]+)}");
    private static volatile BackChannelLogoutConfiguration configuration;

    private OidcConfigurationManager() { }

    public static BackChannelLogoutConfiguration getConfiguration() throws OidcSessionManagementException {
        BackChannelLogoutConfiguration current = configuration;
        if (current == null) {
            synchronized (OidcConfigurationManager.class) {
                current = configuration;
                if (current == null) {
                    current = loadConfiguration();
                    configuration = current;
                }
            }
        }
        return current;
    }

    private static BackChannelLogoutConfiguration loadConfiguration() throws OidcSessionManagementException {
        File configurationFile = new File(CarbonUtils.getCarbonConfigDirPath(), CONFIG_FILE);
        if (!configurationFile.isFile()) {
            log.warn("OIDC session management is disabled because configuration file was not found: " +
                    configurationFile.getAbsolutePath());
            return new BackChannelLogoutConfiguration();
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(configurationFile);
            NodeList sections = document.getElementsByTagName(BACK_CHANNEL_LOGOUT);
            if (sections.getLength() == 0) {
                log.warn("OIDC session management is disabled because BackChannelLogout is not configured in " +
                        configurationFile.getAbsolutePath());
                return new BackChannelLogoutConfiguration();
            }
            Element section = (Element) sections.item(0);
            BackChannelLogoutConfiguration result = new BackChannelLogoutConfiguration();
            result.setEnabled(Boolean.parseBoolean(value(section, "Enabled")));
            result.setIssuer(resolveSystemProperties(value(section, "Issuer")));
            result.setJwksUri(resolveSystemProperties(value(section, "JwksUri")));
            result.setAllowedClockSkewSeconds(integerValue(section, "AllowedClockSkewSeconds", 60));
            result.setStaleMappingRetentionSeconds(longValue(section, "StaleMappingRetentionSeconds", 86400));
            return result;
        } catch (Exception e) {
            throw new OidcSessionManagementException("Unable to load OIDC session configuration from " +
                    configurationFile.getAbsolutePath(), e);
        }
    }

    private static String value(Element parent, String name) {
        NodeList values = parent.getElementsByTagName(name);
        return values.getLength() == 0 ? null : values.item(0).getTextContent().trim();
    }

    private static int integerValue(Element parent, String name, int defaultValue) {
        String value = value(parent, name);
        return value == null || value.isEmpty() ? defaultValue : Integer.parseInt(value);
    }

    private static long longValue(Element parent, String name, long defaultValue) {
        String value = value(parent, name);
        return value == null || value.isEmpty() ? defaultValue : Long.parseLong(value);
    }

    private static String resolveSystemProperties(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = SYSTEM_PROPERTY.matcher(value);
        StringBuffer resolved = new StringBuffer();
        while (matcher.find()) {
            String replacement = System.getProperty(matcher.group(1), matcher.group(0));
            matcher.appendReplacement(resolved, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(resolved);
        return resolved.toString();
    }
}
