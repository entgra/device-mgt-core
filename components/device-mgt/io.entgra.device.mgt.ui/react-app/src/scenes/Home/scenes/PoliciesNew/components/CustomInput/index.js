import React from 'react';
import { Form, Icon, Input, Tooltip } from 'antd';
import { withConfigContext } from '../../../../../../components/ConfigContext';
// import { Button } from 'antd';
// import { Checkbox, Form, Icon, Tooltip } from 'antd';

class CustomInput extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      value: null,
    };
  }

  findWithAttr = (array, attr, value) => {
    for (let i = 0; i < array.length; i += 1) {
      if (array[i][attr] === value) {
        return i;
      }
    }
    return -1;
  };
  handleInput = ({ target: { value } }, itemIndex, itemKey) => {
    const content = this.props.content;
    const contentIndex = this.findWithAttr(
      content,
      'featureCode',
      this.props.feature,
    );

    if (content[contentIndex].contents[0].items[itemIndex].key === itemKey) {
      content[contentIndex].contents[0].items[itemIndex].value =
        value;
    } else if (
      content[contentIndex].contents[0].hasOwnProperty('subContents')
    ) {
      content[contentIndex].contents[0].subContents.map(
        (subContent, subContentKey) => {
          if (subContent.items[itemIndex].key === itemKey) {
            subContent.items[itemIndex].value = value;
          }
        },
      );
    }
    this.props.setChangedPayload(content);
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    const { value } = this.state;
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
            initialValue: null,
            rules: [
              {
                pattern: new RegExp(`${itemData.input.regEx}`),
                message: `${itemData.input.validationMessage}`,
              },
            ],
          })(
            <Input
              value={value}
              onChange={e => this.handleInput(e, this.props.itemIndex, itemData.key)}
              placeholder={itemData.input.placeholderValue}
            />,
          )}
        </Form.Item>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(CustomInput));
