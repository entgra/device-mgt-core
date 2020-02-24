/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.device.mgt.common.exceptions;

public class EventPublishingException extends Exception {

    private static final long serialVersionUID = -3251556311996070297L;

    public EventPublishingException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
    }

    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventPublishingException(String msg) {
        super(msg);
    }

    public EventPublishingException() {
        super();
    }

    public EventPublishingException(Throwable cause) {
        super(cause);
    }

}
