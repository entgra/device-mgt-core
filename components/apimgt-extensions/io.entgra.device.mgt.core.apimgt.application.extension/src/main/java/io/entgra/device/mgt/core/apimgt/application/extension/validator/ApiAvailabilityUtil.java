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
package io.entgra.device.mgt.core.apimgt.application.extension.validator;

import io.entgra.device.mgt.core.apimgt.application.extension.exception.APIManagerException;
import io.entgra.device.mgt.core.apimgt.application.extension.internal.APIApplicationManagerExtensionDataHolder;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.ConsumerRESTAPIServices;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.bean.APIMConsumer.APIInfo;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.APIServicesException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.BadRequestException;
import io.entgra.device.mgt.core.apimgt.extension.rest.api.exceptions.UnexpectedResponseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Shared helper for checking API availability on the gateway via the consumer REST API.
 */
public final class ApiAvailabilityUtil {

    private static final Log log = LogFactory.getLog(ApiAvailabilityUtil.class);

    private ApiAvailabilityUtil() {
    }

    /**
     * @param apiSearchQuery Dev-portal API search query
     * @param tenantDomain   Tenant domain (used for logging only)
     * @return true when at least one API matches the search query
     * @throws APIManagerException when the availability check cannot be completed
     */
    public static boolean areApisPublished(String apiSearchQuery, String tenantDomain) throws APIManagerException {
        ConsumerRESTAPIServices consumerRESTAPIServices =
                APIApplicationManagerExtensionDataHolder.getInstance().getConsumerRESTAPIServices();

        try {
            Map<String, String> queryParam = new HashMap<>();
            queryParam.put("query", apiSearchQuery);

            APIInfo[] apis = consumerRESTAPIServices.getAllApis(queryParam, new HashMap<>());

            if (apis == null || apis.length == 0) {
                log.warn("No APIs matching query [" + apiSearchQuery + "] for tenant: " + tenantDomain);
                return false;
            }

            if (log.isDebugEnabled()) {
                log.debug("Found " + apis.length + " API(s) matching query [" + apiSearchQuery
                        + "] for tenant: " + tenantDomain);
            }
            return true;

        } catch (APIServicesException | BadRequestException | UnexpectedResponseException e) {
            String msg = "Error encountered while checking API availability for tenant: " + tenantDomain
                    + ", query: " + apiSearchQuery;
            log.error(msg, e);
            throw new APIManagerException(msg, e);
        }
    }
}
