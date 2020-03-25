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

import { withConfigContext } from '../../../../../../components/ConfigContext';
import { Breadcrumb, Icon, PageHeader, DatePicker } from 'antd';
import { Link } from 'react-router-dom';

import SimChangedTable from './components/SimChangedTable';
import DeviceFilter from '../../components/DeviceFilter';

const { RangePicker } = DatePicker;

class SimChanged extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      reportType: 'devices',
      deviceId: null,
      deviceDataComplete: null,
      deviceFilters: {
        deviceStatus: null,
        deviceType: null,
        deviceId: null,
      },
      dateFilters: {
        from: null,
        to: null,
      },
    };
  }

  onDeviceFilterChange = deviceFilters => {
    this.setState({
      deviceFilters: {
        deviceStatus: deviceFilters.deviceStatus,
        deviceType: deviceFilters.deviceType,
        deviceId: deviceFilters.deviceId,
      },
    });
  };

  handleRangePickerChange = (value, dateString) => {
    let dateFilters = this.state.dateFilters;
    dateFilters.from = new Date(dateString[0]).getTime();
    dateFilters.to = new Date(dateString[1]).getTime();
    this.setState(dateFilters);
  };

  handleDevices = e => {
    this.setState({
      deviceDataComplete: e,
    });
  };

  render() {
    return (
      <div>
        <PageHeader style={{ paddingTop: 0 }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/entgra">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>
              <Link to="/entgra/reports">Reports</Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>Sim Changed Report</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <DeviceFilter
              devices={this.handleDevices}
              onDeviceFilterChange={this.onDeviceFilterChange}
            />
            <RangePicker
              format="YYYY/MM/DD"
              onChange={this.handleRangePickerChange}
              style={{ marginBottom: 8, marginRight: 5 }}
            />
          </div>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
              <SimChangedTable
                dateFilters={this.state.dateFilters}
                deviceDataComplete={this.state.deviceDataComplete}
                deviceFilters={this.state.deviceFilters}
              />
            </div>
          </div>
        </PageHeader>
      </div>
    );
  }
}

export default withConfigContext(SimChanged);
