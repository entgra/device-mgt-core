import React from 'react';
import Authorized from '../../../../../../components/Authorized';
import ReleasePage from './components/ReleasePage';
import { Result } from 'antd';

class Release extends React.Component {
  render() {
    const { uuid, deviceType } = this.props.match.params;
    return (
      <Authorized
        permission="/permission/admin/app-mgt/store/application/view"
        yes={
          <ReleasePage
            uuid={uuid}
            deviceType={deviceType}
            changeSelectedMenuItem={this.props.changeSelectedMenuItem}
          />
        }
        no={
          <Result
            status="403"
            title="403"
            subTitle="You don't have permission to view apps."
          />
        }
      />
    );
  }
}

export default Release;
