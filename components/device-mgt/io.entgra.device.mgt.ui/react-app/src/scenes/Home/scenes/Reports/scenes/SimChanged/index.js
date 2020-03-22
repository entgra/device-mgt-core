import React from 'react';

import { withConfigContext } from '../../../../../../components/ConfigContext';
import SimChangedTable from './components/SimChangedTable';

import { Breadcrumb, Icon, PageHeader, DatePicker, Radio } from 'antd';
import { Link } from 'react-router-dom';
import DeviceSelect from './components/DeviceSelect';

const { RangePicker } = DatePicker;

class SimChanged extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    this.state = {
      api: 'devices',
      dateFilters: {
        from: null,
        to: null,
      },
      deviceId: null,
      devicesListAll: null,
    };
  }

  handleRangePickerChange = (value, dateString) => {
    let dateFilters = this.state.dateFilters;
    dateFilters.from = new Date(dateString[0]).getTime();
    dateFilters.to = new Date(dateString[1]).getTime();
    this.setState(dateFilters);
  };

  handleApiRadioChange = e => {
    let api = e.target.value;
    this.setState({
      api: api,
    });
  };

  handleDeviceChange = e => {
    this.setState({
      deviceId: e,
    });
  };

  handleDevices = e => {
    this.setState({
      devicesListAll: e,
    });
  };

  render() {
    let api = this.state.api;
    let dateFilters = { ...this.state.dateFilters };
    let deviceId = this.state.deviceId;
    let devicesListAll = this.state.devicesListAll;

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
            <Breadcrumb.Item>Sim Changed Report</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <Radio.Group
              defaultValue="devices"
              style={{ marginBottom: 8, marginRight: 5 }}
              onChange={this.handleApiRadioChange}
            >
              <Radio.Button value="devices">Specific Device</Radio.Button>
              <Radio.Button value="all">All Devices</Radio.Button>
            </Radio.Group>
            <RangePicker
              format="YYYY/MM/DD"
              onChange={this.handleRangePickerChange}
              style={{ marginBottom: 8, marginRight: 5 }}
            />
            {this.state.api === 'devices' ? (
              <DeviceSelect
                style={{ marginBottom: 8, marginRight: 5 }}
                value={this.onChange}
                onDeviceChange={this.handleDeviceChange}
                devices={this.handleDevices}
              />
            ) : (
              false
            )}
          </div>
          <div className="wrap" style={{ marginBottom: '10px' }}>
            <div style={{ backgroundColor: '#ffffff', borderRadius: 5 }}>
              <SimChangedTable
                dateFilters={dateFilters}
                api={api}
                deviceId={deviceId}
                devicesListAll={devicesListAll}
              />
            </div>
          </div>
        </PageHeader>
      </div>
    );
  }
}

export default withConfigContext(SimChanged);
