import React, { Component } from 'react';
import { Breadcrumb, Icon, PageHeader } from 'antd';
import { Link } from 'react-router-dom';
import { withConfigContext } from '../../../../../../components/ConfigContext';
import PolicyProfile from '../../components/PolicyProfile';
class ViewPolicy extends Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
    this.config = this.props.context;
    this.state = {
      data: {},
      policyOverview: {},
      policyId: '',
    };
  }

  render() {
    const {
      match: { params },
    } = this.props;
    return (
      <div>
        <PageHeader style={{ paddingTop: 0 }}>
          <Breadcrumb style={{ paddingBottom: 16 }}>
            <Breadcrumb.Item>
              <Link to="/entgra">
                <Icon type="home" /> Home
              </Link>
            </Breadcrumb.Item>
            <Breadcrumb.Item>Policies</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            {/* <h3>Policies</h3>*/}
            {/* <Paragraph>Create new policy on IoT Server.</Paragraph>*/}
          </div>
          <div style={{ borderRadius: 5 }}>
            <PolicyProfile policyId={params.policyId} />
          </div>
        </PageHeader>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}
export default withConfigContext(ViewPolicy);
