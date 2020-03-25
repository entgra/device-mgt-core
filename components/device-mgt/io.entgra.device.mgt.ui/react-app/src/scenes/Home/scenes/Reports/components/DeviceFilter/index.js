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
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF Any
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';

import { Select, message, notification } from 'antd';
import axios from 'axios';

import { withConfigContext } from '../../../../../../components/ConfigContext';
import { handleApiError } from '../../../../../../services/utils/errorHandler';

const { Option } = Select;
let deviceFilters;

/** This is a common component which can filter devices
 *  based on device status and device types.
 *  The component should have a prop name 'onDeviceFilterChange' in order
 *  to get the device list from the state 'filteredDevices'.
 *   **/
class DeviceFilter extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      devices: null,
      filteredDevices: null,
      deviceFilters: {
        deviceStatus: 'any',
        deviceType: 'any',
        deviceId: 'all',
      },
    };
  }

  componentDidMount() {
    this.getDeviceTypes();
    this.getDevices();
  }

  handleDeviceFilterChange() {
    this.props.onDeviceFilterChange(this.state.deviceFilters);
  }

  handleDeviceStatusChange = status => {
    deviceFilters = this.state.deviceFilters;
    deviceFilters.deviceStatus = status;
    this.setState(
      {
        deviceFilters: deviceFilters,
      },
      () => this.handleDeviceFilterChange(),
    );
    this.updateDevices();
  };

  handleDeviceChange = deviceId => {
    deviceFilters = this.state.deviceFilters;
    deviceFilters.deviceId = deviceId;
    this.setState(
      {
        deviceFilters: deviceFilters,
      },
      () => this.handleDeviceFilterChange(),
    );
  };

  getDeviceTypes() {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/device-types',
      )
      .then(res => {
        if (res.status === 200) {
          const deviceTypes = JSON.parse(res.data.data);
          this.setState({
            deviceTypes: deviceTypes,
          });
        }
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        }
        if (error.hasOwnProperty('response') && error.response.status == 404) {
          handleApiError(error, error.response.data.message);
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description:
              'Error occurred while trying to load non compliance feature list.',
          });
        }
      });
  }

  getDevices() {
    const config = this.props.context;
    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/devices',
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({
            devices: res.data.data.devices,
            filteredDevices: res.data.data.devices,
          });
          this.handleDevices(res.data.data.devices);
        }
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        }
        if (error.hasOwnProperty('response') && error.response.status == 404) {
          handleApiError(error, error.response.data.message);
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description:
              'Error occurred while trying to load non compliance feature list.',
          });
        }
      });
  }

  handleDevices = devices => {
    this.props.devices(devices);
  };

  updateDevices() {
    let devices = this.state.devices;
    let filteredDevices = [];
    const { deviceStatus, deviceType } = { ...this.state.deviceFilters };
    devices.forEach(device => {
      if (deviceStatus == 'any' && deviceType == 'any') {
        filteredDevices.push(device);
      } else if (deviceStatus != 'any' && deviceType == 'any') {
        if (device.enrolmentInfo.status == deviceStatus) {
          filteredDevices.push(device);
        }
      } else if (deviceType != 'any' && deviceStatus == 'any') {
        if (device.type == deviceType) {
          filteredDevices.push(device);
        }
      } else if (
        device.enrolmentInfo.status == deviceStatus &&
        device.type == deviceType
      ) {
        filteredDevices.push(device);
      }
    });
    this.setState({ filteredDevices: filteredDevices });
  }

  onDeviceTypeChange = type => {
    deviceFilters = this.state.deviceFilters;
    deviceFilters.deviceType = type;
    this.setState(
      {
        deviceFilters: deviceFilters,
      },
      () => this.handleDeviceFilterChange(),
    );
    this.updateDevices();
  };

  render() {
    let deviceTypes = this.state.deviceTypes;
    let devicesTypeOptions;
    if (deviceTypes != null) {
      devicesTypeOptions = deviceTypes.map(deviceType => (
        <Option key={deviceType.id} value={deviceType.name}>
          {deviceType.name.charAt(0).toUpperCase() + deviceType.name.slice(1)}
        </Option>
      ));
    }

    let devices = this.state.filteredDevices;
    let devicesList;
    if (devices !== null) {
      devicesList = devices.map(device => (
        <Option key={device.deviceIdentifier} value={device.deviceIdentifier}>
          {device.name}
        </Option>
      ));
      devicesList.unshift(
        <Option key="all" value="all" style={{ textTransform: 'capitalize' }}>
          All
        </Option>,
      );
    } else {
      <Option value="No Devices">No Devices</Option>;
    }

    return (
      <div style={{ display: 'inline-block' }}>
        <Select
          showSearch
          style={{ width: 200, marginRight: 5, textTransform: 'capitalize' }}
          placeholder="Select device status"
          initialValue="any"
          optionFilterProp="children"
          onChange={this.handleDeviceStatusChange}
          filterOption={(input, option) =>
            option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
        >
          <Option key="any" value="any">
            Any
          </Option>
          <Option key="ACTIVE" value="ACTIVE">
            Active
          </Option>
          <Option key="INACTIVE" value="INACTIVE">
            Inactive
          </Option>
          <Option key="REMOVED" value="REMOVED">
            Removed
          </Option>
        </Select>

        <Select
          showSearch
          style={{ width: 200, marginRight: 5 }}
          placeholder="Select device type"
          initialValue="any"
          optionFilterProp="children"
          onChange={this.onDeviceTypeChange}
          filterOption={(input, option) =>
            option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
          }
          deviceTypes={this.state.deviceTypes}
        >
          <Option value="any">Any</Option>
          {devicesTypeOptions}
        </Select>

        <Select
          showSearch
          style={{ width: 200, marginRight: 5 }}
          placeholder="Select a device"
          initialValue="all"
          optionFilterProp="children"
          onChange={this.handleDeviceChange}
        >
          {devicesList}
        </Select>
      </div>
    );
  }
}

export default withConfigContext(DeviceFilter);
