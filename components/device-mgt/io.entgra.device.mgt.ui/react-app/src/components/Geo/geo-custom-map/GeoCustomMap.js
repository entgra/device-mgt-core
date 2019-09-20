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
    Polyline, Popup, CircleMarker, Tooltip
} from "react-leaflet";
import {withConfigContext} from "../../../context/ConfigContext";

class GeoCustomMap extends Component {

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
     * @param currentLocation - current location object
     * @returns content
     */
    currentLocationMarker = ({currentLocation}) => {
        const initMarker = ref => {
            if (ref) {
                ref.leafletElement.openPopup()
            }
        };
        const content = currentLocation.map((marker, index) =>
            <Marker ref={initMarker} key={index} position={[marker.latitude, marker.longitude]}>
                <Popup>
                    {marker.deviceName}<br/>
                    {marker.deviceIdentifier}<br/>
                    Speed :{marker.speed}<br/>
                    Last seen: {}
                </Popup>
            </Marker>);
        return <div style={{display: "none"}}>{content}</div>;

    };

    /**
     * Pop up circle marker for initial location
     * @param locationData - location data object
     * @returns content
     */
    startingLocation = ({locationData}) => {

        const startingPoint = [locationData.locationData[0].latitude, locationData.locationData[0].longitude];
        const content =
            <CircleMarker center={startingPoint} color="red" radius={2}>
                <Tooltip>Starting Location</Tooltip>
            </CircleMarker>;

        return <div style={{display: "none"}}>{content}</div>;
    };

    /**
     * Polyline draw for historical locations
     * @param locationData - location data object
     * @returns content
     */
    polylineMarker = ({locationData}) => {

        const polyMarkers = locationData.locationData
        //filter location points that has less than 5m distance
            .filter(locationPoint => locationPoint.distance > 5)
            .map(locationPoint => {
                return [locationPoint.latitude, locationPoint.longitude]
            });

        const content =
            <Polyline color="green" positions={polyMarkers}>
                <Popup>on the way</Popup>
            </Polyline>;

        return <div style={{display: "none"}}>{content}</div>;
    };

    /**
     * Renders the map with markers
     */
    renderMap = () => {
        const center = [this.state.lat, this.state.lng];
        const locationData = this.props.locationData;
        const currentLocation = this.props.currentLocation;
        const config = this.props.context;
        const url = config.geoMap.url;
        const attribution = config.geoMap.attribution;
        return (
            <Map center={center} zoom={this.state.zoom}>
                <TileLayer
                    url={url}
                    attribution={attribution}
                />
                {this.currentLocationMarker({currentLocation})}
                {locationData &&
                <Fragment>
                    {this.polylineMarker({locationData})}
                    {this.startingLocation({locationData})}
                </Fragment>
                }
            </Map>
        )
    };

    render() {
        return (
            <div>
                {this.renderMap()}
            </div>
        );
    }
}

export default withConfigContext(GeoCustomMap);
