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
import { Breadcrumb, Icon, PageHeader } from 'antd';
import { Link } from 'react-router-dom';
import DevicesTable from '../../components/DevicesTable';

class UngroupedDevices extends React.Component {
  render() {
    const config = this.props.context;
    const apiURL =
      window.location.origin +
      config.serverConfig.invoker.uri +
      config.serverConfig.invoker.deviceMgt +
      '/reports/android/ungrouped-device?groupName=BYOD&groupName=COPE&';

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
            <Breadcrumb.Item>Group Unassigned devices</Breadcrumb.Item>
          </Breadcrumb>
        </PageHeader>
        <div id="table" style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
          <DevicesTable apiUrl={apiURL} />
        </div>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}

export default withConfigContext(UngroupedDevices);
