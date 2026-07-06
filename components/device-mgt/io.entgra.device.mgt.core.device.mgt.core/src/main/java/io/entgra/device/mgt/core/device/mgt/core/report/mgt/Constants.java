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

package io.entgra.device.mgt.core.device.mgt.core.report.mgt;

import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;

public class Constants {
    public static final String DEFAULT_CONFIG_FILE_LOCATION = CarbonUtils.getCarbonConfigDirPath() + File.separator +
            Constants.REPORT_MGT_CONFIG_XML_FILE;
    public static final String REPORT_MGT_CONFIG_XML_FILE = "reporting-mgt.xml";
    // device types
    public static final String ANDROID = "android";
    public static final String IOS = "ios";
    // device properties
    public static final String OS_BUILD_DATE = "OS_BUILD_DATE";
    public static final String OS_VERSION = "OS_VERSION";
    public static final String OS_VALUE = "OS_VALUE";
    // OS version value generating properties
    public static final int NUM_OF_OS_VERSION_DIGITS= 5;
    public static final int NUM_OF_OS_VERSION_POSITIONS = 3;
    public static final String BASIC_AUTH_HEADER_PREFIX = "Basic ";
    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String WSS_PROTOCOL = "wss";
    public static final String WS_PROTOCOL = "ws";
    public static final String SCHEME_SEPARATOR = "://";
    public static final String URI_SEPARATOR = "/";
    public static final String URI_QUERY_SEPARATOR = "?";
    public static final String COLON = ":";
    public static final String FILE_EXT_PNG = ".png";
    public static final String FILE_EXT_JPG = ".jpg";
    public static final String FILE_EXT_JPEG = ".jpeg";
    public static final String FILE_EXT_WEBP = ".webp";
    public static final String FILE_EXT_SVG = ".svg";
    public static final String CONTENT_TYPE_PNG = "image/png";
    public static final String CONTENT_TYPE_JPEG = "image/jpeg";
    public static final String CONTENT_TYPE_WEBP = "image/webp";
    public static final String CONTENT_TYPE_SVG = "image/svg+xml";
    public static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";
    // reporting publisher data labels
    public static final String REPORTING_DATA_LABEL = "data";
    public static final String REPORTING_DEVICE_LOGS_LABEL = "DEVICE_LOGS";

    public final static class BirtReporting {
        public static final String BIRT_REPORTING_HOST = "iot.birt.reporting.host";
        public static final String BIRT_REPORTING_API_REPORT_PATH = "/report/";
        public static final String BIRT_REPORTING_API_DOWNLOAD_TEMPLATE_URL = "/url";
        public static final String BIRT_REPORTING_API_TEMPLATE = "/design";
        public static final String UTF8_ENCODING = "UTF-8";
        public static final String BIRT_REPORTING_API_REPORT_DATA_PATH = "/report/data";
        public static final String BIRT_RPT_DESIGN_EXT = ".rptdesign";
        public static final String APP_USAGE = "APP_USAGE";
        public static final String DEVICE_INFO = "DEVICE_INFO";
        public static final String LOCATION_INFO = "LOCATION_INFO";
        public static final String UNSUPPORTED_REPORT_TYPE = "UNSUPPORTED";
        public static final String TENANT_ID = "tenantId";
        public static final String DEVICE_EVENT="DEVICE_EVENT";
        public static final String BIRT_REPORTING_API_UPLOAD_TEMPLATE_FILE = "/file";
        public static final String CATEGORY_ICON_UPLOAD_ENDPOINT = "device-mgt/v1.0/reports/category/icon/uploads";
        public static final String EVENT_TYPE = "EVENT_TYPE";
        public static final String EVENT_DATA = "EVENT_DATA";
        public static final String TIMESTAMP = "TIMESTAMP";
        public static final String DATA = "DATA";
        public static final int HTTP_STATUS_OK = 200;
        public static final int HTTP_STATUS_ACCEPTED = 202;
        public static final int HTTP_STATUS_ALREADY_REPORTED = 208;
        public static final int HTTP_STATUS_BAD_REQUEST = 400;
        public static final int HTTP_STATUS_NOT_FOUND = 404;
        public static final int HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;
        public static final String RESPONSE_FIELD_STATUS = "status";
        public static final String RESPONSE_FIELD_MESSAGE = "message";
        public static final String RESPONSE_ALREADY_EXISTS_MSG = "Design Report Name Already Exists";
        public static final String QUERY_PARAM_FILE_NAMES = "fileNames";
    }
}

