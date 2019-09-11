/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React, {Component, Fragment} from "react";
import {
    Map,
    TileLayer,
    Marker,
    Polyline, Popup, CircleMarker, WMSTileLayer
} from "react-leaflet";

export default class GeoCustomMap extends Component {

    constructor(props) {
        super(props);
        this.state = {
            lat: 6.8702857,
            lng: 79.8774241,
            zoom: 16,
        };
    }

    /**
     * Pop up marker for the device's current location
     * @param currentLocation current location object
     * @returns content
     */
    currentLocationMarker = ({currentLocation}) => {
        const content = currentLocation.map((marker, index) =>
            <Marker key={index} position={[marker.latitude, marker.longitude]}>
                <Popup>
                <span>
                    {marker.deviceName}<br/>
                    {marker.deviceIdentifier}<br/>
                    Speed :{marker.speed}<br/>
                </span>
                </Popup>
            </Marker>);
        return <div style={{display: "none"}}>{content}</div>;

    };

    /**
     * Pop up circle marker for initial location
     * @param locationData location data object
     * @returns content
     */
    startingLocation = ({locationData}) => {

        const startingPoint = [locationData.locationData[0].latitude, locationData.locationData[0].longitude]
        const content =
            <CircleMarker center={startingPoint} color="red" radius={3}>
                <Popup>Starting Location</Popup>

            </CircleMarker>;

        return <div style={{display: "none"}}>{content}</div>;
    };

    /**
     * Polyline draw for historical locations
     * @param locationData location data object
     * @returns content
     */
    polylineMarker = ({locationData}) => {

        const polyMarkers = locationData.locationData
            //filter location points that has less than 5m distance
            .filter(locationpoint => locationpoint.distance > 5)
            .map(locationpoint => {
                return [locationpoint.latitude, locationpoint.longitude]
            });

        const content =
            <Polyline color="green" positions={polyMarkers} smoothFactor={10}>
                <Popup>on the way</Popup>
            </Polyline>;

        return <div style={{display: "none"}}>{content}</div>;
    };


    render() {
        const center = [this.state.lat, this.state.lng];
        const locationData = this.props.locationData;
        const currentLocation = this.props.currentLocation;

        return (
            <div>
                <Map center={center} zoom={this.state.zoom}>
                    <TileLayer
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        attribution="&copy; <a href=&quot;http://osm.org/copyright&quot;>OpenStreetMap</a> contributors"
                    />
                    <WMSTileLayer
                        layers={this.state.bluemarble ? 'nasa:bluemarble' : 'ne:ne'}
                        url="https://demo.boundlessgeo.com/geoserver/ows"
                    />
                    {this.currentLocationMarker({currentLocation})}
                    {locationData &&
                        <Fragment>
                            {this.polylineMarker({locationData})}
                            {this.startingLocation({locationData})}
                        </Fragment>
                    }
                </Map>
            </div>
        );
    }
}
