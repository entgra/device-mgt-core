/*
 *   Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 *  Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.entgra.device.mgt.core.dynamic.task.mgt.core;

import io.entgra.device.mgt.core.device.mgt.common.exceptions.MetadataManagementException;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.Metadata;
import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.DynamicTaskConfigurationManagementService;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.CategorizedDynamicTask;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.bean.DynamicTaskPlatformConfigurations;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskManagementException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.DynamicTaskScheduleException;
import io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.api.NotFoundException;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.constant.Constants;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.internal.DynamicTaskManagementExtensionServiceDataHolder;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.DynamicTaskContextPatchExecutor;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.DynamicTaskManagementUtil;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.DynamicTaskPatch;
import io.entgra.device.mgt.core.dynamic.task.mgt.core.util.DynamicTaskSchedulerUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DynamicTaskConfigurationManagementServiceImpl implements DynamicTaskConfigurationManagementService {
    private static final Log log = LogFactory.getLog(DynamicTaskConfigurationManagementServiceImpl.class);

    private DynamicTaskConfigurationManagementServiceImpl() {
    }

    public static DynamicTaskConfigurationManagementService getInstance() {
        return ReferenceHolder.INSTANCE;
    }

    /**
     * Get effective dynamic task platform configuration after mapping existing configuration against updated ones.
     *
     * @param tenantDomain                   Tenant domain.
     * @param updatedCategorizedDynamicTasks Updated dynamic tasks.
     * @return Effective {@link DynamicTaskPlatformConfigurations}
     * @throws DynamicTaskManagementException Throws when error encountered while getting effective configurations.
     */
    private static DynamicTaskPlatformConfigurations getEffectiveDynamicTaskPlatformConfigurations(String tenantDomain, Set<CategorizedDynamicTask> updatedCategorizedDynamicTasks) throws DynamicTaskManagementException {
        try {
            List<CategorizedDynamicTask> existingCategorizedDynamicTasks =
                    new ArrayList<>(DynamicTaskManagementUtil.getDynamicTaskPlatformConfigurations(tenantDomain).getCategorizedDynamicTasks());
            for (CategorizedDynamicTask updatedCategorizedDynamicTask : updatedCategorizedDynamicTasks) {
                if (existingCategorizedDynamicTasks.contains(updatedCategorizedDynamicTask)) {
                    CategorizedDynamicTask existingCategorizedDynamicTask =
                            existingCategorizedDynamicTasks.get(existingCategorizedDynamicTasks.indexOf(updatedCategorizedDynamicTask));
                    existingCategorizedDynamicTask.setEnable(updatedCategorizedDynamicTask.isEnable());
                    existingCategorizedDynamicTask.setFrequency(updatedCategorizedDynamicTask.getFrequency());
                    existingCategorizedDynamicTask.setDeviceTypes(updatedCategorizedDynamicTask.getDeviceTypes());
                    existingCategorizedDynamicTask.setOperationCodes(updatedCategorizedDynamicTask.getOperationCodes());
                }
            }
            return new DynamicTaskPlatformConfigurations(new HashSet<>(existingCategorizedDynamicTasks));
        } catch (NotFoundException e) {
            String msg =
                    "Failed to locate categorized dynamic task configuration for tenant domain [" + tenantDomain + "].";
            log.error(msg);
            throw new DynamicTaskManagementException(msg, e);
        }
    }

    /**
     * Update and record updated dynamic task platform configurations to the metadata registry.
     *
     * @param tenantDomain                      Tenant domain.
     * @param dynamicTaskPlatformConfigurations {@link DynamicTaskPlatformConfigurations}
     * @throws DynamicTaskManagementException Throws when error encountered while updating the metadata registry.
     */
    private void updateMetaRegistry(String tenantDomain,
                                    DynamicTaskPlatformConfigurations dynamicTaskPlatformConfigurations) throws DynamicTaskManagementException {
        MetadataManagementService metadataManagementService =
                DynamicTaskManagementExtensionServiceDataHolder.getInstance().getMetadataManagementService();
        Metadata updatedMetadataEntry = new Metadata();
        updatedMetadataEntry.setMetaKey(tenantDomain + Constants.CONFIG_PREFIX.CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX);
        updatedMetadataEntry.setMetaValue(DynamicTaskManagementExtensionServiceDataHolder.getGson().toJson(dynamicTaskPlatformConfigurations));
        try {
            // Do not remove the tenant safety check, since the service can be invoked by tenanted thread, even though
            // the API level restriction is ensured.
            if (Objects.equals(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(),
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                metadataManagementService.updateMetadata(updatedMetadataEntry);
            } else {
                try {
                    PrivilegedCarbonContext.startTenantFlow();
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                    metadataManagementService.updateMetadata(updatedMetadataEntry);
                } finally {
                    PrivilegedCarbonContext.endTenantFlow();
                }
            }
        } catch (MetadataManagementException e) {
            String msg =
                    "Error encountered while updating the effective configuration entry for key [" + tenantDomain +
                            Constants.CONFIG_PREFIX.CONFIGURABLE_CATEGORIZED_DYNAMIC_TASK_CONFIG_PREFIX + "]";
            log.error(msg, e);
            throw new DynamicTaskManagementException(msg, e);
        }
    }

    @Override
    public DynamicTaskPlatformConfigurations getDynamicTaskPlatformConfigurations(String tenantDomain) throws NotFoundException,
            DynamicTaskManagementException {
        return DynamicTaskManagementUtil.getDynamicTaskPlatformConfigurations(tenantDomain);
    }

    @Override
    public DynamicTaskPlatformConfigurations updateCategorizedDynamicTasks(String tenantDomain,
                                                                           Set<CategorizedDynamicTask> updatedCategorizedDynamicTasks) throws DynamicTaskManagementException {
        DynamicTaskPlatformConfigurations effectiveDynamicTaskPlatformConfigurations =
                getEffectiveDynamicTaskPlatformConfigurations(tenantDomain, updatedCategorizedDynamicTasks);
        updateMetaRegistry(tenantDomain, effectiveDynamicTaskPlatformConfigurations);
        DynamicTaskContextPatchExecutor.getInstance().patch(new DynamicTaskPatch(tenantDomain,
                updatedCategorizedDynamicTasks));
        return effectiveDynamicTaskPlatformConfigurations;
    }

    @Override
    public DynamicTaskPlatformConfigurations addCategorizedDynamicTask(String tenantDomain,
                                                                       CategorizedDynamicTask newCategorizedDynamicTask)
            throws DynamicTaskManagementException {
        List<CategorizedDynamicTask> existingCategorizedDynamicTasks;
        try {
            existingCategorizedDynamicTasks =
                    new ArrayList<>(DynamicTaskManagementUtil.getDynamicTaskPlatformConfigurations(tenantDomain)
                            .getCategorizedDynamicTasks());
        } catch (NotFoundException e) {
            String msg =
                    "Failed to locate categorized dynamic task configuration for tenant domain [" + tenantDomain + "].";
            log.error(msg, e);
            throw new DynamicTaskManagementException(msg, e);
        }

        if (existingCategorizedDynamicTasks.contains(newCategorizedDynamicTask)) {
            String msg =
                    "Categorized dynamic task [" + newCategorizedDynamicTask.getCategoryCode() + "] already exists " +
                            "for tenant domain [" + tenantDomain + "].";
            log.error(msg);
            throw new DynamicTaskManagementException(msg);
        }

        existingCategorizedDynamicTasks.add(newCategorizedDynamicTask);
        DynamicTaskPlatformConfigurations updatedDynamicTaskPlatformConfigurations =
                new DynamicTaskPlatformConfigurations(new HashSet<>(existingCategorizedDynamicTasks));
        DynamicTaskManagementUtil.populateConfigurableDeviceTypes(updatedDynamicTaskPlatformConfigurations);

        updateMetaRegistry(tenantDomain, updatedDynamicTaskPlatformConfigurations);
        scheduleNewCategorizedDynamicTask(tenantDomain, newCategorizedDynamicTask);

        return updatedDynamicTaskPlatformConfigurations;
    }

    /**
     * Schedule a newly added categorized dynamic task for the first time.
     * <p>
     * Unlike {@link DynamicTaskContextPatchExecutor}, which patches already-scheduled tasks asynchronously on a
     * background thread, this runs synchronously so that a scheduling failure for a brand-new category is reported
     * back to the caller instead of being silently logged. This mirrors how {@code TenantCreateObserver} schedules
     * a tenant's initial set of categorized dynamic tasks.
     *
     * @param tenantDomain           Tenant domain that owns the new categorized dynamic task.
     * @param categorizedDynamicTask New {@link CategorizedDynamicTask} to schedule.
     * @throws DynamicTaskManagementException Throws when error encountered while scheduling the new categorized
     *                                        dynamic task. The metadata registry has already been updated to
     *                                        include this category by the time this can be thrown.
     */
    private void scheduleNewCategorizedDynamicTask(String tenantDomain, CategorizedDynamicTask categorizedDynamicTask)
            throws DynamicTaskManagementException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            DynamicTaskSchedulerUtil.scheduleDynamicTask(categorizedDynamicTask, tenantId, tenantDomain);
        } catch (DynamicTaskScheduleException e) {
            String msg =
                    "Failed to schedule the newly added categorized dynamic task [" +
                            categorizedDynamicTask.getCategoryCode() + "] for tenant domain [" + tenantDomain +
                            "]. The metadata registry has already been updated to include this category; manual " +
                            "reconciliation may be required.";
            log.error(msg, e);
            throw new DynamicTaskManagementException(msg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public DynamicTaskPlatformConfigurations resetToDefault(String tenantDomain) throws DynamicTaskManagementException {
        return updateCategorizedDynamicTasks(tenantDomain,
                DynamicTaskManagementUtil.getDefaultConfigurableDynamicTaskPlatformConfigurations().getCategorizedDynamicTasks());
    }

    private static class ReferenceHolder {
        public static DynamicTaskConfigurationManagementServiceImpl INSTANCE =
                new DynamicTaskConfigurationManagementServiceImpl();
    }
}
