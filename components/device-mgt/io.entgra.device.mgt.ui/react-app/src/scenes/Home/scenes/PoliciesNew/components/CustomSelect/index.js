import React from 'react';
import { Checkbox, Form, Icon, Select, Tooltip } from 'antd';
import { withConfigContext } from '../../../../../../components/ConfigContext';
// import { Button } from 'antd';
// import { Checkbox, Form, Icon, Tooltip } from 'antd';
const { Option } = Select;

class CustomSelect extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
  }

  findWithAttr = (array, attr, value) => {
    for (let i = 0; i < array.length; i += 1) {
      if (array[i][attr] === value) {
        return i;
      }
    }
    return -1;
  };

  handleSelector = (event, itemIndex, itemKey) => {
    console.log(event);
    const content = this.props.content;
    const contentIndex = this.findWithAttr(
      content,
      'featureCode',
      this.props.feature,
    );

    if (content[contentIndex].contents[0].items[itemIndex].key === itemKey) {
      content[contentIndex].contents[0].items[itemIndex].value = event;
    } else if (
      content[contentIndex].contents[0].hasOwnProperty('subContents')
    ) {
      content[contentIndex].contents[0].subContents.map(
        (subContent, subContentKey) => {
          if (subContent.items[itemIndex].key === itemKey) {
            subContent.items[itemIndex].value = event;
          }
        },
      );
    }

    this.props.setChangedPayload(content);
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    const itemData = this.props.itemData;
    return (
      <div>
        <Form.Item
          label={
            <span>
              {itemData.label}&nbsp;
              <Tooltip title={itemData.tooltip} placement="right">
                <Icon type="question-circle-o" />
              </Tooltip>
            </span>
          }
          style={{ display: 'block' }}
        >
          {getFieldDecorator(`${itemData.key}`, {
            initialValue: `${itemData.value}`,
          })(
            <Select
              onChange={e =>
                this.handleSelector(e, this.props.itemIndex, itemData.key)
              }
            >
              {itemData.select.options.map((option, i) => {
                return (
                  <Option key={i} value={option.definedValue}>
                    {option.displayingValue}
                  </Option>
                );
              })}
            </Select>,
          )}
        </Form.Item>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(CustomSelect));
