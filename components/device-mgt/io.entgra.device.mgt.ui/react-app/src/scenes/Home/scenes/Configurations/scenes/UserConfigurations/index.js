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
import {
  Drawer,
  message,
  notification,
  Card,
  Button,
  PageHeader,
  Breadcrumb,
  Icon,
  Radio,
} from 'antd';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import axios from 'axios';
import { Link } from 'react-router-dom';
import NotificationsTable from '../../../Notifications/Components/NotificationsTable';

// eslint-disable-next-line no-unused-vars
let config = null;

class UserConfigurations extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    config = this.props.context;
    this.state = {};
  }

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
            <Breadcrumb.Item>Configurations</Breadcrumb.Item>
            <Breadcrumb.Item>User Configurations</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <h3>USER CONFIGURATIONS</h3>

            <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}></div>
          </div>
        </PageHeader>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}

export default withConfigContext(UserConfigurations);
