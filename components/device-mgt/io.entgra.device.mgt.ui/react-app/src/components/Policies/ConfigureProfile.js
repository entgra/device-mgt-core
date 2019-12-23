import React from 'react';
import {Tabs, Row, Col, Switch, Menu,Input, Typography, Form, Checkbox, Select,
    Tooltip, Icon, Collapse, Alert, Upload, Button,Radio, Table, Popconfirm} from "antd";
import { FaBeer } from 'react-icons/fa';
import {withConfigContext} from "../../context/ConfigContext";
import "../../pages/Dashboard/Policies/policies.css";
import jsonResponse from "./configuration";
const { Title, Text, Paragraph } = Typography;
const { TabPane } = Tabs;
const {Option} = Select;
const {Panel} = Collapse;
const { TextArea } = Input;

const policyConfigurationsList = jsonResponse.PolicyConfigurations;

class ConfigureProfile extends React.Component {
    constructor(props) {
        super(props);
        this.config =  this.props.context;
        this.policies = policyConfigurationsList.androidPolicy.Policy;
        this.state = {
            isDisplayMain: "none",
            activePanelKeys: [],
            activeSubPanelKeys: [],
            count: 0,
            activeRadioValues: "",
        }
    };

    componentDidMount() {
    };

    handleRadioPanel = (e)=>{
        this.setState({
            activeRadioValue : e.target.value
        });
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

    getPanelItems = (panel)=>{
        const { getFieldDecorator } = this.props.form;
            return (
                panel.map((item,k)=>{
                    switch(item._type){
                        case "select":
                            return(
                                <Form.Item key={k}
                                           label={
                                               <span>
                                               {item.Label}&nbsp;
                                                   <Tooltip title={item.tooltip} placement="right">
                                                   <Icon type="question-circle-o" />
                                               </Tooltip>
                                           </span>
                                           }
                                           style={{display: "block"}}>
                                    {getFieldDecorator(`${item._id}`, {
                                        initialValue: `${item.Optional.Option[0].name}`
                                    })(
                                        <Select>
                                            {item.Optional.Option.map((option,i)=>{
                                                return(
                                                    <Option value={option.value}>{option.name}</Option>
                                                );
                                            })}
                                        </Select>
                                    )}
                                </Form.Item>
                            );
                        case "input":
                            return(
                                <Form.Item key={k}
                                           label={
                                               <span>
                                               {item.Label}&nbsp;
                                                   <Tooltip title={item.tooltip} placement="right">
                                                    <Icon type="question-circle-o" />
                                               </Tooltip>
                                           </span>
                                           }
                                           style={{display: "block"}}>
                                    {getFieldDecorator(`${item._id}`, {
                                        rules: [
                                            {
                                                pattern: new RegExp(`${item.Optional.rules.regex}`),
                                                message: `${item.Optional.rules.validationMsg}`,
                                            },
                                        ],
                                    })(
                                        <Input placeholder={item.Optional.Placeholder}/>
                                    )}
                                </Form.Item>
                            );
                        case "checkbox":
                            if(item.Optional.hasOwnProperty("subPanel")){
                                return (
                                    <div key={k} >
                                        <Collapse bordered={false} activeKey={this.state.activeSubPanelKeys} >
                                            <Collapse.Panel key={item._id}
                                                            showArrow={false}
                                                            style={{border:0}}
                                                            header={
                                                                <Form.Item key={k}>
                                                                    {getFieldDecorator(`${item._id}`, {
                                                                        valuePropName: 'checked',
                                                                        initialValue: item.Optional.checked,
                                                                    })(
                                                                        <Checkbox
                                                                            onChange={this.handleSubPanel}>
                                                                            <span>
                                                                                {item.Label}&nbsp;
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
                                                        {this.getPanelItems(item.Optional.subPanel.PanelItem)}
                                                    </div>
                                                </div>
                                            </Collapse.Panel>
                                        </Collapse>
                                    </div>
                                )
                            }else{
                                return(
                                    <Form.Item key={k}>
                                        {getFieldDecorator(`${item._id}`, {
                                            valuePropName: 'checked',
                                            initialValue: item.Optional.checked,
                                        })(
                                            <Checkbox>
                                        <span>
                                            {item.Label}&nbsp;
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
                                               {item.Label}&nbsp;
                                                   <Tooltip title={item.tooltip} placement="right">
                                                    <Icon type="question-circle-o" />
                                               </Tooltip>
                                           </span>
                                           }
                                           style={{display: "block"}}>
                                    {getFieldDecorator(`${item._id}`, {

                                    })(
                                        <TextArea placeholder={item.Optional.Placeholder}
                                                  rows={item.Optional.Row} />
                                    )}
                                </Form.Item>
                            );
                        case "radioGroup":
                            return(
                                <div>
                                    <Form.Item key={k}
                                               label={
                                                   <span>
                                               {item.Label}&nbsp;
                                                       <Tooltip title={item.tooltip} placement="right">
                                                    <Icon type="question-circle-o" />
                                               </Tooltip>
                                           </span>
                                               }
                                               style={{display: "block"}}>
                                        {getFieldDecorator(`${item._id}`, {
                                        })(
                                            <Radio.Group onChange={this.handleRadioPanel}>
                                                {item.Optional.Radio.map((option,i)=>{
                                                    return(
                                                        <Radio value={option.value}>{option.name}</Radio>
                                                    );
                                                })}
                                            </Radio.Group>
                                        )}
                                    </Form.Item>
                                    <div className={"radio-panel-container"} style={{marginTop: -10}}>
                                        <Tabs id={item._id} activeKey={this.state.activeRadioValue}>
                                            {item.Optional.Radio.map((option,i)=>{
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
                                <Title key={k} level={4}>{item.Label} </Title>
                            );
                        case "paragraph":
                            return(
                                <Paragraph key={k} style={{marginTop:20}}>{item.Label} </Paragraph>
                            );
                        case "alert":
                            return(
                                <Alert
                                    key={k}
                                    description={item.Label}
                                    type="warning"
                                    showIcon
                                />
                            );
                        case "upload":
                            return(
                                <Form.Item key={k}
                                           label={
                                               <span>
                                               {item.Label}&nbsp;
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
                            const column = this.getColumns({ getFieldDecorator }, item.Optional.columns);
                            return (
                                <div key={k}>
                                    <Button
                                        onClick={()=>this.handleAdd(dataSourceArr)}
                                        type="primary"
                                        style={{ marginBottom: 16 }}>
                                        <Icon type="plus-circle"/>{item.Optional.button.name}
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
                            <TabPane tab={<span>{element.Name}</span>} key={i}  >
                                { Object.values(element.Panel).map((panel, j)=>{
                                    return(
                                        <div key={j}>
                                            <Collapse bordered={false} activeKey={this.state.activePanelKeys}>
                                                <Collapse.Panel key={panel.panelId}
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
                                                            {this.getPanelItems(panel.PanelItem)}
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
            </div>);
    }
}

export default withConfigContext(Form.create()(ConfigureProfile));