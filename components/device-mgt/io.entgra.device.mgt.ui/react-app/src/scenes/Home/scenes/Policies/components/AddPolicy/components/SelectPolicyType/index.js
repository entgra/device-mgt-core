import React from 'react';
import { Form, Icon, Radio, Select, Tooltip } from 'antd';
import { withConfigContext } from '../../../../../../../../components/ConfigContext';
const { Option } = Select;

class SelectPolicyType extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
  }

  handlePolicyTypes = event => {
    if (event.target.value === 'GENERAL') {
      document.getElementById('generalPolicySubPanel').style.display = 'block';
    } else {
      document.getElementById('generalPolicySubPanel').style.display = 'none';
    }
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    return (
      <div>
        <Form.Item style={{ display: 'block' }}>
          {getFieldDecorator('policyType', {
            initialValue: 'GENERAL',
          })(
            <Radio.Group onChange={this.handlePolicyTypes}>
              <Radio value="GENERAL">General Policy</Radio>
              <Radio value="CORRECTIVE">Corrective Policy</Radio>
            </Radio.Group>,
          )}
        </Form.Item>
        <div id="generalPolicySubPanel" style={{ display: 'block' }}>
          <Form.Item
            label={
              <span>
                Select Corrective Policy&nbsp;
                <Tooltip
                  title={
                    'Select the corrective policy to be applied when this general policy is violated'
                  }
                  placement="right"
                >
                  <Icon type="question-circle-o" />
                </Tooltip>
              </span>
            }
          >
            {getFieldDecorator('correctiveActions', {})(
              <Select style={{ width: '100%' }}>
                <Option value="">None</Option>
              </Select>,
            )}
          </Form.Item>
        </div>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(SelectPolicyType));
