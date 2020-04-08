import React from 'react';
import policyUIConfigurations from './ProfileCofigurationsList';
import {
  Button,
  Col,
  Collapse,
  Form,
  Row,
  Switch,
  Tabs,
  Typography,
} from 'antd';
import { withConfigContext } from '../../../../../../../components/ConfigContext';
import CustomCheckBox from '../../../components/CustomCheckBox';
import CustomSelect from '../../../components/CustomSelect';
import CustomInput from '../../../components/CustomInput';
const { Title } = Typography;
const { TabPane } = Tabs;

let policyConfigurePayload = {};
// const policiesList = policyUIConfigurations.policies;
// let payloadss = {};
class NewConfigureProfile extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.state = {
      activePanelKeys: [],
      payload: policyUIConfigurations.policies,
    };
  }

  setChangedPayload = content => {
    content.map(feature => {
      policyConfigurePayload[feature.featureCode] = feature.contents;
    });
    console.log(policyConfigurePayload);
  };

  addFeatures = features => {
    features.map(feature => {
      policyConfigurePayload[feature.featureCode] = feature.contents;
    });
  };

  removeFeatures = features => {
    features.map(feature => {
      delete policyConfigurePayload[feature.featureCode];
    });
  };

  // handle Switch on off button
  handleMainPanel = (e, ref, features) => {
    if (e) {
      let joined = this.state.activePanelKeys.concat(ref);
      this.setState({ activePanelKeys: joined });
      this.addFeatures(features);
    } else {
      let index = this.state.activePanelKeys.indexOf(ref);
      if (index !== -1) {
        this.state.activePanelKeys.splice(index, 1);
        let removed = this.state.activePanelKeys;
        this.setState({ activePanelKeys: removed });
        this.removeFeatures(features);
      }
    }
  };

  getContentItem = (content, feature, featureContent) => {
    return content.map((item, itemKey) => {
      if (item.hasOwnProperty('checkbox')) {
        return (
          <CustomCheckBox
            itemData={item}
            itemIndex={itemKey}
            content={featureContent}
            feature={feature}
            getContentItem={this.getContentItem}
            setChangedPayload={this.setChangedPayload}
          />
        );
      } else if (item.hasOwnProperty('select')) {
        return (
          <CustomSelect
            itemData={item}
            itemIndex={itemKey}
            content={featureContent}
            feature={feature}
            setChangedPayload={this.setChangedPayload}
          />
        );
      } else if (item.hasOwnProperty('input')) {
        return (
          <CustomInput
            itemData={item}
            itemIndex={itemKey}
            content={featureContent}
            feature={feature}
            setChangedPayload={this.setChangedPayload}
          />
        );
      }
    });
  };

  onHandleContinue = (e, formname) => {
    let profileFeaturesList = [];
    Object.entries(policyConfigurePayload).map(([feature, content]) => {
      let payloadContent = {};
      Object.values(content[0].items).map(value => {
        payloadContent[value.key] = value.value;
      });
      if (content[0].hasOwnProperty('subContents')) {
        Object.entries(content[0].subContents).map(([key, subContent]) => {
          if (payloadContent[subContent.conditionList.conditions[0].name]) {
            let subContents = {};
            Object.values(subContent.items).map(value => {
              subContents[value.key] = value.value;
            });
            payloadContent[subContent.key] = subContents;
          }
        });
      }
      let profileFeature = {
        featureCode: feature,
        deviceType: 'android',
        content: payloadContent,
      };
      profileFeaturesList.push(profileFeature);
    });
    console.log(profileFeaturesList);
    this.props.getPolicyPayloadData(formname, profileFeaturesList);
    this.props.getNextStep();
  };

  render() {
    const { payload } = this.state;
    return (
      <div className="tab-container">
        <Tabs tabPosition={'left'} size={'large'}>
          {payload.map((policy, i) => {
            return (
              <TabPane tab={<span>{policy.name}</span>} key={`policy-${i}`}>
                {Object.values(policy.features).map((feature, j) => {
                  return (
                    <div key={`feature-${j}`}>
                      <div>
                        <Row>
                          <Col offset={0} span={14}>
                            <Title level={4}> {policy.name} </Title>
                          </Col>
                          <Col offset={8} span={1}>
                            <Switch
                              checkedChildren="ON"
                              unCheckedChildren="OFF"
                              onChange={e =>
                                this.handleMainPanel(
                                  e,
                                  policy.name,
                                  policy.features,
                                )
                              }
                            />
                          </Col>
                        </Row>
                        <Row>{policy.description}</Row>
                      </div>
                      {this.state.activePanelKeys.includes(policy.name) && (
                        <Form>
                          {feature.contents.map((content, contentKey) => {
                            return (
                              <div key={`content-${contentKey}`}>
                                {this.getContentItem(
                                  content.items,
                                  feature.featureCode,
                                  policy.features,
                                )}
                              </div>
                            );
                          })}
                        </Form>
                      )}
                    </div>
                  );
                })}
              </TabPane>
            );
          })}
        </Tabs>
        <Col span={16} offset={20}>
          <div style={{ marginTop: 24 }}>
            <Button style={{ marginRight: 8 }} onClick={this.props.getPrevStep}>
              Back
            </Button>
            <Button
              type="primary"
              onClick={e => this.onHandleContinue(e, 'configureProfileData')}
            >
              Continue
            </Button>
          </div>
        </Col>
      </div>
    );
  }
}

export default withConfigContext(Form.create()(NewConfigureProfile));
