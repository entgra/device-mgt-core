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
import {Button, Form, Row, Col, Card, Steps, message, notification} from 'antd';
import { withConfigContext } from '../../context/ConfigContext';
import SelectPlatform from './SelectPlatform';
import ConfigureProfile from './ConfigureProfile';
import axios from "axios";
const { Step } = Steps;
let apiUrl;

class AddPolicy extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      isAddDeviceModalVisible: false,
      current: 0,
      loading: false,
      policiesList:[]
    };
  }

    getPolicyConfigJson = (type) => {
        const config = this.props.context;
        this.setState({ loading: true });

        apiUrl =
            window.location.origin +
            config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt +
            '/device-types/'+ type + '/policies';

        // send request to the invokers
        axios
            .get(apiUrl)
            .then(res => {
                if (res.status === 200) {
                    const pagination = { ...this.state.pagination };
                    this.setState({
                        loading: false,
                        policiesList: JSON.parse(res.data.data),
                        current: 1,
                    });
                }
            })
            .catch(error => {
                if (error.hasOwnProperty('response') && error.response.status === 401) {
                    // todo display a popop with error
                    message.error('You are not logged in');
                    window.location.href = window.location.origin + '/entgra/login';
                } else {
                    notification.error({
                        message: 'There was a problem',
                        duration: 0,
                        description: 'Error occurred while trying to load Policy details.',
                    });
                }
                this.setState({ loading: false });
            });
    };

  next() {
    const current = this.state.current + 1;
    this.setState({ current });
  }

  prev() {
    const current = this.state.current - 1;
    this.setState({ current });
  }

  render() {
    const { current, policiesList } = this.state;
    return (
      <div>
        <Row>
          <Col span={20} offset={2}>
            <Steps style={{ minHeight: 32 }} current={current}>
              <Step key="Platform" title="Select a Platform" />
              <Step key="ProfileConfigure" title="Configure profile" />
              <Step key="PolicyType" title="Select policy type" />
              <Step key="AssignGroups" title="Assign to groups" />
              <Step key="Publish" title="Publish to devices" />
              <Step key="Result" title="Result" />
            </Steps>
          </Col>
          <Col span={16} offset={4}>
            <Card style={{ marginTop: 24 }}>
              <div style={{ display: current === 0 ? 'unset' : 'none' }}>
                <SelectPlatform
                    getPolicyConfigJson={this.getPolicyConfigJson}
                />
              </div>
              <div style={{ display: current === 1 ? 'unset' : 'none' }}>
                <ConfigureProfile
                    policiesList={policiesList}
                />
              </div>
              <div style={{ display: current === 2 ? 'unset' : 'none' }}></div>
              <div style={{ display: current === 3 ? 'unset' : 'none' }}></div>
              <div style={{ display: current === 4 ? 'unset' : 'none' }}></div>
              <div style={{ display: current === 5 ? 'unset' : 'none' }}></div>
            </Card>
          </Col>
          <Col span={16} offset={4}>
            <div style={{ marginTop: 24 }}>
              {current > 0 && (
                <Button style={{ marginRight: 8 }} onClick={() => this.prev()}>
                  Previous
                </Button>
              )}
              {current < 5 && current > 0 && (
                <Button type="primary" onClick={() => this.next()}>
                  Next
                </Button>
              )}
              {current === 5 && <Button type="primary">Done</Button>}
            </div>
          </Col>
        </Row>
      </div>
    );
  }
}

export default withConfigContext(
  Form.create({ name: 'add-policy' })(AddPolicy),
);
