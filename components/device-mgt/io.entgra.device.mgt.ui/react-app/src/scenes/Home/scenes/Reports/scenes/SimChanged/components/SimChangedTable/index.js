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

/* eslint-disable react/display-name */
import React from 'react';
import axios from 'axios';

import { withConfigContext } from '../../../../../../../../components/ConfigContext';

import { Icon, Table, Tooltip, message } from 'antd';
import moment from 'moment';
import TimeAgo from 'javascript-time-ago';
import en from 'javascript-time-ago/locale/en';
import {
  handleApiError,
  handleReportingApiError,
} from '../../../../../../../../services/utils/errorHandler';

let config = null;

const columns = [
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
      isLoading: false,
      selectedRows: [],
      paramsObj: {},
      deviceId: null,
      deviceDataDeviceMgt: null,
      pageableData: {
        totalCount: null,
        pageSize: 10,
        pageNumber: 0,
      },
    };
  }

  componentDidUpdate(prevProps, prevState, snapshot) {
    const { dateFilters, deviceFilters } = this.props;
    let reportType = 'all';
    if (prevProps.deviceDataDeviceMgt != this.props.deviceDataDeviceMgt) {
      this.setState({
        deviceDataDeviceMgt: this.props.deviceDataDeviceMgt,
      });
    }
    if (prevProps != this.props) {
      switch (reportType) {
        case 'all':
          if (dateFilters.from != null && dateFilters.to != null) {
            this.fetchData();
          }
          break;
        case 'devices':
          if (
            dateFilters.from != null &&
            dateFilters.to != null &&
            deviceFilters.deviceId != null
          ) {
            this.fetchData();
          }
          break;
        default:
          break;
      }
    }
  }

  handleTableChange = (pagination, filters, sorter) => {
    const pager = { ...this.state.pagination };
    pager.current = pagination.current;
    const pageableData = this.state.pageableData;
    pageableData.pageNumber = pager.current - 1;
    this.setState({
      pagination: pager,
      pageableData: pageableData,
    });
    this.fetchData();
  };

  fetchData() {
    const { dateFilters, deviceFilters } = this.props;
    let reportType;
    if (deviceFilters.deviceId == 'all') {
      reportType = '/all';
    } else {
      reportType = '/devices';
    }
    let url =
      config.reportingServerConfig.invoker.uri +
      config.reportingServerConfig.invoker.simChanged +
      reportType;

    if (reportType == '/devices') {
      let deviceId = deviceFilters.deviceId;
      if (deviceId != null) {
        url += '/' + deviceFilters.deviceId;
      }
    }

    if (dateFilters.from != null && dateFilters.to != null) {
      let ApiConfig = {
        headers: {
          tenantId: '0',
        },
        params: {
          startTime: dateFilters.from,
          endTime: dateFilters.to,
          count: this.state.pageableData.pageSize,
          offset: this.state.pageableData.pageNumber,
        },
      };

      axios
        .get(url, ApiConfig)
        .then(res => {
          if (res.status === 200) {
            this.setState({
              isLoading: false,
              data: res.data.content,
              pageableData: {
                totalCount: res.data.totalElements,
                pageSize: res.data.pageable.pageSize,
                pageNumber: res.data.pageable.pageNumber,
              },
            });
          }
        })
        .catch(error => {
          if (!error.response) {
            handleReportingApiError(
              'Error occurred while trying to load the reports',
            );
          } else {
            if (
              error.hasOwnProperty('response') &&
              error.response.status === 401
            ) {
              message.error('You are not logged in');
              window.location.href = window.location.origin + '/entgra/login';
            }
            if (
              error.hasOwnProperty('response') &&
              error.response.status == 404
            ) {
              handleApiError(error, error.response.data.message);
            }
          }
        });
    }
  }

  render() {
    let { data, pagination, isLoading, deviceDataDeviceMgt } = this.state;
    let devicesData = [];
    let rowKey = 0;

    /**
     * This map is used to merge the device names with deviceData from device
     * by using deviceIdentifier as the key
     *
     * @var deviceDataReporting: contains device info from report-gen api (doesnt have device name)
     * @var deviceDataDeviceMgt: contains device info from device-mgt api (contains device name)
     * **/
    data.forEach(deviceDataReporting => {
      let deviceName;
      let deviceType;
      deviceDataDeviceMgt.forEach(device => {
        if (device.deviceIdentifier === deviceDataReporting.deviceId) {
          deviceName = device.name;
          deviceType = device.type;
        } else {
          deviceName = deviceDataReporting.deviceId;
          deviceType = null;
        }
      });
      devicesData.push({
        key: ++rowKey,
        deviceName: deviceName,
        deviceType: deviceType,
        dateOfChange: deviceDataReporting.dateOfChange,
        newSimImsi: deviceDataReporting.newSimImsi,
        newSimNumber: deviceDataReporting.newSimNumber,
      });
    });

    return (
      <div>
        <Table
          columns={columns}
          rowKey="key"
          dataSource={devicesData}
          pagination={{
            ...pagination,
            size: 'small',
            total: this.state.pageableData.totalCount,
            showTotal: (total, range) =>
              `showing ${range[0]}-${range[1]} of ${total} devices`,
            showQuickJumper: true,
          }}
          isLoading={isLoading}
          onChange={this.handleTableChange}
          rowSelection={this.rowSelection}
        />
      </div>
    );
  }
}

export default withConfigContext(SimChangedTable);
