/*
 *  Copyright (c) 2018 - 2024, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.entgra.device.mgt.core.cea.mgt.enforce.service;

import io.entgra.device.mgt.core.cea.mgt.enforce.exception.CEAPolicyOperationException;

public interface CEAPolicyOperation {
    /**
     * Sync with the active sync server and enforce the conditional access policy
     *
     * @throws CEAPolicyOperationException Throws when error occurred while performing enforcement operations
     */
    void enforce() throws CEAPolicyOperationException;
}
