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
import { PageHeader, Breadcrumb, Icon, Radio } from 'antd';

import { Link } from 'react-router-dom';
import { withConfigContext } from '../../../context/ConfigContext';
import PolicyDevicesTable from '../Widgets/PolicyDevicesTable';
import moment from 'moment';
import DateRangePicker from '../DateRangePicker';

// eslint-disable-next-line no-unused-vars
let config = null;

class PolicyReport extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    config = this.props.context;
    this.state = {
      isCompliant: true,
      // This object contains parameters which pass into API endpoint
      policyReportData: {
        from: moment()
          .subtract(6, 'days')
          .format('YYYY-MM-DD'),
        to: moment()
          .add(1, 'days')
          .format('YYYY-MM-DD'),
      },
    };
  }

  handleModeChange = e => {
    const isCompliant = e.target.value;
    this.setState({ isCompliant });
  };

  // Get modified value from datepicker and set it to paramsObject
  updateDurationValue = (modifiedFromDate, modifiedToDate) => {
    let tempParamObj = this.state.policyReportData;
    tempParamObj.from = modifiedFromDate;
    tempParamObj.to = modifiedToDate;
    this.setState({ policyReportData: tempParamObj });
  };

  render() {
    const { isCompliant } = this.state;
    const policyData = { ...this.state.policyReportData };
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
              style={{ marginBottom: 8, marginRight: 10 }}
            >
              <Radio.Button value={true}>Policy Compliant Devices</Radio.Button>
              <Radio.Button value={false}>
                Policy Non-Compliant Devices
              </Radio.Button>
            </Radio.Group>

            <DateRangePicker updateDurationValue={this.updateDurationValue} />

            <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
              <PolicyDevicesTable
                policyReportData={policyData}
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

export default withConfigContext(PolicyReport);
