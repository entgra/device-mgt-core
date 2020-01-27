import React from 'react';
import {
  Tabs,
  Row,
  Col,
  Switch,
  Input,
  Typography,
  Form,
  Collapse,
  Checkbox,
  Select,
  Tooltip,
  Icon,
  Table,
  Alert,
  Upload,
  Popconfirm,
  Button,
  Radio, message, notification,
} from 'antd';
import { withConfigContext } from '../../context/ConfigContext';
import '../../pages/Dashboard/Policies/policies.css';
import jsonResponse from './configuration';
import axios from "axios";
const { Text, Title, Paragraph } = Typography;
const { TabPane } = Tabs;
const { Option } = Select;
const { TextArea } = Input;
const {Panel} = Collapse;
let apiUrl;

const policyConfigurationsList = jsonResponse.policyConfigurations;

class ConfigureProfile extends React.Component {
  constructor(props) {
    super(props);
    this.config = this.props.context;
    this.policies = policyConfigurationsList.androidPolicy.policy;
    this.state = {
      loading: false,
      isDisplayMain: "none",
      activePanelKeys: [],
      activeSubPanelKeys: [],
      count: 0,
      dataArray: [],
      customInputDataArray: [],
      inputTableDataSources: {},
      addPolicyForms: null,
    };
  }

  componentDidMount() {}

  //handle items which handle from radio buttons
  handleRadioPanel = (e, subPanel)=>{
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
      if(panel.id===e){
        document.getElementById(panel.id).style.display = "block";
      }else {
        document.getElementById(panel.id).style.display = "none";
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

  handleCustomInputTable = (event)=>{
    const { count, customInputDataArray } = this.state;
    const newData = [{
      key: count,
      CERT_NAME: `${event.file.name}`
    }];
    this.setState({
      customInputDataArray: [...customInputDataArray, newData],
      count: count +1
    });
  };

  handleAdd = (dataSource, array) => {
    const { count , inputTableDataSources} = this.state;
    const newData = [{
      key: count,
    }];
    inputTableDataSources[array].push(newData);
    Object.defineProperty(inputTableDataSources , array, {value: inputTableDataSources[array]});
    this.setState({
      inputTableDataSources ,
      count: count +1
    });
  };

  getColumns = ({ getFieldDecorator },arr) =>{
    const columnArray = [];
    const actionColumn = [{
      title: '',
      dataIndex: 'operation',
      render: (name, row) =>
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
                {getFieldDecorator(`${columnData.key}${i}`,{})
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
                {getFieldDecorator(`${columnData.key}${i}`, {})
                (
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
                {getFieldDecorator(`${columnData.key}${i}`, {
                  initialValue: columnData.others.initialDataIndex
                })(
                    <Select>
                      {columnData.others.option.map((option,i)=>{
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
                              <div id={panel.id} style={{display:"none"}}>
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
                                              initialValue: item.optional.ischecked,
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
                              {item.optional.subPanel.map((panel,i) =>{
                                return(
                                    <div>
                                      {this.getPanelItems(panel.panelItem)}
                                    </div>
                                );
                              })}
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
                        initialValue: item.optional.ischecked,
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
                      <div className={"sub-panel-container"} >
                          {item.optional.subPanel.map((panel,i) =>{
                              return(
                                  <div id={panel.id} style={(panel.id===item.optional.initialValue) ? {display:"block"}: {display:"none"}}>
                                      {this.getPanelItems(panel.panelItem)}
                                  </div>
                              );
                          })}
                      </div>
                    {/*<div className={"radio-panels"} style={{marginTop: -10}}>*/}
                    {/*  {*/}
                    {/*    item.optional.S.map((option,i)=>{*/}
                    {/*      return(*/}
                    {/*          <div id={option.id} style={{display:"none"}}>*/}
                    {/*            {this.getPanelItems(option.subPanel)}*/}
                    {/*          </div>*/}
                    {/*      );*/}
                    {/*    })}*/}
                    {/*</div>*/}
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
              const column1 = this.getColumns({ getFieldDecorator }, item.optional.columns);
              if(!(`${item.optional.dataSource}` in this.state.inputTableDataSources)){
                Object.defineProperty(this.state.inputTableDataSources , `${item.optional.dataSource}`, {value: [] , writable: true});
              }
              // this.setState({
              //   dataArrayObject.
              // })
              return (
                  <div key={k}>
                    <Button
                        onClick={()=>this.handleAdd(dataArray, item.optional.dataSource)}
                        type="primary"
                        style={{ marginBottom: 16 }}>
                      <Icon type="plus-circle"/>{item.optional.button.name}
                    </Button>
                    <Table id={item.id} dataSource={this.state.inputTableDataSources[item.optional.dataSource]} columns={column1}/>
                  </div>
              );

            case "customInputTable":
              const column2 = this.getColumns({ getFieldDecorator }, item.optional.columns);
              return (
                  <div key={k}>
                    <Upload onChange={this.handleCustomInputTable}>
                      <Button
                          // onClick={()=>this.handleAdd(dataArray, item.id)}
                          type="primary"
                          style={{ marginBottom: 16 }}>
                        <Icon type="plus-circle"/>{item.optional.button.name}
                      </Button>
                    </Upload>
                    <Table id={item.id} dataSource={this.state.customInputDataArray} columns={column2}/>
                  </div>
              );

            default:
              return null;
          }
        })
    )
  };


  render() {
      const { policyUIConfigurationsList } = this.props;
    return (
        <div className="tab-container">
            <Tabs tabPosition={"left"} size={"large"}>
                { policyUIConfigurationsList.map((element, i) =>{
                    return(
                        <TabPane tab={<span>{element.name}</span>} key={i}  >
                            { Object.values(element.panels).map((panel, j)=>{
                                panel = panel.panel;
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
