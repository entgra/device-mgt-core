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
import React from "react";
import moment from "moment";
import DateTimeRangeContainer from "react-advanced-datetimerange-picker";
import {Button, Select, message, notification, Tag} from "antd";
import axios from "axios";
import {withConfigContext} from "../../../context/ConfigContext";
import GeoCustomMap from "../geo-custom-map/GeoCustomMap";
import "./GeoDashboard.css";

import currentLocationData from "../mockCurrentLocation.json";

class GeoDashboard extends React.Component {

    constructor(props) {
        super(props);
        let start = moment(new Date());
        let end = moment(start)
            .add(5, "days")
            .subtract(1, "minute");
        this.state = {
            deviceData: [],
            selectedDevice: '',
            locationData: [],
            currentLocation: [],
            loading: false,
            start: start,
            end: end,
        };
    }

    /**
     * Call back on apply button in the date time picker
     * @param startDate
     * @param endDate
     */
    applyCallback = (startDate, endDate) => {
        console.log("Apply Callback");
        // console.log(startDate.format("DD-MM-YYYY HH:mm"));
        this.setState({
            start: startDate,
            end: endDate
        });
    };

    /**
     * Api call handle on fetch location date button
     */
    handleApiCall = () => {

        if (this.state.selectedDevice && this.state.start && this.state.end) {
            const toInMills = moment(this.state.end);
            console.log("To time: " + toInMills);
            const fromInMills = moment(this.state.start);
            console.log("From time: " + fromInMills);
            const deviceType = this.state.selectedDevice.type;
            const deviceId = this.state.selectedDevice.deviceIdentifier;
            const config = this.props.context;
            this.setState({loading: true});

            axios.get(window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt
                + "/devices/" + deviceType + "/" + deviceId + "/location-history?" + "from=" + fromInMills + "&to=" +
                toInMills,).then(res => {
                if (res.status === 200) {
                    const locationData = JSON.parse(res.data.data);
                    this.setState({
                        loading: false,
                        locationData,
                    });
                }
            }).catch((error) => {
                if (error.hasOwnProperty("response") && error.response.status === 401) {
                    message.error('You are not logged in');
                    window.location.href = window.location.origin + '/entgra/login';
                } else {
                    notification["error"]({
                        message: "There was a problem",
                        duration: 0,
                        description:
                            "Error occurred while trying to fetch locations......",
                    });
                }

                this.setState({loading: false});
                console.log(error);
            });
        } else {
            notification["error"]({
                message: "There was a problem",
                duration: 0,
                description:
                    "Please provide a date range and a device.",
            });
        }
    };

    /**
     * Device dropdown list handler
     */
    handleDeviceList = (e) => {
        let selectedDevice = this.state.deviceData[e];
        this.setState({selectedDevice})
    };

    componentDidMount() {
        this.fetchDevices();
        this.fetchCurrentLocation();
    }

    /**
     * fetches current location to create a marker
     */
    fetchCurrentLocation = () => {
        this.setState({currentLocation: currentLocationData});
    };


    /**
     * fetches device data to populate the dropdown list
     */
    fetchDevices = () => {
        const config = this.props.context;
        this.setState({loading: true});

        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt +
            "/devices",
        ).then(res => {
            if (res.status === 200) {
                this.setState({
                    loading: false,
                    deviceData: res.data.data.devices,
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = window.location.origin + '/entgra/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load devices.",
                });
            }

            this.setState({loading: false});
        });
    };

    /**
     * Geo Dashboard menu
     * @param ranges Date Range
     * @param local
     * @param maxDate
     */
    geoNavBar = (ranges, local, maxDate) => {
        let {deviceData} = this.state;
        let value =
            `
            ${this.state.start.format("DD-MM-YYYY HH:mm")} - ${this.state.end.format("DD-MM-YYYY HH:mm")}
            `;

        return (
            <div className="controllerDiv">

                <Button style={{marginRight: 20}}>
                    <DateTimeRangeContainer
                        ranges={ranges}
                        start={this.state.start}
                        end={this.state.end}
                        local={local}
                        maxDate={maxDate}
                        applyCallback={this.applyCallback}
                    >
                        {value}
                    </DateTimeRangeContainer>
                </Button>

                <Select
                    showSearch
                    style={{width: 220, marginRight: 20}}
                    placeholder="Select a Device"
                    optionFilterProp="children"
                    onChange={this.handleDeviceList}
                    filterOption={(input, option) =>
                        option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                    }
                >
                    {deviceData.map((device, index) =>
                        <Select.Option key={index} value={index}>
                            {device.name + " "}<Tag>{device.enrolmentInfo.status.toUpperCase()}</Tag>
                        </Select.Option>)}
                </Select>

                <Button onClick={this.handleApiCall}>Fetch Locations</Button>

            </div>
        );
    };

    render() {
        let {locationData} = this.state;
        let {currentLocation} = this.state;
        let now = new Date();
        let start = moment(
            new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0)
        );
        let end = moment(start)
            .add(1, "days")
            .subtract(1, "seconds");
        let ranges = {
            "Today Only": [moment(start), moment(end)],
            "Yesterday Only": [
                moment(start).subtract(1, "days"),
                moment(end).subtract(1, "days")
            ],
            "3 Days": [moment(start).subtract(3, "days"), moment(end)],
            "5 Days": [moment(start).subtract(5, "days"), moment(end)],
            "1 Week": [moment(start).subtract(7, "days"), moment(end)],
            "2 Weeks": [moment(start).subtract(14, "days"), moment(end)],
            "1 Month": [moment(start).subtract(1, "months"), moment(end)],
        };
        let local = {
            format: "DD-MM-YYYY HH:mm",
            sundayFirst: false
        };
        let maxDate = moment(start).add(24, "hour");

        return (
            <div className="container">
                {this.geoNavBar(ranges, local, maxDate)}
                {locationData.length > 0 ?
                    <GeoCustomMap locationData={{locationData}} currentLocation={currentLocation}/>
                    :
                    <GeoCustomMap currentLocation={currentLocation}/>
                }
            </div>
        );
    }
}

export default withConfigContext(GeoDashboard);
