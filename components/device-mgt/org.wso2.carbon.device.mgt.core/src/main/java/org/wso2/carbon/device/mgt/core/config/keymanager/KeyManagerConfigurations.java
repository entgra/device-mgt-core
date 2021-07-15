/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.core.config.keymanager;

import org.wso2.carbon.device.mgt.core.util.DeviceManagerUtil;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configurations related to key management.
 */
@XmlRootElement(name = "KeyManagerConfiguration")
public class KeyManagerConfigurations {
	private String serverUrl;
	private String adminUsername;
	private String adminPassword;

	@XmlElement(name = "AdminUsername", required = true)
	public String getAdminUsername() {
		return DeviceManagerUtil.replaceSystemProperty(adminUsername);
	}

	public void setAdminUsername(String adminUsername) {
		this.adminUsername = adminUsername;
	}

	@XmlElement(name = "AdminPassword", required = true)
	public String getAdminPassword() {
		return DeviceManagerUtil.replaceSystemProperty(adminPassword);
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	@XmlElement(name = "ServerUrl", required = true)
	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

}
