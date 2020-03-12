import React from 'react';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { Button, Col, Form, Input } from 'antd';
const { TextArea } = Input;

class PublishDevices extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
  }

  onHandlePrev() {
    this.props.getPrevStep();
  }

  render() {
    const { getFieldDecorator } = this.props.form;
    return (
      <div>
        <Form.Item
          label={'Set a name to your policy *'}
          style={{ display: 'block' }}
        >
          {getFieldDecorator('policyName', {
            rules: [
              {
                pattern: new RegExp('^.{1,30}$'),
                message: 'Should be 1-to-30 characters long',
              },
            ],
          })(<Input placeholder={'Should be 1 to 30 characters long'} />)}
        </Form.Item>
        <Form.Item label={'Add a Description'} style={{ display: 'block' }}>
          {getFieldDecorator('description', {})(<TextArea rows={8} />)}
        </Form.Item>
        <Col span={16} offset={18}>
          <div style={{ marginTop: 24 }}>
            <Button
              style={{ marginRight: 8 }}
              onClick={() => this.onHandlePrev()}
            >
              Back
            </Button>
            <Button type="primary" style={{ marginRight: 8 }}>
              Save & Publish
            </Button>
            <Button type="primary">Save</Button>
          </div>
        </Col>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(PublishDevices));
