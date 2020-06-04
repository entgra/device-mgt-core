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
import { Tag, Timeline, Card } from 'antd';
import { withConfigContext } from '../../../../../../../../../../components/ConfigContext';
import axios from 'axios';
import { handleApiError } from '../../../../../../../../../../services/utils/errorHandler';

class LifeCycleHistory extends React.Component {
  constructor(props) {
    super(props);
    this.state = { lifeCycleStates: [] };
  }

  componentDidMount() {
    this.getLifeCycleHistory();
  }

  getLifeCycleHistory = () => {
    const config = this.props.context;
    const { uuid } = this.props;

    axios
      .get(
        window.location.origin +
          config.serverConfig.invoker.uri +
          config.serverConfig.invoker.publisher +
          '/applications/life-cycle/state-changes/' +
          uuid,
      )
      .then(res => {
        if (res.status === 200) {
          this.setState({ lifeCycleStates: JSON.parse(res.data.data) });
        }
      })
      .catch(error => {
        handleApiError(
          error,
          'Error occurred while trying to get lifecycle history',
        );
      });
  };

  render() {
    const { lifeCycleStates } = this.state;
    return (
      <div>
        <Timeline mode={'alternate'}>
          {lifeCycleStates.map(
            (state, index) =>
              state && (
                <Timeline.Item key={index} label={state.updatedAt}>
                  <Card>
                    State changed from <br />
                    <Tag color="blue">{state.previousState}</Tag> to{' '}
                    <Tag color="blue">{state.currentState}</Tag>
                  </Card>
                </Timeline.Item>
              ),
          )}
        </Timeline>
      </div>
    );
  }
}

export default withConfigContext(LifeCycleHistory);
