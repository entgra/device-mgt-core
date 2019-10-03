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
    Polyline, Popup, Tooltip
} from "react-leaflet";
import {withConfigContext} from "../../../context/ConfigContext";

class GeoCustomMap extends Component {

    constructor(props) {
        super(props);
    }

    // /**
    //  * Pop up marker for the device's current location
    //  * @param currentLocation - current location object
    //  * @returns content
    //  */
    // currentLocationMarker = ({currentLocation}) => {
    //     const initMarker = ref => {
    //         if (ref) {
    //             ref.leafletElement.openPopup()
    //         }
    //     };
    //     const content = currentLocation.map((marker, index) =>
    //                                                 <Marker ref={initMarker} key={index}
    //                                                         position={[marker.latitude, marker.longitude]}>
    //                                                     <Popup>
    //                                                         {marker.deviceName}<br/>
    //                                                         {marker.deviceIdentifier}<br/>
    //                                                         Speed :{marker.speed}<br/>
    //                                                         Last seen: {}
    //                                                     </Popup>
    //                                                 </Marker>);
    //     return <div style={{display: "none"}}>{content}</div>;
    //
    // };

    /**
     * Polyline draw for historical locations
     * @param locationData - location data object
     * @returns content
     */
    polylineMarker = (locationData) => {

        const polyMarkers = locationData
            .map(locationPoint => {
                return [locationPoint.latitude, locationPoint.longitude]
            });

        return (
            <div style={{display: "none"}}>{
                <Polyline color="green" positions={polyMarkers}>
                    <Popup>on the way</Popup>
                </Polyline>
            }</div>
        );
    };

    // /**
    //  * Renders the map with markers
    //  */
    // renderMap = () => {
    //     const locationData = this.props.locationData;
    //     let startingLocation = this.startingLocation({locationData});
    //     // const currentLocation = this.props.currentLocation;
    //
    //     return <div style={{display: "none"}}>{startingLocation}</div>;
    // };

    render() {
        const locationData = this.props.locationData;
        const config = this.props.context;
        const attribution = config.geoMap.attribution;
        const url = config.geoMap.url;
        const startingPoint = [locationData[0].latitude, locationData[0].longitude];
        const zoom = config.geoMap.defaultZoomLevel;
        return (
            <div style={{backgroundColor: "#ffffff", borderRadius: 5, padding: 5}}>
                <Map center={startingPoint} zoom={zoom}>
                    <TileLayer
                        url={url}
                        attribution={attribution}
                    />
                    <Fragment>
                        {this.polylineMarker(locationData)}
                        <Marker position={startingPoint}>
                            <Tooltip>Starting Location</Tooltip>
                        </Marker>
                    </Fragment>
                </Map>
            </div>
        );
    }
}

export default withConfigContext(GeoCustomMap);
