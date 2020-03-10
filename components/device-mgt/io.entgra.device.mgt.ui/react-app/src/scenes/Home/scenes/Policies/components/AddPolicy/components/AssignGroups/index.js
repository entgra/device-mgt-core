import React from 'react';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import {
  Form,
  Icon,
  message,
  notification,
  Radio,
  Select,
  Tooltip,
} from 'antd';
import axios from 'axios';
const { Option } = Select;

class AssignGroups extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      roles: [],
      users: [],
      groups: [],
    };
  }
  componentDidMount() {
    this.getRole();
    this.fetchGroups();
  }

  handleSetUserRoleFormItem = event => {
    if (event.target.value === 'roles') {
      document.getElementById('roleSelector').style.display = 'block';
      document.getElementById('userSelector').style.display = 'none';
    } else {
      document.getElementById('userSelector').style.display = 'block';
      document.getElementById('roleSelector').style.display = 'none';
    }
  };

  getRole = () => {
    let apiURL =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/roles?user-store=PRIMARY&limit=100';

    axios
      .get(apiURL)
      .then(res => {
        if (res.status === 200) {
          const roles = [];
          for (let i = 0; i < res.data.data.roles.length; i++) {
            roles.push(
              <Option key={res.data.data.roles[i]}>
                {res.data.data.roles[i]}
              </Option>,
            );
          }
          this.setState({
            roles: roles,
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
            description: 'Error occurred while trying to load roles.',
          });
        }
      });
  };

  loadUsersList = value => {
    let apiURL =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/users/search/usernames?filter=' +
      value +
      '&domain=Primary';
    axios
      .get(apiURL)
      .then(res => {
        if (res.status === 200) {
          let user = JSON.parse(res.data.data);
          let users = [];
          for (let i = 0; i < user.length; i++) {
            users.push(
              <Option key={user[i].username}>{user[i].username}</Option>,
            );
          }
          this.setState({
            users: users,
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
            description: 'Error occurred while trying to load users.',
          });
        }
      });
  };

  // fetch data from api
  fetchGroups = () => {
    let apiUrl =
      window.location.origin +
      this.config.serverConfig.invoker.uri +
      this.config.serverConfig.invoker.deviceMgt +
      '/admin/groups';

    // send request to the invokerss
    axios
      .get(apiUrl)
      .then(res => {
        if (res.status === 200) {
          let groups = [];
          for (let i = 0; i < res.data.data.deviceGroups.length; i++) {
            groups.push(
              <Option key={res.data.data.deviceGroups[i].name}>
                {res.data.data.deviceGroups[i].name}
              </Option>,
            );
          }
          this.setState({
            groups: groups,
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
            description: 'Error occurred while trying to load device groups.',
          });
        }
      });
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    return (
      <div>
        <div>
          <Radio.Group onChange={this.handleSetUserRoleFormItem}>
            <Radio value="roleSelector">Set User role(s)</Radio>
            <Radio value="userSelector">Set User(s)</Radio>
          </Radio.Group>
          <div id={'roleSelector'} style={{ display: 'block' }}>
            <Form.Item>
              {getFieldDecorator('roles', {})(
                <Select mode="multiple" style={{ width: '100%' }}>
                  {this.state.roles}
                </Select>,
              )}
            </Form.Item>
          </div>
          <div id={'userSelector'} style={{ display: 'none' }}>
            <Form.Item>
              {getFieldDecorator('users', {})(
                <Select
                  mode="multiple"
                  style={{ width: '100%' }}
                  onSearch={this.loadUsersList}
                >
                  {this.state.users}
                </Select>,
              )}
            </Form.Item>
          </div>
        </div>
        <Form.Item label={'Select Groups'} style={{ display: 'block' }}>
          {getFieldDecorator('deviceGroups', {})(
            <Select mode="multiple" style={{ width: '100%' }}>
              {this.state.groups}
            </Select>,
          )}
        </Form.Item>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(AssignGroups));
