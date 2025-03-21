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

package io.entgra.device.mgt.core.device.mgt.common.geo.service;

import io.entgra.device.mgt.core.device.mgt.common.event.config.EventMetaData;

public class GeoFenceEventMeta implements EventMetaData {
    private int id;
    private String fenceName;
    private String description;
    private double latitude;
    private double longitude;
    private float radius;
    private String geoJson;
    private String fenceShape;

    public GeoFenceEventMeta() {}

    public GeoFenceEventMeta(GeofenceData geofenceData) {
        this.id = geofenceData.getId();
        this.fenceName = geofenceData.getFenceName();
        this.description = geofenceData.getDescription();
        this.latitude = geofenceData.getLatitude();
        this.longitude = geofenceData.getLongitude();
        this.radius = geofenceData.getRadius();
        this.geoJson = geofenceData.getGeoJson();
        this.fenceShape = geofenceData.getFenceShape();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFenceName() {
        return fenceName;
    }

    public void setFenceName(String fenceName) {
        this.fenceName = fenceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getGeoJson() {
        return geoJson;
    }

    public void setGeoJson(String geoJson) {
        this.geoJson = geoJson;
    }

    public String getFenceShape() {
        return fenceShape;
    }

    public void setFenceShape(String fenceShape) {
        this.fenceShape = fenceShape;
    }
}
