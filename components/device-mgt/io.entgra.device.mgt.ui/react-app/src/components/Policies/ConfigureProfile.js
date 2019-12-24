/*
 * Copyright (c) 2019, Entgra (pvt) Ltd. (http://entgra.io) All Rights Reserved.
 *
 * Entgra (pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import {Tabs, Row, Col, Switch, Menu,Input, Typography, Form, Checkbox, Select,
    Tooltip, Icon, Collapse, Alert, Upload, Button,Radio, Table, Popconfirm} from "antd";
import {withConfigContext} from "../../context/ConfigContext";
import "../../pages/Dashboard/Policies/policies.css";
import jsonResponse from "./configuration";
const { Title, Text, Paragraph } = Typography;
const { TabPane } = Tabs;
const {Option} = Select;
const {Panel} = Collapse;
const { TextArea } = Input;

const policyConfigurationslist = jsonResponse.policyConfigurations;

class ConfigureProfile extends React.Component {
    constructor(props) {
        super(props);
        this.config =  this.props.context;
        this.policies = policyConfigurationslist.androidPolicy.policy;
        this.state = {
            isDisplayMain: "none",
            activePanelKeys: [],
            activeSubPanelKeys: [],
            count: 0,
            activeRadioValues: "",
            activeSelectedValues: "",
        }
    };

    componentDidMount() {
    };

    handleRadioPanel = (e)=>{
        this.setState({
            activeRadioValue : e.target.value
        });
    };

    handleSelectedPanel =(e)=>{
      console.log(e);
      this.setState({
          activeSelectedValues: e
      })
    };

    handleSubPanel = (e) =>{
        if(e.target.checked){
            let joined = this.state.activeSubPanelKeys.concat(e.target.id);
            this.setState({ activeSubPanelKeys: joined });
        }else{
            let index = this.state.activeSubPanelKeys.indexOf(e.target.id);
            if (index !== -1) {
                this.state.activeSubPanelKeys.splice(index, 1);
                let removed = this.state.activeSubPanelKeys;
                this.setState({ activeSubPanelKeys: removed });
            }
        }
    };

    //handle Switch on off button
    handleMainPanel = (e, ref) =>{
        if(e){
            let joined = this.state.activePanelKeys.concat(ref);
            this.setState({ activePanelKeys: joined });
        }else{
            let index = this.state.activePanelKeys.indexOf(ref);
            if (index !== -1) {
                this.state.activePanelKeys.splice(index, 1);
                let removed = this.state.activePanelKeys;
                this.setState({ activePanelKeys: removed });
            }
        }
    };

    // handleAdd = (dataSource) => {
    //     const { count } = this.state;
    //     const newData = [{
    //         key: count,
    //     }];
    //     dataSource.concat(newData);
    //     // console.log(count+1);
    //     this.setState({
    //         count: count +1
    //     });
    // };

    getColumns = ({ getFieldDecorator },arr) =>{
        const columnArray = [];
        const actionColumn = [{
                title: 'Actions',
                dataIndex: 'operation',
                render: (text, record) =>
                    <Form.Item>
                        <Popconfirm title="Sure to delete?" >
                            Delete
                        </Popconfirm>
                    </Form.Item> },
        ];
        Object.values(arr).map((columnData, c) =>{
            const column = {
                title: `${columnData.name}`,
                dataIndex: `${columnData.key}`,
                key: `${columnData.key}`,
                render: (name, row, i) => (
                    <Form.Item>
                        {getFieldDecorator({})(<Input/>)}
                    </Form.Item>)
            };
            columnArray.push(column);
        });
        const columns = columnArray.concat(actionColumn);
        return(columns);
    };

    //generate form items
    getPanelItems = (panel)=>{
        const { getFieldDecorator } = this.props.form;
            return (
                panel.map((item,k)=>{
                    switch(item.type){
                        case "select":
                            if(item.optional.hasOwnProperty("subPanel")){
                                return(
                                    <div>
                                        <Form.Item key={k}
                                                   label={
                                                       <span>
                                               {item.label}&nbsp;
                                                           <Tooltip title={item.tooltip} placement="right">
                                                   <Icon type="question-circle-o" />
                                               </Tooltip>
                                           </span>
                                                   }
                                                   style={{display: "block"}}>
                                            {getFieldDecorator(`${item.id}`, {
                                                initialValue: `${item.optional.option[0].name}`
                                            })(
                                                <Select onChange={this.handleSelectedPanel}>
                                                    {item.optional.option.map((option,i)=>{
                                                        return(
                                                            <Option className={option.panelKey} value={option.value}>{option.name}</Option>
                                                        );
                                                    })}
                                                </Select>
                                            )}
                                        </Form.Item>
                                        <div className={"sub-panel-container"}>
                                            <Tabs animated={false} tabBarGutter={0} activeKey={this.state.activeSelectedValues}>
                                                {item.optional.subPanel.map((panel,i) =>{
                                                    return(
                                                        <TabPane
                                                            tab={""}
                                                            key={panel.key}
                                                        >
                                                            {this.getPanelItems(panel.panelItem)}
                                                        </TabPane>
                                                    );
                                                })}
                                            </Tabs>
                                        </div>
                                    </div>
                                );
                            }
                            else {
                            return(
                                <Form.Item key={k}
                                           label={
                                               <span>
                                               {item.label}&nbsp;
                                                   <Tooltip title={item.tooltip} placement="right">
                                                   <Icon type="question-circle-o" />
                                               </Tooltip>
                                           </span>
                                           }
                                           style={{display: "block"}}>
                                    {getFieldDecorator(`${item.id}`, {
                                        initialValue: `${item.optional.option[0].name}`
                                    })(
                                        <Select>
                                            {item.optional.option.map((option,i)=>{
                                                return(
                                                    <Option value={option.value}>{option.name}</Option>
                                                );
                                            })}
                                        </Select>
                                    )}
                                </Form.Item>
                            );
                            }
                        case "input":
                            return(
                                <Form.Item key={k}
                                           label={
                                               <span>
                                               {item.label}&nbsp;
                                                   <Tooltip title={item.tooltip} placement="right">
                                                    <Icon type="question-circle-o" />
                                               </Tooltip>
                                           </span>
                                           }
                                           style={{display: "block"}}>
                                    {getFieldDecorator(`${item.id}`, {
                                        rules: [
                                            {
                                                pattern: new RegExp(`${item.optional.rules.regex}`),
                                                message: `${item.optional.rules.validationMsg}`,
                                            },
                                        ],
                                    })(
                                        <Input placeholder={item.optional.placeholder}/>
                                    )}
                                </Form.Item>
                            );
                        case "checkbox":
                            if(item.optional.hasOwnProperty("subPanel")){
                                return (
                                    <div key={k} >
                                        <Collapse bordered={false} activeKey={this.state.activeSubPanelKeys} >
                                            <Collapse.Panel key={item.id}
                                                            showArrow={false}
                                                            style={{border:0}}
                                                            header={
                                                                <Form.Item key={k}>
                                                                    {getFieldDecorator(`${item.id}`, {
                                                                        valuePropName: 'checked',
                                                                        initialValue: item.optional.checked,
                                                                    })(
                                                                        <Checkbox
                                                                            onChange={this.handleSubPanel}>
                                                                            <span>
                                                                                {item.label}&nbsp;
                                                                                <Tooltip title={item.tooltip} placement="right">
                                                                                    <Icon type="question-circle-o" />
                                                                                </Tooltip>
                                                                            </span>
                                                                        </Checkbox>
                                                                    )}
                                                                </Form.Item>
                                                            }>
                                                <div>
                                                    <div>
                                                        {this.getPanelItems(item.optional.subPanel.panelItem)}
                                                    </div>
                                                </div>
                                            </Collapse.Panel>
                                        </Collapse>
                                    </div>
                                )
                            }else{
                                return(
                                    <Form.Item key={k}>
                                        {getFieldDecorator(`${item.id}`, {
                                            valuePropName: 'checked',
                                            initialValue: item.optional.checked,
                                        })(
                                            <Checkbox>
                                        <span>
                                            {item.label}&nbsp;
                                            <Tooltip title={item.tooltip} placement="right">
                                                <Icon type="question-circle-o" />
                                            </Tooltip>
                                        </span>
                                            </Checkbox>
                                        )}
                                    </Form.Item>
                                );
                            }

                        case "textArea":
                            return(
                                <Form.Item key={k}
                                           label={
                                               <span>
                                               {item.label}&nbsp;
                                                   <Tooltip title={item.tooltip} placement="right">
                                                    <Icon type="question-circle-o" />
                                               </Tooltip>
                                           </span>
                                           }
                                           style={{display: "block"}}>
                                    {getFieldDecorator(`${item.id}`, {

                                    })(
                                        <TextArea placeholder={item.optional.placeholder}
                                                  rows={item.optional.row} />
                                    )}
                                </Form.Item>
                            );
                        case "radioGroup":
                            return(
                                <div>
                                    <Form.Item key={k}
                                               label={
                                                   <span>
                                               {item.label}&nbsp;
                                                       <Tooltip title={item.tooltip} placement="right">
                                                    <Icon type="question-circle-o" />
                                               </Tooltip>
                                           </span>
                                               }
                                               style={{display: "block"}}>
                                        {getFieldDecorator(`${item.id}`, {
                                        })(
                                            <Radio.Group onChange={this.handleRadioPanel}>
                                                {item.optional.radio.map((option,i)=>{
                                                    return(
                                                        <Radio value={option.value}>{option.name}</Radio>
                                                    );
                                                })}
                                            </Radio.Group>
                                        )}
                                    </Form.Item>
                                    <div className={"radio-panels"} style={{marginTop: -10}}>
                                        <Tabs id={item.id} activeKey={this.state.activeRadioValue}>
                                            {item.optional.radio.map((option,i)=>{
                                                return(
                                                    <TabPane
                                                        tab={null}
                                                        key={option.value}
                                                    >
                                                        {this.getPanelItems(option.subPanel)}
                                                    </TabPane>
                                                );
                                            })}
                                        </Tabs>
                                    </div>
                                </div>
                            );
                        case "title":
                            return(
                                <Title key={k} level={4}>{item.label} </Title>
                            );
                        case "paragraph":
                            return(
                                <Paragraph key={k} style={{marginTop:20}}>{item.label} </Paragraph>
                            );
                        case "alert":
                            return(
                                <Alert
                                    key={k}
                                    description={item.label}
                                    type="warning"
                                    showIcon
                                />
                            );
                        case "upload":
                            return(
                                <Form.Item
                                    key={k}
                                    label={
                                        <span>
                                            {item.label}&nbsp;
                                            <Tooltip title={item.tooltip} placement="right">
                                                <Icon type="question-circle-o" />
                                            </Tooltip>
                                        </span>
                                     }
                                >
                                    {getFieldDecorator('upload', {

                                    })(
                                        <Upload>
                                            <Button>
                                                <Icon type="upload" /> Click to upload
                                            </Button>
                                        </Upload>,
                                    )}
                                </Form.Item>
                            );
                        case "inputTable":
                            let dataSourceArr = [];
                            const column = this.getColumns({ getFieldDecorator }, item.optional.columns);
                            return (
                                <div key={k}>
                                    <Button
                                        onClick={()=>this.handleAdd(dataSourceArr)}
                                        type="primary"
                                        style={{ marginBottom: 16 }}>
                                        <Icon type="plus-circle"/>{item.optional.button.name}
                                    </Button>
                                    <Table dataSource={dataSourceArr} columns={column}/>
                                </div>
                            );
                        default:
                            return null;
                    }
                })
            )
    };

    render() {
        return (
            <div className="tab-container">
                <Tabs tabPosition={"left"} size={"large"}>
                    { this.policies.map((element, i) =>{
                        return(
                            <TabPane tab={<span>{element.name}</span>} key={i}  >
                                { Object.values(element.panel).map((panel, j)=>{
                                    return(
                                        <div key={j}>
                                            <Collapse bordered={false} activeKey={this.state.activePanelKeys}>
                                                <Collapse.Panel
                                                    key={panel.panelId}
                                                    showArrow={false}
                                                    style={{border:0}}
                                                    header={
                                                        <div>
                                                            <Row>
                                                                <Col offset={0} span={14}>
                                                                    <Title level={4}> {panel.title} </Title>
                                                                </Col>
                                                                <Col offset={8}  span={1}>
                                                                    <Switch
                                                                        checkedChildren="ON"
                                                                        unCheckedChildren="OFF"
                                                                        onChange={(e)=>this.handleMainPanel(e, `${panel.panelId}`)}/>
                                                                </Col>
                                                            </Row>
                                                            <Row>{panel.description}</Row>
                                                        </div>}>
                                                    <div>
                                                        <Form>
                                                            {this.getPanelItems(panel.panelItem)}
                                                        </Form>
                                                    </div>
                                                </Collapse.Panel>
                                            </Collapse>
                                        </div>);
                                })
                                }
                            </TabPane>)
                    })
                    }
                </Tabs>
            </div>
        );
    }
}

export default withConfigContext(Form.create()(ConfigureProfile));