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
import { Icon, Table, Tooltip } from 'antd';
import moment from 'moment';
import TimeAgo from 'javascript-time-ago';
import en from 'javascript-time-ago/locale/en';

import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { REPORTING_HOST } from '../../../../../../../../services/utils/constants';
import { handleApiError } from '../../../../../../../../services/utils/errorHandler';

let config = null;
let api, startTime, endTime;

const columnsSimChanged = [
  {
    title: 'Device',
    dataIndex: 'deviceName',
    width: 200,
  },
  {
    title: 'Type',
    dataIndex: 'deviceType',
    key: 'type',
    render: type => {
      const defaultPlatformIcons = config.defaultPlatformIcons;
      let icon = defaultPlatformIcons.default.icon;
      let color = defaultPlatformIcons.default.color;
      let theme = defaultPlatformIcons.default.theme;

      if (defaultPlatformIcons.hasOwnProperty(type)) {
        icon = defaultPlatformIcons[type].icon;
        color = defaultPlatformIcons[type].color;
        theme = defaultPlatformIcons[type].theme;
      }

      return (
        <span style={{ fontSize: 20, color: color, textAlign: 'center' }}>
          <Icon type={icon} theme={theme} />
        </span>
      );
    },
  },
  {
    title: 'Sim Changed Date',
    dataIndex: 'dateOfChange',
    key: 'dateOfChange',
    render: data => {
      if (data) {
        return (
          <Tooltip title={new Date(data).toString()}>
            {moment(data).fromNow()}
          </Tooltip>
        );
      }
      return 'Not available';
    },
  },
  {
    title: 'New Sim IMSI',
    dataIndex: 'newSimImsi',
    key: 'IMSI',
  },
  {
    title: 'New Sim Number',
    dataIndex: 'newSimNumber',
    key: 'number',
  },
];

class SimChangedTable extends React.Component {
  constructor(props) {
    super(props);
    config = this.props.context;
    TimeAgo.addLocale(en);
    this.state = {
      data: [],
      pagination: {},
      loading: false,
      selectedRows: [],
      paramsObj: {},
      deviceId: null,
      devicesListAll: null,
    };
  }

  componentDidMount() {
    api = this.props.api;
    startTime = this.props.dateFilters.from;
    endTime = this.props.dateFilters.to;
    switch (api) {
      case 'all':
        if (startTime != null && endTime != null) {
          this.fetchData();
        }
        break;
      case 'devices':
        if (
          startTime != null &&
          endTime != null &&
          this.props.deviceId != null
        ) {
          this.fetchData();
        }
        break;
      default:
        break;
    }
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    if (
      prevProps.dateFilters !== this.props.dateFilters ||
      prevProps.api !== this.props.api
    ) {
      api = this.props.api;
      startTime = this.props.dateFilters.from;
      endTime = this.props.dateFilters.to;
      switch (api) {
        case 'all':
          if (startTime != null && endTime != null) {
            this.fetchData();
          }
          break;
        case 'devices':
          if (
            startTime != null &&
            endTime != null &&
            this.props.deviceId != null
          ) {
            this.fetchData();
          }
          break;
        default:
          break;
      }
      this.setState({
        devicesListAll: this.props.devicesListAll,
      });
    }
  }

  handleTableChange = (pagination, filters, sorter) => {
    const pager = { ...this.state.pagination };
    pager.current = pagination.current;
    this.setState({
      pagination: pager,
    });
    this.fetchData({
      results: pagination.pageSize,
      page: pagination.current,
      sortField: sorter.field,
      sortOrder: sorter.order,
      ...filters,
    });
  };

  fetchData = () => {
    api = this.props.api;
    startTime = this.props.dateFilters.from;
    endTime = this.props.dateFilters.to;
    let url = REPORTING_HOST + '/sim-changed/' + api;

    if (api === 'devices') {
      let deviceId = this.props.deviceId;
      if (deviceId != null) {
        url += '/' + this.props.deviceId;
      }
    }

    if (startTime != null && endTime != null) {
      let config = {
        headers: {
          tenantId: '0',
          'Access-Control-Allow-Origin': '*',
        },
        params: {
          startTime: startTime,
          endTime: endTime,
          count: 25,
          offset: 0,
        },
      };

      axios
        .get(url, config)
        .then(res => {
          if (res.status === 200) {
            console.log(res.statusCode);
            const pagination = { ...this.state.pagination };
            this.setState({
              loading: false,
              data: res.data.content,
              pagination,
            });
          }          
        })
        .catch(error => {
          console.log(error.response.data.message);
          handleApiError(error, error.response.data.message);
          this.setState({ loading: false });
        });
    }
  };

  render() {
    let { data, pagination, loading, devicesListAll } = this.state;
    let dataAll = [];
    let deviceName;
    let deviceType;

    /**
     * This map is used to merge the device names with deviceData from device
     * by using deviceIdentifier as the key
     *
     * @var deviceData: contains device info from report-gen api (doesnt have device name)
     * @var device: contains device info from device-mgt api (contains device name)
     * **/
    data.map(deviceData => {
      devicesListAll.map(device => {
        if (device.deviceIdentifier === deviceData.deviceId) {
          deviceName = device.name;
          deviceType = device.type;
        }
      });
      dataAll.push({
        deviceName: deviceName,
        deviceType: deviceType,
        dateOfChange: deviceData.dateOfChange,
        newSimImsi: deviceData.newSimImsi,
        newSimNumber: deviceData.newSimNumber,
      });
    });

    return (
      <div>
        <Table
          columns={columnsSimChanged}
          rowKey={record => record.id}
          dataSource={dataAll}
          pagination={{
            ...pagination,
            size: 'small',
            total: data.count,
            showTotal: (total, range) =>
              `showing ${range[0]}-${range[1]} of ${total} devices`,
            showQuickJumper: true,
          }}
          loading={loading}
          onChange={this.handleTableChange}
          rowSelection={this.rowSelection}
        />
      </div>
    );
  }
}

export default withConfigContext(SimChangedTable);