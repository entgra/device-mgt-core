import React from 'react';
import { Checkbox, Form, Icon, Tooltip, Typography } from 'antd';
import { withConfigContext } from '../../../../../../components/ConfigContext';
// import { Button } from 'antd';
// import { Checkbox, Form, Icon, Tooltip } from 'antd';
const { Title } = Typography;

class CustomCheckBox extends React.Component {
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
  handleCheckbox = (event, itemIndex, itemKey) => {
    const content = this.props.content;
    const contentIndex = this.findWithAttr(
      content,
      'featureCode',
      this.props.feature,
    );

    if (content[contentIndex].contents[0].items[itemIndex].key === itemKey) {
      content[contentIndex].contents[0].items[itemIndex].value =
        event.target.checked;
    } else if (
      content[contentIndex].contents[0].hasOwnProperty('subContents')
    ) {
      content[contentIndex].contents[0].subContents.map(
        (subContent, subContentKey) => {
          if (subContent.items[itemIndex].key === itemKey) {
            subContent.items[itemIndex].value = event.target.checked;
          }
        },
      );
    }

    this.props.setChangedPayload(content);
  };

  render() {
    const { getFieldDecorator } = this.props.form;
    const itemData = this.props.itemData;
    const subContent = this.props.content[0].contents[0].subContents;
    const feature = this.props.content[0].featureCode;
    return (
      <div>
        {itemData.hasOwnProperty('subTitle') && (
          <div>
            <Title level={4}>{itemData.subTitle}</Title>
          </div>
        )}
        <Form.Item>
          {getFieldDecorator(`${itemData.key}`, {
            valuePropName: 'checked',
            initialValue: itemData.value,
          })(
            <Checkbox
              onChange={e =>
                this.handleCheckbox(e, this.props.itemIndex, itemData.key)
              }
            >
              <span>
                {itemData.label}&nbsp;
                <Tooltip title={itemData.tooltip} placement="right">
                  <Icon type="question-circle-o" />
                </Tooltip>
              </span>
            </Checkbox>,
          )}
        </Form.Item>

        {subContent.map((content, contentKey) => {
          if (
            content.conditionList.conditions[0].name === itemData.key &&
            itemData.value
          ) {
            return (
              <div>
                {this.props.getContentItem(
                  content.items,
                  feature,
                  this.props.content,
                )}
              </div>
            );
          }
        })}
      </div>
    );
  }
}

export default withConfigContext(Form.create()(CustomCheckBox));
