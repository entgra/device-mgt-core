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
package io.entgra.device.mgt.core.application.mgt.common;

import java.util.TreeMap;

/**
 * Rating represents the an overall rating value and number of users who has rated for an application release.
 */
public class Rating {

    /**
     * Rating value of the application release.
     */
    private double ratingValue;

    /**
     * Number of users who has rated for the application release.
     */
    private int noOfUsers;

    /**
     * Represent the rating variety for the application release
     */
    private TreeMap<Integer, Integer> ratingVariety;

    public double getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(double ratingValue) {
        this.ratingValue = ratingValue;
    }

    public int getNoOfUsers() {
        return noOfUsers;
    }

    public void setNoOfUsers(int noOfUsers) {
        this.noOfUsers = noOfUsers;
    }

    public TreeMap<Integer, Integer> getRatingVariety() {
        return ratingVariety;
    }

    public void setRatingVariety(TreeMap<Integer, Integer> ratingVariety) {
        this.ratingVariety = ratingVariety;
    }
}
