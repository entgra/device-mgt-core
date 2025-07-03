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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

public class NotificationListenerTest {

    private List<String> receivedMessages;
    private List<List<String>> receivedUserLists;
    private NotificationListener testListener;

    @BeforeMethod
    public void setUp() {
        receivedMessages = new ArrayList<>();
        receivedUserLists = new ArrayList<>();
        testListener = (message, usernames) -> {
            receivedMessages.add(message);
            receivedUserLists.add(usernames);
        };
        NotificationEventBroker.registerListener(testListener);
    }

    @Test
    public void testListenerReceivesNotification() {
        String testMessage = "Test notification message";
        List<String> users = Arrays.asList("alice", "bob");
        NotificationEventBroker.pushMessage(testMessage, users);
        assertEquals(receivedMessages.size(), 2);
        assertEquals(receivedMessages.get(0), testMessage);
        assertEquals(receivedUserLists.size(), 2);
        assertEquals(receivedUserLists.get(0), users);
    }

    @Test
    public void testListenerReceivesMultipleNotifications() {
        NotificationEventBroker.pushMessage("Message 1",
                Collections.singletonList("user1"));
        NotificationEventBroker.pushMessage("Message 2",
                Arrays.asList("user2", "user3"));
        assertEquals(receivedMessages.size(), 2);
        assertEquals(receivedMessages.get(0), "Message 1");
        assertEquals(receivedUserLists.get(1), Arrays.asList("user2", "user3"));
    }
}
