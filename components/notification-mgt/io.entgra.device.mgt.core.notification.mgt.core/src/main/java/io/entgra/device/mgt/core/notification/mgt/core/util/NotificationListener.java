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


package io.entgra.device.mgt.core.notification.mgt.core.util;

import java.util.List;

/**
 * A listener interface for receiving notification messages.
 * Implementations of this interface can be registered to receive
 * notifications targeted at specific users.
 * When a message is published via the notification system,
 * the {@code onMessage} method will be called with the message content
 * and a list of usernames to whom the message is intended.
 */
public interface NotificationListener {


    /**
     * @param message   the notification message content
     * @param usernames a list of usernames the message is intended for
     */
    void onMessage(String message, List<String> usernames);
}
