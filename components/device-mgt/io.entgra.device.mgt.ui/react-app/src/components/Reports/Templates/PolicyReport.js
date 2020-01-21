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

import React from 'react';
import {
  PageHeader,
  Typography,
  Breadcrumb,
  Icon,
  Tag,
  Radio,
  Select,
  Button,
  Card,
  Row,
  Col,
  message,
  notification,
  Empty,
} from 'antd';

import { Link } from 'react-router-dom';
import { withConfigContext } from '../../../context/ConfigContext';
import PolicyDevicesTable from '../Widgets/PolicyDevicesTable';
const { Paragraph } = Typography;
const { CheckableTag } = Tag;

const { Option } = Select;
let config = null;

class PolicyReport extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    config = this.props.context;
    const { reportData } = this.props.location;
    this.state = {
      isCompliant: true,
      // This object contains parameters which pass into API endpoint
      policyReportData: {
        policy: reportData.data ? reportData.data.policyId : null,
        from: reportData ? reportData.duration[0] : null,
        to: reportData ? reportData.duration[1] : null,
      },
    };
  }

  handleModeChange = e => {
    const isCompliant = e.target.value;
    this.setState({ isCompliant });
  };

  render() {
    const { isCompliant, policyReportData } = this.state;

    if (policyReportData.from && policyReportData.to) {
      return (
        <div>
          <PageHeader style={{ paddingTop: 0 }}>
            <Breadcrumb style={{ paddingBottom: 16 }}>
              <Breadcrumb.Item>
                <Link to="/entgra">
                  <Icon type="home" /> Home
                </Link>
              </Breadcrumb.Item>
              <Breadcrumb.Item>Report</Breadcrumb.Item>
            </Breadcrumb>
            <div className="wrap" style={{ marginBottom: '10px' }}>
              <h3>Policy Report</h3>

              <Radio.Group
                onChange={this.handleModeChange}
                defaultValue={true}
                value={isCompliant}
                style={{ marginBottom: 8 }}
              >
                <Radio.Button value={true}>
                  Policy Compliant Devices
                </Radio.Button>
                <Radio.Button value={false}>
                  Policy Non-Compliant Devices
                </Radio.Button>
              </Radio.Group>

              <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
                <PolicyDevicesTable
                  policyReportData={policyReportData}
                  isCompliant={isCompliant}
                />
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
}

export default withConfigContext(PolicyReport);
