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


package io.entgra.device.mgt.core.policy.mgt.core.mgt;


import io.entgra.device.mgt.core.device.mgt.common.Feature;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Profile;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.ProfileFeature;
import io.entgra.device.mgt.core.policy.mgt.common.FeatureManagementException;

import java.util.List;

public interface FeatureManager {

    ProfileFeature addProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagementException;

    ProfileFeature updateProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagementException;

    List<ProfileFeature> addProfileFeatures(List<ProfileFeature> features, int profileId)
            throws FeatureManagementException;

    List<ProfileFeature> updateProfileFeatures(List<ProfileFeature> features, int profileId)
            throws FeatureManagementException;

    List<Feature> getAllFeatures(String deviceType) throws FeatureManagementException;

    List<ProfileFeature> getFeaturesForProfile(int profileId) throws FeatureManagementException;

    boolean deleteFeaturesOfProfile(Profile profile) throws FeatureManagementException;
}
