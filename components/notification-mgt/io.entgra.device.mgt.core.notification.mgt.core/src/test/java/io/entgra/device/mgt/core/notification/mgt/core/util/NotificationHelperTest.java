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

import io.entgra.device.mgt.core.device.mgt.common.metadata.mgt.MetadataManagementService;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfig;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigRecipients;
import io.entgra.device.mgt.core.notification.mgt.common.beans.NotificationConfigurationList;
import io.entgra.device.mgt.core.notification.mgt.common.exception.NotificationManagementException;
import io.entgra.device.mgt.core.notification.mgt.core.internal.NotificationManagementDataHolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

public class NotificationHelperTest {

    @Mock
    private MetadataManagementService metaSvc;

    @Mock
    private RealmService realmSvc;

    @Mock
    private UserStoreManager userStoreManager;

    @Mock
    private UserRealm userRealm;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void resetDataHolder() {
        NotificationManagementDataHolder.getInstance().setMetaDataManagementService(metaSvc);
        NotificationManagementDataHolder.getInstance().setRealmService(realmSvc);
    }

    @Test
    public void testExtractUsernamesFromRecipientsNull() throws Exception {
        List<String> out = NotificationHelper.extractUsernamesFromRecipients(null, 1);
        Assert.assertTrue(out.isEmpty());
    }

    @Test
    public void testExtractUsernames_FromUsersAndRoles() throws Exception {
        NotificationConfigRecipients rec = new NotificationConfigRecipients();
        rec.setUsers(Arrays.asList("u1", "u2"));
        rec.setRoles(Arrays.asList("roleA"));
        Mockito.when(realmSvc.getTenantUserRealm(1)).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.when(userStoreManager
                .getUserListOfRole("roleA")).thenReturn(new String[]{"u3", "u2"});
        List<String> out = NotificationHelper.extractUsernamesFromRecipients(rec, 1);
        Assert.assertTrue(out.containsAll(Arrays.asList("u1", "u2", "u3")));
    }

    @Test
    public void testResolveCutoffTimestamp_valid() {
        Timestamp ts = NotificationHelper.resolveCutoffTimestamp("2 days");
        Assert.assertNotNull(ts);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testResolveCutoffTimestamp_invalid() {
        NotificationHelper.resolveCutoffTimestamp("abc");
    }

    @Test
    public void testGetNotificationConfigurationByCode_nullMeta() throws Exception {
        Mockito.when(metaSvc.retrieveMetadata(
                NotificationHelper.NOTIFICATION_CONFIG_META_KEY)).thenReturn(null);
        NotificationConfig out = NotificationHelper.getNotificationConfigurationByCode("X");
        Assert.assertNull(out);
    }

    @Test(expectedExceptions = NotificationManagementException.class)
    public void testGetNotificationConfigurationByCode_nullService() throws Exception {
        NotificationManagementDataHolder.getInstance().setMetaDataManagementService(null);
        NotificationHelper.getNotificationConfigurationByCode("X");
    }

    @Test
    public void testGetNotificationConfigurationsFromMetadata_nullMeta() throws Exception {
        Mockito.when(metaSvc.retrieveMetadata(
                NotificationHelper.NOTIFICATION_CONFIG_META_KEY)).thenReturn(null);
        NotificationConfigurationList result =
                NotificationHelper.getNotificationConfigurationsFromMetadata();
        Assert.assertNull(result);
    }
}
