/*
 * Copyright (c) 2020, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
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

import React from 'react';
import axios from 'axios';
import { Select } from 'antd';
import TimeAgo from 'javascript-time-ago';
import en from 'javascript-time-ago/locale/en';

const { Option } = Select;

class DeviceSelect extends React.Component {
  constructor(props) {
    super(props);
    // eslint-disable-next-line no-undef
    config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      devices: [],
      deviceId: null,
    };
  }

  componentDidMount() {
    this.fetchData();
  }

  onDeviceChange = e => this.props.onDeviceChange(e);

  handleDevices(devices) {
    this.props.devices(devices);
  }

  fetchData = () => {
    axios
      .get(
        window.location.origin +
          '/entgra-ui-request-handler/invoke/device-mgt/v1.0/devices',
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({
            devices: res.data.data.devices,
          });
          this.handleDevices(res.data.data.devices);
        }
      });
  };
  render() {
    let devices = this.state.devices;
    let devicesList = devices.map(device => (
      <Option key={device.deviceIdentifier} value={device.deviceIdentifier}>
        {device.name}
      </Option>
    ));
    return (
      <Select
        showSearch
        style={{ width: 200 }}
        placeholder="Select a device"
        optionFilterProp="children"
        onChange={this.onDeviceChange}
      >
        {devicesList}
      </Select>
    );
  }
}

export default DeviceSelect;
