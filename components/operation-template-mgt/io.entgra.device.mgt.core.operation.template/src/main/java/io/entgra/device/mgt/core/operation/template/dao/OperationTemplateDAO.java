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

package io.entgra.device.mgt.core.operation.template.dao;

import io.entgra.device.mgt.core.operation.template.dto.OperationTemplate;
import io.entgra.device.mgt.core.operation.template.exception.OperationTemplateManagementDAOException;

import java.util.List;
import java.util.Set;

/**
 * This class represents the key operations associated with persisting mobile-device related
 * information.
 */
public interface OperationTemplateDAO {
    void addOperationTemplate(OperationTemplate operationTemplate) throws OperationTemplateManagementDAOException;

    OperationTemplate updateOperationTemplate(OperationTemplate operationTemplate)
            throws OperationTemplateManagementDAOException;

    OperationTemplate getOperationTemplate(String deviceType, String subTypeId, String operationCode) throws OperationTemplateManagementDAOException;

    List<OperationTemplate> getAllOperationTemplates(String deviceType) throws OperationTemplateManagementDAOException;

    /**
     * Retrieves the operation registers associated with the specified device type, subtype, and operation code.
     *
     * @param deviceType the type of the device
     * @param subTypeId the identifier of the device subtype
     * @param operationCode the code of the operation
     * @return a list of operation register names
     * @throws OperationTemplateManagementDAOException if an error occurs while retrieving the operation registers
     */
    List<String> getOperationRegisters(String deviceType, String subTypeId, String operationCode)
            throws OperationTemplateManagementDAOException;

    boolean deleteOperationTemplate(String deviceType, String subTypeId, String operationCode) throws OperationTemplateManagementDAOException;

     Set<String> getOperationTemplateCodes(String deviceType, String subTypeId) throws OperationTemplateManagementDAOException;
}
