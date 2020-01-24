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
import { PageHeader, Breadcrumb, Icon, message, notification } from 'antd';

import { Link } from 'react-router-dom';
import { withConfigContext } from '../../../context/ConfigContext';
import axios from 'axios';
import DateRangePicker from '../DateRangePicker';
import moment from 'moment';
import { Chart, Geom, Axis, Tooltip, Legend } from 'bizcharts';
import DataSet from '@antv/data-set';

// eslint-disable-next-line no-unused-vars
let config = null;

class EnrollmentsVsUnenrollmentsReport extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    config = this.props.context;
    this.state = {
      paramsObject: {
        from: moment()
          .subtract(6, 'days')
          .format('YYYY-MM-DD'),
        to: moment()
          .add(1, 'days')
          .format('YYYY-MM-DD'),
      },
      data: [],
      fields: [],
    };
  }

  componentDidMount() {
    this.fetchData();
  }

  // Get modified value from datepicker and set it to paramsObject
  updateDurationValue = (modifiedFromDate, modifiedToDate) => {
    let tempParamObj = this.state.paramsObject;
    tempParamObj.from = modifiedFromDate;
    tempParamObj.to = modifiedToDate;
    this.setState({ paramsObject: tempParamObj });
    this.fetchData();
  };

  // Call count APIs and get count for given parameters, then create data object to build pie chart
  fetchData = () => {
    this.setState({ loading: true });

    const { paramsObject } = this.state;

    const encodedExtraParams = Object.keys(paramsObject)
      .map(key => key + '=' + paramsObject[key])
      .join('&');

    axios
      .all([
        axios.get(
          window.location.origin +
            config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt +
            '/reports/count?status=ACTIVE&status=INACTIVE&' +
            encodedExtraParams,
          'Enrollments',
        ),
        axios.get(
          window.location.origin +
            config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt +
            '/reports/count?status=REMOVED&' +
            encodedExtraParams,
          'Unenrollments',
        ),
      ])
      .then(res => {
        let keys = Object.keys(res[0].data.data);
        let enrollments = res[0].data.data;
        let unenrollments = res[1].data.data;

        enrollments.name = 'Enrollments';
        unenrollments.name = 'Unenrollments';

        const finalData = [enrollments, unenrollments];

        this.setState({
          data: finalData,
          fields: keys,
        });
      })
      .catch(error => {
        if (error.hasOwnProperty('response') && error.response.status === 401) {
          // todo display a popup with error
          message.error('You are not logged in');
          window.location.href = window.location.origin + '/entgra/login';
        } else {
          notification.error({
            message: 'There was a problem',
            duration: 0,
            description: 'Error occurred while trying to get device count.',
          });
        }
      });
  };

  render() {
    const ds = new DataSet();
    const dv = ds.createView().source(this.state.data);
    dv.transform({
      type: 'fold',
      fields: this.state.fields,
      key: 'Time',
      value: 'Number of Devices',
    });

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
            <h3>Enrollments vs Unenrollments Report</h3>
            <DateRangePicker updateDurationValue={this.updateDurationValue} />

            <div
              style={{
                backgroundColor: '#ffffff',
                borderRadius: 5,
                marginTop: 10,
              }}
            >
              <Chart height={400} data={dv} forceFit>
                <Axis name="Time" />
                <Axis name="Number of Devices" />
                <Legend />
                <Tooltip
                  crosshairs={{
                    type: 'y',
                  }}
                />
                <Geom
                  type="interval"
                  position="Time*Number of Devices"
                  color={'name'}
                  adjust={[
                    {
                      type: 'dodge',
                      marginRatio: 1 / 32,
                    },
                  ]}
                />
              </Chart>
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

export default withConfigContext(EnrollmentsVsUnenrollmentsReport);
