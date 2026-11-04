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
package io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.entgra.device.mgt.core.device.mgt.common.operation.mgt.OperationManagementException;
import io.entgra.device.mgt.core.device.mgt.core.config.DeviceConfigurationManager;
import io.entgra.device.mgt.core.device.mgt.core.config.push.notification.FCMConfiguration;
import io.entgra.device.mgt.core.device.mgt.core.config.push.notification.PushNotificationConfiguration;
import io.entgra.device.mgt.core.device.mgt.extensions.push.notification.provider.fcm.internal.FCMDataHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Process-wide singleton that dispatches FCM wake-up calls, solving two problems:
 *
 * <ol>
 *   <li><b>Priority:</b> user initiated (high priority) wake-up calls are dispatched on a
 *       dedicated pool that is fully isolated from the throttled pool used for task initiated
 *       (low priority) wake-up calls, so a running task that floods the queue with thousands of
 *       wake-up calls can never delay a user's wake-up call.</li>
 *   <li><b>Deduplication:</b> after a wake-up call is sent to a device, further calls to the same
 *       device are suppressed for a fixed cooldown period (a single wake-up makes the device pull
 *       all pending operations, so duplicate calls are wasteful). User initiated calls bypass the
 *       cooldown and refresh its timestamp.</li>
 * </ol>
 *
 * This is a singleton (not tied to a {@code FCMNotificationStrategy} instance) because the
 * strategy may be re-created periodically by the operation manager's tenant-aware strategy cache;
 * keeping the pools and cooldown cache here prevents leaking thread pools on every refresh.
 */
public class FCMWakeUpDispatcher {

    private static final Log log = LogFactory.getLog(FCMWakeUpDispatcher.class);

    private static volatile FCMWakeUpDispatcher instance;

    private final ThreadPoolExecutor userPool;
    private final ThreadPoolExecutor taskPool;
    /** key -> timestamp (ms) of the last wake-up call sent to the device. */
    private final Cache<String, Long> cooldownCache;
    private final long cooldownMillis;

    private FCMWakeUpDispatcher() {
        FCMConfiguration config = getFCMConfiguration();
        this.cooldownMillis = TimeUnit.SECONDS.toMillis(config.getDeviceCooldownSeconds());
        this.cooldownCache = CacheBuilder.newBuilder()
                .expireAfterWrite(config.getDeviceCooldownSeconds(), TimeUnit.SECONDS)
                .build();
        this.userPool = buildPool("fcm-user-wakeup", config.getUserPoolSize(),
                config.getUserQueueCapacity());
        this.taskPool = buildPool("fcm-task-wakeup", config.getTaskPoolSize(),
                config.getTaskQueueCapacity());
        log.info("FCM wake-up dispatcher initialized. cooldown=" + config.getDeviceCooldownSeconds()
                + "s, userPool=" + config.getUserPoolSize() + ", taskPool=" + config.getTaskPoolSize());
    }

    public static FCMWakeUpDispatcher getInstance() {
        if (instance == null) {
            synchronized (FCMWakeUpDispatcher.class) {
                if (instance == null) {
                    instance = new FCMWakeUpDispatcher();
                }
            }
        }
        return instance;
    }

    private static FCMConfiguration getFCMConfiguration() {
        PushNotificationConfiguration pushNotificationConfiguration = DeviceConfigurationManager.getInstance()
                .getDeviceManagementConfig().getPushNotificationConfiguration();
        FCMConfiguration fcmConfiguration = pushNotificationConfiguration.getFCMConfiguration();
        // Fall back to defaults if the FCMConfiguration block is absent from cdm-config.xml.
        return fcmConfiguration != null ? fcmConfiguration : new FCMConfiguration();
    }

    /**
     * Build a bounded pool. The task pool drops the oldest queued wake-up call when full; those
     * devices are eventually re-notified by the periodic push notification scheduler task.
     */
    private ThreadPoolExecutor buildPool(String name, int poolSize, int queueCapacity) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicLong counter = new AtomicLong();
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, name + "-" + counter.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        };
        return new ThreadPoolExecutor(poolSize, poolSize, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity), threadFactory,
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    /**
     * Enqueue a wake-up call for asynchronous delivery and return immediately.
     *
     * @param request the wake-up call to deliver
     */
    public void dispatch(FCMWakeUpRequest request) {
        boolean highPriority = request.isHighPriority();
        String key = cooldownKey(request.getTenantId(), request.getDeviceIdentifier());

        if (highPriority) {
            // User initiated calls bypass the cooldown but refresh its timestamp so that
            // subsequent task initiated calls to the same device remain suppressed.
            cooldownCache.put(key, System.currentTimeMillis());
        } else if (isInCooldown(key)) {
            if (log.isDebugEnabled()) {
                log.debug("Suppressing duplicate FCM wake-up call for device " + request.getDeviceIdentifier()
                        + " (within cooldown window).");
            }
            return;
        } else {
            // Atomically claim the cooldown slot; if another thread won the race, suppress.
            Long previous = cooldownCache.asMap().putIfAbsent(key, System.currentTimeMillis());
            if (previous != null && (System.currentTimeMillis() - previous) < cooldownMillis) {
                if (log.isDebugEnabled()) {
                    log.debug("Suppressing duplicate FCM wake-up call for device " + request.getDeviceIdentifier()
                            + " (lost cooldown race).");
                }
                return;
            }
        }

        ThreadPoolExecutor pool = highPriority ? userPool : taskPool;
        pool.execute(() -> deliver(request));
    }

    private void deliver(FCMWakeUpRequest request) {
        PrivilegedCarbonContext.startTenantFlow();
        try {
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(request.getTenantId(), true);
            request.getSender().send();
        } catch (Exception e) {
            log.error("Error occurred while sending FCM wake-up call to device " + request.getDeviceIdentifier()
                    + ". Rescheduling for retry.", e);
            reschedule(request);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Reschedule a failed wake-up call so the periodic push notification scheduler task retries it.
     */
    private void reschedule(FCMWakeUpRequest request) {
        if (request.getOperationId() <= 0 || request.getEnrolmentId() <= 0) {
            return;
        }
        try {
            FCMDataHolder.getInstance().getDeviceManagementProviderService()
                    .scheduleNotification(request.getDeviceType(), request.getOperationId(),
                            request.getEnrolmentId());
        } catch (OperationManagementException e) {
            log.error("Unable to reschedule FCM wake-up call. Operation ID: " + request.getOperationId()
                    + ", Enrolment ID: " + request.getEnrolmentId() + ", Device ID: "
                    + request.getDeviceIdentifier(), e);
        }
    }

    private boolean isInCooldown(String key) {
        Long lastSent = cooldownCache.getIfPresent(key);
        return lastSent != null && (System.currentTimeMillis() - lastSent) < cooldownMillis;
    }

    private static String cooldownKey(int tenantId, String deviceIdentifier) {
        return tenantId + ":" + deviceIdentifier;
    }

    /**
     * Shut down the dispatcher pools. Invoked on bundle deactivation.
     */
    public void shutdown() {
        userPool.shutdownNow();
        taskPool.shutdownNow();
    }
}
