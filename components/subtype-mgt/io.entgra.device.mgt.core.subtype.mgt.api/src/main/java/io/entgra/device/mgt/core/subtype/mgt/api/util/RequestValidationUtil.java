/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.entgra.device.mgt.core.subtype.mgt.api.util;

import io.entgra.device.mgt.core.subtype.mgt.api.exception.BadRequestException;
import io.entgra.device.mgt.core.subtype.mgt.dto.DeviceSubType;
import io.entgra.device.mgt.core.subtype.mgt.spi.DeviceSubTypeService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestValidationUtil {
    private static final Log log = LogFactory.getLog(RequestValidationUtil.class);
    public static void validateSubTypeId(String subTypeId) throws BadRequestException {
        if (subTypeId == null) {
            String msg = "Invalid subtype id: " + subTypeId;
            log.error(msg);
            throw new BadRequestException(msg);
        }
        if (StringUtils.isEmpty(subTypeId)) {
            String msg = "Value of subtype id cannot be empty. ";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        if (Integer.parseInt(subTypeId) <= 0) {
            String msg = "Invalid subtype id: " + subTypeId;
            log.error(msg);
            throw new BadRequestException(msg);
        }
    }

    public static void validateDeviceSubTypeName(String deviceSubTypeName) throws BadRequestException {
        if (deviceSubTypeName == null) {
            String msg = "Invalid subtype name";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        if (deviceSubTypeName.isEmpty()) {
            String msg = "Invalid subtype name: " + deviceSubTypeName;
            log.error(msg);
            throw new BadRequestException(msg);
        }

    }

    public static void validateSubType(DeviceSubType deviceSubType) throws BadRequestException {
        if (deviceSubType == null) {
            String msg = "Value of subtype cannot be empty. ";
            log.error(msg);
            throw new BadRequestException(msg);
        }

        validateSubTypeId(deviceSubType.getSubTypeId());
        validateDeviceSubTypeName(deviceSubType.getSubTypeName());

    }


}
