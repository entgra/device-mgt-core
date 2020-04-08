import React from 'react';
import { Breadcrumb, Icon, PageHeader, Typography } from 'antd';
import { Link } from 'react-router-dom';
import AddPolicyNew from './AddPolicyNew';
const { Paragraph } = Typography;

class PoliciesNew extends React.Component {
  routes;

  constructor(props) {
    super(props);
    this.routes = props.routes;
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
            <Breadcrumb.Item>Policies</Breadcrumb.Item>
          </Breadcrumb>
          <div className="wrap">
            <h3>PolicyConfiguration</h3>
            <Paragraph>All policies for device management</Paragraph>
          </div>
          <div style={{ borderRadius: 5 }}>
            <AddPolicyNew />
          </div>
        </PageHeader>
        <div
          style={{ background: '#f0f2f5', padding: 24, minHeight: 720 }}
        ></div>
      </div>
    );
  }
}

export default PoliciesNew;
