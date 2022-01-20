package org.wso2.carbon.device.mgt.core.operation.timeout.task.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.device.mgt.common.operation.mgt.Activity;
import org.wso2.carbon.device.mgt.common.operation.mgt.ActivityStatus;
import org.wso2.carbon.device.mgt.common.operation.mgt.Operation;
import org.wso2.carbon.device.mgt.common.operation.mgt.OperationManagementException;
import org.wso2.carbon.device.mgt.core.config.operation.timeout.OperationTimeout;
import org.wso2.carbon.device.mgt.core.internal.DeviceManagementDataHolder;
import org.wso2.carbon.device.mgt.core.task.impl.DynamicPartitionedScheduleTask;

import java.util.List;
import java.util.Map;

public class OperationTimeoutTask extends DynamicPartitionedScheduleTask {

    private static final Log log = LogFactory.getLog(OperationTimeoutTask.class);
    private String deviceType;
    private OperationTimeout operationTimeoutConfig;

    @Override
    public void setProperties(Map<String, String> properties) {
        super.setProperties(properties);
        deviceType = properties.get(OperationTimeoutTaskManagerServiceImpl.DEVICE_TYPE);
        String operationTimeoutTaskConfigStr = properties.get(OperationTimeoutTaskManagerServiceImpl.OPERATION_TIMEOUT_TASK_CONFIG);
        Gson gson = new Gson();
        operationTimeoutConfig = gson.fromJson(operationTimeoutTaskConfigStr, OperationTimeout.class);
    }

    @Override
    public String getProperty(String name) {
        return super.getProperty(name);
    }

    @Override
    public void refreshContext() {
        super.refreshContext();
    }

    @Override
    protected void setup() {

    }

    @Override
    protected void executeDynamicTask() {
        try {

             long timeMillis = System.currentTimeMillis() - operationTimeoutConfig.getTimeout() * 60 * 1000;
            List<Activity> activities = DeviceManagementDataHolder.getInstance().getOperationManager()
                    .getActivities(deviceType, operationTimeoutConfig.getCode(), timeMillis,
                            operationTimeoutConfig.getInitialStatus());
            for (Activity activity : activities) {
                for(ActivityStatus activityStatus : activity.getActivityStatus()) {
                    String operationId = activity.getActivityId().replace("ACTIVITY_", "");
                    Operation operation = DeviceManagementDataHolder.getInstance().getOperationManager().getOperation(Integer.parseInt(operationId));
                    operation.setStatus(Operation.Status.valueOf(operationTimeoutConfig.getNextStatus()));
                    DeviceManagementDataHolder.getInstance().getOperationManager()
                            .updateOperation(activityStatus.getDeviceIdentifier(), operation);
                }
            }

        } catch (OperationManagementException e) {
            String msg = "Error occurred while retrieving operations.";
            log.error(msg, e);
        }
    }

}
