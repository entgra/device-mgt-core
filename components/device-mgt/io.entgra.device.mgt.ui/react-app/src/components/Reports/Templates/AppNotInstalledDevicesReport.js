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
import { PageHeader, Breadcrumb, Icon, Button, Popover, Input } from 'antd';

import { Link } from 'react-router-dom';
import { withConfigContext } from '../../../context/ConfigContext';

import AppListDropDown from '../Widgets/AppListDropDown';
import ReportDevicesTable from '../Widgets/ReportDevicesTable';

// eslint-disable-next-line no-unused-vars
let config = null;
let url;
let externalAppPackageName;
let externalAppVersion;

const InputGroup = Input.Group;

class AppNotInstalledDevicesReport extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    config = this.props.context;
    this.state = {
      apiUrl: null,
      visible: false,
      isExternalApp: false,
    };
  }

  getAppList = appPackageName => {
    url =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.deviceMgt +
      '/reports/android/' +
      appPackageName +
      '/not-installed?';
  };

  onClickGenerateButton = () => {
    const { isExternalApp } = this.state;
    if (isExternalApp) {
      if (externalAppVersion) {
        url =
          window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/reports/android/' +
          externalAppPackageName +
          '/not-installed?app-version=' +
          externalAppVersion +
          '&';
      } else {
        url =
          window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.deviceMgt +
          '/reports/android/' +
          externalAppPackageName +
          '/not-installed?';
      }
    }
    this.setState({ apiUrl: url });
  };

  handlePopoverVisibleChange = visible => {
    this.setState({ visible });
  };

  onChangePackageName = e => {
    externalAppPackageName = e.currentTarget.value;
  };

  onChangeVersion = e => {
    externalAppVersion = e.currentTarget.value;
  };

  onClickSetButton = () => {
    this.setState({ isExternalApp: true, visible: false });
  };

  render() {
    const { apiUrl, isExternalApp } = this.state;
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
            <Breadcrumb.Item>App NOT Installed Devices Report</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <h3>Policy Report</h3>

            <div style={{ display: 'flex', marginBottom: '10px' }}>
              <div
                style={{ display: !isExternalApp ? 'inline-block' : 'none' }}
              >
                <AppListDropDown getAppList={this.getAppList} />
              </div>

              <div style={{ display: isExternalApp ? 'inline-block' : 'none' }}>
                <InputGroup compact>
                  <Input
                    value={externalAppPackageName}
                    style={{ width: '60%' }}
                    placeholder={'Package Name'}
                  />
                  <Input
                    value={externalAppVersion}
                    style={{ width: '40%' }}
                    placeholder={'App Version'}
                  />
                </InputGroup>
              </div>

              <Popover
                trigger="click"
                content={
                  <div style={{ display: 'flex' }}>
                    <InputGroup compact>
                      <Input
                        style={{ width: '60%' }}
                        placeholder={'Package Name'}
                        onChange={this.onChangePackageName}
                      />
                      <Input
                        style={{ width: '40%' }}
                        placeholder={'App Version'}
                        onChange={this.onChangeVersion}
                      />
                    </InputGroup>

                    <Button
                      type="primary"
                      onClick={this.onClickSetButton}
                      style={{ marginLeft: '5px' }}
                    >
                      Set
                    </Button>
                  </div>
                }
                visible={this.state.visible}
                onVisibleChange={this.handlePopoverVisibleChange}
              >
                <Button type="default" style={{ marginLeft: '10px' }}>
                  External App
                </Button>
              </Popover>

              <Button
                type="primary"
                onClick={this.onClickGenerateButton}
                style={{ marginLeft: '10px' }}
              >
                Generate Report
              </Button>
            </div>
            <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
              <ReportDevicesTable apiUrl={apiUrl} />
            </div>
          </div>
        </PageHeader>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}

export default withConfigContext(AppNotInstalledDevicesReport);
