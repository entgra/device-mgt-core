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

package io.entgra.device.mgt.core.device.mgt.core.task.impl;

import io.entgra.device.mgt.core.device.mgt.core.archival.ArchivalException;
import io.entgra.device.mgt.core.device.mgt.core.archival.ArchivalService;
import io.entgra.device.mgt.core.device.mgt.core.archival.ArchivalServiceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.core.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class ArchivedDataDeletionTask extends RandomlyAssignedScheduleTask {

    private static final Log log = LogFactory.getLog(ArchivedDataDeletionTask.class);
    private static final String TASK_NAME = "ARCHIVED_DATA_CLEANUP_TASK";

    private ArchivalService archivalService;

    @Override
    public void setProperties(Map<String, String> map) {

    }

    @Override
    public void setup() {
        this.archivalService = new ArchivalServiceImpl();
    }

    @Override
    protected void executeRandomlyAssignedTask() {
        log.info("Executing DataDeletionTask at " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        long startTime = System.nanoTime();
        try {
            archivalService.deleteArchivedRecords();
        } catch (ArchivalException e) {
            log.error("An error occurred while executing DataDeletionTask", e);
        }
        long endTime = System.nanoTime();
        long difference = (endTime - startTime) / (1000000 * 1000);
        log.info("DataDeletionTask completed. Total execution time: " + difference + " seconds");
    }

    @Override
    public String getTaskName() {
        return TASK_NAME;
    }

}
