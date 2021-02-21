/*
 * Copyright (c) 2021, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
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

package org.wso2.carbon.device.mgt.common.policy.mgt.ui;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "APIContent")
public class APIContent {

    private String apiContentKey;
    private Item item;

    @XmlElement(name = "APIContentKey")
    public String getApiContentKey() {
        return apiContentKey;
    }

    public void setApiContentKey(String apiContentKey) {
        this.apiContentKey = apiContentKey;
    }

    @XmlElement(name = "Item")
    public Item getItem() { return item; }

    public void setItem(Item item) { this.item = item; }
}
