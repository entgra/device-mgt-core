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
            activeSelectedValues: [],
            dataArray: []
        }
    };

    componentDidMount() {
    };

    //handle items which handle from radio buttons
    handleRadioPanel = (e, subPanel)=>{
        console.log(e);
        {subPanel.map((panel,i) =>{
            if(panel.value===e.target.value){
                document.getElementById(panel.value).style.display = "block";
            }else {
                document.getElementById(panel.value).style.display = "none";
            }
        })}

    };

    //handle items which handle from select options
    handleSelectedPanel =(e, subPanel)=>{
        {subPanel.map((panel,i) =>{
            if(panel.key===e){
                document.getElementById(panel.key).style.display = "block";
            }else {
                document.getElementById(panel.key).style.display = "none";
            }
        })}
    };

    //handle items which handle from checkbox
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

    handleAdd = (dataSource, id) => {
        const { count, dataArray } = this.state;
        const newData = [{
            key: count,
        }];
        this.setState({
            dataArray: [...dataArray, newData],
            count: count +1
        });
        console.log(dataArray);
        console.log(id);
    };

    getColumns = ({ getFieldDecorator },arr) =>{
        const columnArray = [];
        const actionColumn = [{
                title: '',
                dataIndex: 'operation',
                render: (text, record) =>
                    <Form.Item>
                        <Popconfirm title="Sure to delete?" >
                            <a><Text type="danger"><Icon type="delete"/></Text></a>
                        </Popconfirm>
                    </Form.Item> },
        ];
        Object.values(arr).map((columnData, c) =>{
             if(columnData.type==="input"){
                const column = {
                    title: `${columnData.name}`,
                    dataIndex: `${columnData.key}`,
                    key: `${columnData.key}`,
                    render: (name, row, i) => (
                        <Form.Item>
                            {getFieldDecorator(`${columnData.key}`,{})
                            (<Input type={columnData.others.inputType} placeholder={columnData.others.placeholder}/>)}
                        </Form.Item>)
                };
                columnArray.push(column);

            }else if(columnData.type==="upload"){
                const column = {
                    title: `${columnData.name}`,
                    dataIndex: `${columnData.key}`,
                    key: `${columnData.key}`,
                    render: (name, row, i) => (
                        <Form.Item>
                            {getFieldDecorator(`${columnData.key}`, {

                            })(
                                <Upload>
                                    <Button>
                                        <Icon type="upload" /> Choose file
                                    </Button>
                                </Upload>,
                            )}
                        </Form.Item>)
                };
                columnArray.push(column);
            }else if(columnData.type==="select"){
                 const column = {
                     title: `${columnData.name}`,
                     dataIndex: `${columnData.key}`,
                     key: `${columnData.key}`,
                     render: (name, row, i) => (
                         <Form.Item>
                             {getFieldDecorator(`${columnData.key}`, {
                                 initialValue: columnData.others.initialDataIndex
                             })(
                                 <Select>
                                     {columnData.others.options.map((option,i)=>{
                                         return(
                                             <Option value={option.key}>{option.value}</Option>
                                         );
                                     })}
                                 </Select>,
                             )}
                         </Form.Item>
                         )
                 };
                 columnArray.push(column);

             }


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
                                                <Select onChange={(e)=>this.handleSelectedPanel(e, item.optional.subPanel)}>
                                                    {item.optional.option.map((option,i)=>{
                                                        return(
                                                            <Option value={option.value}>{option.name}</Option>
                                                        );
                                                    })}
                                                </Select>
                                            )}
                                        </Form.Item>
                                        <div className={"sub-panel-container"} >
                                            {item.optional.subPanel.map((panel,i) =>{
                                                return(
                                                    <div id={panel.key} style={{display:"none"}}>
                                                    {this.getPanelItems(panel.panelItem)}
                                                    </div>
                                                );
                                            })}
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
                                            initialValue:`${item.optional.initialValue}`
                                        })(
                                            <Radio.Group onChange={(e)=>this.handleRadioPanel(e, item.optional.radio)}>
                                                {item.optional.radio.map((option,i)=>{
                                                    return(
                                                        <Radio value={option.value}>{option.name}</Radio>
                                                    );
                                                })}
                                            </Radio.Group>
                                        )}
                                    </Form.Item>
                                    <div className={"radio-panels"} style={{marginTop: -10}}>
                                        {
                                            item.optional.radio.map((option,i)=>{
                                            return(
                                                <div id={option.value} style={{display:"none"}}>
                                                    {this.getPanelItems(option.subPanel)}
                                                </div>
                                            );
                                        })}
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
                            let dataArray = [];
                            const column = this.getColumns({ getFieldDecorator }, item.optional.columns);
                            return (
                                <div key={k}>
                                    <Button
                                        onClick={()=>this.handleAdd(dataArray, item.id)}
                                        type="primary"
                                        style={{ marginBottom: 16 }}>
                                        <Icon type="plus-circle"/>{item.optional.button.name}
                                    </Button>
                                    <Table id={item.id} dataSource={this.state.dataArray} columns={column}/>
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