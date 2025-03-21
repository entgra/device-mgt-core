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

package io.entgra.device.mgt.core.policy.mgt.core.mock;

import io.entgra.device.mgt.core.device.mgt.common.ServerCtxInfo;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.dto.HeartBeatEvent;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.dto.ServerContext;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.exception.HeartBeatManagementException;
import io.entgra.device.mgt.core.server.bootup.heartbeat.beacon.service.HeartBeatManagementService;

import java.util.Map;

public class TestHeartBeatManagementService implements HeartBeatManagementService {
    @Override
    public boolean isTaskPartitioningEnabled() throws HeartBeatManagementException {
        return false;
    }

    @Override
    public ServerCtxInfo getServerCtxInfo() throws HeartBeatManagementException {
        return null;
    }

    @Override
    public String updateServerContext(ServerContext ctx) throws HeartBeatManagementException {
        return null;
    }

    @Override
    public boolean recordHeartBeat(HeartBeatEvent event) throws HeartBeatManagementException {
        return false;
    }

    @Override
    public void electCandidate(int elapsedTimeInSeconds) throws HeartBeatManagementException {

    }

    @Override
    public boolean updateTaskExecutionAcknowledgement(String newTask)
            throws HeartBeatManagementException {
        return false;
    }

    @Override
    public boolean isQualifiedToExecuteTask() throws HeartBeatManagementException {
        return false;
    }

    @Override public Map<Integer, ServerContext> getActiveServers() throws HeartBeatManagementException {
        return null;
    }

    @Override
    public void notifyClusterFormationChanged(int elapsedTimeInSeconds) throws HeartBeatManagementException {

    }
}
