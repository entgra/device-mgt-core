import React from 'react';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
import { Form, Input } from 'antd';
const { TextArea } = Input;

class PublishDevices extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {};
  }
  componentDidMount() {}

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
      </div>
    );
  }
}

export default withConfigContext(Form.create()(PublishDevices));
