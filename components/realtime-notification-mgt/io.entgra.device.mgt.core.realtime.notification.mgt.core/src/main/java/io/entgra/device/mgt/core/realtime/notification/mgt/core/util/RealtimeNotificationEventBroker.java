/*
 * Copyright (c) 2018 - 2025, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
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

package io.entgra.device.mgt.core.realtime.notification.mgt.core.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A broker class responsible for managing and dispatching notification events
 * to registered {@link RealtimeNotificationListener}s.
 * This class maintains a thread-safe list of listeners and provides methods
 * to register listeners and broadcast messages to them based on targeted usernames.
 */
public class RealtimeNotificationEventBroker {

    /**
     * A thread-safe list of registered notification listeners.
     */
    private static final List<RealtimeNotificationListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Registers a {@link RealtimeNotificationListener} to receive notification messages.
     * @param listener the listener to be registered
     */
    public static void registerListener(RealtimeNotificationListener listener) {
        listeners.add(listener);
    }

    /**
     * Sends a notification message to all registered listeners, along with the
     * list of usernames for whom the message is intended.
     * @param message   the notification message content
     * @param usernames a list of usernames the message is intended for
     */
    public static void pushMessage(String message, List<String> usernames) {
        for (RealtimeNotificationListener listener : listeners) {
            listener.onMessage(message, usernames);
        }
    }
}
