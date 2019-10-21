/*
 * Copyright (c) 2019, Entgra (Pvt) Ltd. (http://entgra.io) All Rights Reserved.
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
package org.wso2.carbon.policy.mgt.common;

public class PolicyTransformException extends Exception {

    private static final long serialVersionUID = -4976670117832668628L;

    public PolicyTransformException(String message, Throwable cause) {
        super(message, cause);
    }

    public PolicyTransformException(Throwable cause) {
        super(cause);
    }

    public PolicyTransformException(String msg) {
        super(msg);
    }

    public PolicyTransformException() {
        super();
    }
}
