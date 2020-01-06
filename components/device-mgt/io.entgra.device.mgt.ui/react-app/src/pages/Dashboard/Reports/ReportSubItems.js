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

import {Link} from "react-router-dom";
import moment from "moment";


const { Option } = Select;

class ReportSubItems extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            reportParams:{
                reportType:"Device"
            }
        }
    }

    subReports = {
        device:[
            {name:"Enrollments vs Unenrollments", description:"Summary of all enrollments and unenrollments"},
            {name:"Active vs Inactive", description: "Summary of all active and inactive devices"}]
    }


    durationItemArray = [
        {name:"Daily Report", description:"Enrollments of today", duration:[moment().format('YYYY-MM-DD'), moment().add(1, 'days').format('YYYY-MM-DD')]},
        {name:"Weekly Report", description:"Enrollments of last 7 days", duration:[moment().subtract(6, 'days').format('YYYY-MM-DD'), moment().add(1, 'days').format('YYYY-MM-DD')]},
        {name:"Monthly Report", description:"Enrollments of last month", duration:[moment().subtract(29, 'days').format('YYYY-MM-DD'), moment().add(1, 'days').format('YYYY-MM-DD')]}]

    render(){

        let item = this.subReports.device.map((data) =>
            <Col key={data.name} span={6}>
                <Link
                    to={{
                        pathname: "/entgra/reportList",
                        reportData: {
                            subReportType:"enrollments_vs_unenrollments"
                        }
                    }}>
                    <Card key={data.name} bordered={true} hoverable={true} style={{borderRadius: 10, marginBottom: 16}}>

                        <div align='center'>
                            <Icon type="desktop" style={{ fontSize: '25px', color: '#08c' }}/>
                            <h2><b>{data.name}</b></h2>
                            <p>{data.description}</p>
                            {/*<p>{data.duration}</p>*/}
                            {/*<ReportFilterModal/>*/}
                        </div>
                    </Card>
                </Link>
            </Col>
        );
        return(
            <div>
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

export default ReportSubItems;