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
    Col,
    Row, Select,
    Radio, Card,
    Button
} from "antd";
import {withConfigContext} from "../../context/ConfigContext";
import ReportFilterModal from "./ReportFilterModal";
import {Link} from "react-router-dom";

let config = null;

const { Option } = Select;

class ReportTopics extends React.Component {

    constructor(props) {
        super(props);
        config = this.props.context;
        this.state = {
            selectedTab:'all',
            reportsArray:[
                {name:'Summary of enrollments',reportType:'Device', params:['ALL','BYOD','COPE']},
                {name:'Summary of enrollment types',reportType:'Device', params:['ALL','BYOD','COPE']},
                {name:'Summary of device status',reportType:'Device', params:['ALL','BYOD','COPE']}],
            deviceTypes: [{id:1,name:"Android"},{id:2,name:"IOS"},{id:3,name:"Windows"}],
            reportObject:{
                reportType:'Device',
                params:['ALL','BYOD','COPE']
            }
        }
        console.log(this.state.deviceTypes);
    }


    reportsObj = {
        all:[
            {name:'Summary of enrollments',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of enrollment types',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of device status',reportType:'Device', params:['ALL','BYOD','COPE']}],
        device: [
            {name:'Summary of enrollments',reportType:'Device', params:['ALL','ACTIVE','REMOVED'], paramsType:'status'},
            {name:'Summary of enrollment types',reportType:'Device', params:['ALL','BYOD','COPE'], paramsType:'ownership'},
            {name:'Summary of device status',reportType:'Device', params:['ALL','ACTIVE','INACTIVE','REMOVED'], paramsType:'status'}],
        application: [
            {name:'Summary of enrollments',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of enrollment types',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of device status',reportType:'Device', params:['ALL','BYOD','COPE']}],
        policy: [
            {name:'Summary of enrollments',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of enrollment types',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of device status',reportType:'Device', params:['ALL','BYOD','COPE']}],
        configuration: [
            {name:'Summary of enrollments',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of enrollment types',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of device status',reportType:'Device', params:['ALL','BYOD','COPE']}],
        operation: [
            {name:'Summary of enrollments',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of enrollment types',reportType:'Device', params:['ALL','BYOD','COPE']},
            {name:'Summary of device status',reportType:'Device', params:['ALL','BYOD','COPE']}]
    };

    onChange = e => {
        this.setState({
            selectedTab: e.target.value,
            reportsArray: this.reportsObj[e.target.value]
        });
        //console.log(this.reportsObj[e.target.value]);
    };

    handleChange = (value) => {
        console.log(`selected ${value}`);
    }

    render(){

        const config = this.props.context;
        console.log(this.state.deviceTypes);
        let deviceTypes = this.state.deviceTypes.map((data) =>
            <Option value={data.name} key={data.id}>{data.name}</Option>
        );

        let item = this.state.reportsArray.map((data) =>
            <Col key={data.name} span={6}>
                <Link
                    to={{
                        pathname: "/entgra/reportdetails",
                        reportData: {
                            reportType: data.reportType,
                            params: data.params,
                            paramsType: data.paramsType
                        }
                    }}>
                    <Card key={data.name} bordered={true} hoverable={true} style={{borderRadius: 10, marginBottom: 16}}>

                            <div align='center'>
                                <Icon type="desktop" style={{ fontSize: '25px', color: '#08c' }}/>
                                <h2><b>{data.name}</b></h2>
                                <p>{"deviceTypes"}</p>
                                {/*<ReportFilterModal/>*/}
                            </div>
                    </Card>
                </Link>
            </Col>
        );
        return(
            <div>
                <div style={{paddingBottom:'5px'}}>
                    <Radio.Group value={this.state.selectedTab} onChange={this.onChange} style={{ marginBottom: 16 }}>
                        <Radio.Button value="all" key={1}>All</Radio.Button>
                        <Radio.Button value="device" key={2}>Device</Radio.Button>
                        <Radio.Button value="application" key={3}>Application</Radio.Button>
                        <Radio.Button value="policy" key={4}>Policy</Radio.Button>
                        <Radio.Button value="configuration" key={5}>Configuration</Radio.Button>
                        <Radio.Button value="operation" key={6}>Operation</Radio.Button>
                    </Radio.Group>
                </div>
                <div style={{borderRadius: 5}}>
                    {/*<ReportDeviceTable paramsObject={params}/>*/}

                    <Row gutter={16} >
                        {item}
                    </Row>
                </div>
            </div>
        )
    }
}

export default withConfigContext(ReportTopics);