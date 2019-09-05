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
import PropTypes from "prop-types";
import React, {Component} from "react";
import {
    Map,
    TileLayer,
    Marker,
    Popup
} from "react-leaflet";

const MyPopupMarker = ({children, position}) => (
    <Marker position={position}>
        <Popup>
            <span>{children}</span>
        </Popup>
    </Marker>
);

const MyMarkersList = ({markers: locationData}) => {
    const items = locationData.map(({key, ...props}) => (
        <MyPopupMarker key={key} {...props} />
    ));
    return <div style={{display: "none"}}>{items}</div>;
};

MyMarkersList.propTypes = {
    markers: PropTypes.array.isRequired
};

export default class GeoCustomMap extends Component {

    constructor(props) {
        super(props);
        // const { locationData } = this.props;
        this.state = {
            lat: 6.927079,
            lng: 79.861244,
            zoom: 14,
            //
            // markers: [
            //     {
            //         key: "marker1",
            //         position: [6.927079, 79.861244],
            //         children: "Colombo"
            //     }
            // ]
        };
    }


    render() {

        const center = [this.state.lat, this.state.lng];
        const locationData = this.props.locationData;
        console.log(locationData);

        if(locationData.length) {
            const markers = locationData.map(locationPoint => {
                return {
                    key: lcoationPoint.deviceId
                }
            });

            console.log(markers);
        }
        return (
            <div>
                <Map center={center} zoom={this.state.zoom}>
                    <TileLayer
                        attribution="&amp;copy <a href=&quot;http://osm.org/copyright&quot;>OpenStreetMap</a> contributors"
                        url="http://{s}.tile.osm.org/{z}/{x}/{y}.png"/>

                    {/*<MyMarkersList markers={this.props.markers}/>*/}
                </Map>
            </div>
        );
    }
}
