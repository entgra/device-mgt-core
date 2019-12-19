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

import React from "react";
import {
    PageHeader,
    Typography,
    Breadcrumb,
    Icon,
    Tag,
    Radio, Select, Button, Card,
    Row, Col, message, notification
} from "antd";

import {Link} from "react-router-dom";
import PoliciesTable from "../../../components/Policies/PoliciesTable";
import DevicesTable from "../../../components/Devices/DevicesTable";
import DateRangePicker from "../../../components/Reports/DateRangePicker";
import ReportDeviceTable from "../../../components/Devices/ReportDevicesTable";
import PieChart from "../../../components/Reports/Widgets/PieChart";
import axios from "axios";
import CountWidget from "../../../components/Reports/Widgets/CountWidget";
const {Paragraph} = Typography;
const { CheckableTag } = Tag;

const { Option } = Select;

class ReportDetails extends React.Component {
    routes;

    tagsFromServer = ['Enrolled', 'Unenrolled'];


    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state = {
            selectedTags: ['Enrolled'],
            paramsObject:{},
            count:0
        };

        console.log(window.innerHeight/2);
    }

    //Get modified value from datepicker and set it to paramsObject
    updateDurationValue = (modifiedFromDate,modifiedToDate) => {
        let tempParamObj = this.state.paramsObject;
        tempParamObj.from = modifiedFromDate;
        tempParamObj.to = modifiedToDate;
        this.setState({paramsObject:tempParamObj});
        console.log(this.state.paramsObject);
    };

    onRadioChange = (e) => {
        const { reportData } = this.props.location;
        console.log(e.target.value);
        const value = e.target.value;
        let tempParamObj = this.state.paramsObject;

        // tempParamObj.status = value;
        // if(value=="ALL" && tempParamObj.status) {
        //     delete tempParamObj.status;
        // }

        this.setParam(tempParamObj, reportData.paramsType, value);

        this.setState({paramsObject:tempParamObj});
        console.log(this.state.paramsObject);
    };

    getStats = (value) => {
        console.log(value);
        //this.setState({count:value})
    }

    setParam = (tempParamObj, type, value) => {
        if(type=="status"){
            tempParamObj.status = value;
            if(value=="ALL" && tempParamObj.status) {
                delete tempParamObj.status;
            }
        } else if(type=="ownership"){
            tempParamObj.ownership = value;
            if(value=="ALL" && tempParamObj.ownership) {
                delete tempParamObj.ownership;
            }
        }
    };

    render() {
        const { selectedTags } = this.state;
        const { reportData } = this.props.location;
        console.log(reportData);

        let radioItems;
        if(reportData){
            radioItems = reportData.params.map((data) =>
                <Radio.Button value={data} key={data}>{data}</Radio.Button>
            );
        }else{
            const empty = ['ALL']
            radioItems = empty.map((data) =>
                <Radio.Button value={data} key={data}>{data}</Radio.Button>
            );
        }
        const params = {...this.state.paramsObject};


        return (
            <div>
                <PageHeader style={{paddingTop: 0}}>
                    <Breadcrumb style={{paddingBottom: 16}}>
                        <Breadcrumb.Item>
                            <Link to="/entgra"><Icon type="home"/> Home</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>Report</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap" style={{marginBottom: '10px'}}>
                        <h3>Summary of enrollments</h3>
                        <div style={{marginBottom: '10px'}}>

                            <Select defaultValue="android" style={{ width: 120 , marginRight:10}}>
                                <Option value="android">Android</Option>
                                <Option value="ios">IOS</Option>
                                <Option value="windows">Windows</Option>
                            </Select>

                            <DateRangePicker
                                updateDurationValue={this.updateDurationValue}/>
                        </div>


                    </div>

                    <div>
                        <Row>
                            <Col span={18} push={6}>

                                    <Card
                                        bordered={true}
                                        hoverable={true}
                                        style={{borderRadius: 5, marginBottom: 10, marginLeft:10, height:window.innerHeight*0.5}}>


                                        <PieChart/>

                                    </Card>

                            </Col>
                            <Col span={6} pull={18}>

                                    <Card
                                        className="scrollable-container"
                                        bordered={true}
                                        hoverable={true}
                                        style={{borderRadius: 5, marginBottom: 10, width:"100%", height:window.innerHeight*0.5}}>

                                        <CountWidget allCount={this.state.count}/>

                                    </Card>

                            </Col>
                        </Row>
                    </div>

                    <div style={{marginBottom: '10px'}}>
                        <Radio.Group defaultValue="a" buttonStyle="solid" onChange={this.onRadioChange} style={{marginRight: '10px'}}>
                            {radioItems}
                        </Radio.Group>

                    </div>

                    <div style={{backgroundColor:"#ffffff", borderRadius: 5}}>
                        <ReportDeviceTable paramsObject={params} getStats={this.getStats}/>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 720}}>

                </div>
            </div>
        );
    }
}

export default ReportDetails;
