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


package io.entgra.device.mgt.core.policy.mgt.core.dao;

import io.entgra.device.mgt.core.device.mgt.common.Feature;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.Profile;
import io.entgra.device.mgt.core.device.mgt.common.policy.mgt.ProfileFeature;

import java.util.List;

/**
 * This interface represents the key operations related to profile features of device policies.
 */
public interface FeatureDAO {

    /**
     * This method is used to add a feature related to given profile.
     *
     * @param feature consists of device specific configurations.
     * @param profileId id of the profile.
     * @return returns ProfileFeature object.
     * @throws FeatureManagerDAOException
     */
    ProfileFeature addProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagerDAOException;

    /**
     * This method is used to update a feature related to given profile.
     * @param feature consists of device specific configurations.
     * @param profileId id of the profile.
     * @return returns updated ProfileFeature object.
     * @throws FeatureManagerDAOException
     */
    ProfileFeature updateProfileFeature(ProfileFeature feature, int profileId) throws FeatureManagerDAOException;

    /**
     * This method is used to add set of features to a given profile.
     *
     * @param features consists of device specific configurations.
     * @param profileId id of the profile.
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<ProfileFeature> addProfileFeatures(List<ProfileFeature> features, int profileId) throws
            FeatureManagerDAOException;

    /**
     * This method is used to update set of features to a given profile.
     *
     * @param features consists of device specific configurations.
     * @param profileId id of the profile.
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<ProfileFeature> updateProfileFeatures(List<ProfileFeature> features, int profileId) throws
            FeatureManagerDAOException;

    /**
     * This method is used to retrieve all the profile features.
     *
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<ProfileFeature> getAllProfileFeatures() throws FeatureManagerDAOException;

    /**
     * This method is used to retrieve all the profile features based on device type.
     *
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<Feature> getAllFeatures(String deviceType) throws FeatureManagerDAOException;

    /**
     * This method is used to retrieve all the profile features of given profile.
     *
     * @param profileId id of the profile.
     * @return returns list of ProfileFeature objects.
     * @throws FeatureManagerDAOException
     */
    List<ProfileFeature> getFeaturesForProfile(int profileId) throws FeatureManagerDAOException;

    /**
     * This method is used to remove set of features of given profile.
     *
     * @param profile that contains features to be removed.
     * @return returns true if success.
     * @throws FeatureManagerDAOException
     */
    boolean deleteFeaturesOfProfile(Profile profile) throws FeatureManagerDAOException;

    /**
     * This method is used to remove set of features of given profile id.
     *
     * @param profileId id of the profile.
     * @return returns true if success.
     * @throws FeatureManagerDAOException
     */
    boolean deleteFeaturesOfProfile(int profileId) throws FeatureManagerDAOException;

    /**
     * This method is used to remove a profile feature of given feature id.
     *
     * @param featureId id of the feature.
     * @return returns true if success.
     * @throws FeatureManagerDAOException
     */
    boolean deleteProfileFeatures(int featureId) throws FeatureManagerDAOException;

}
