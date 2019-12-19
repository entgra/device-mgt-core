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
import {Card, Col, Icon} from 'antd';

class ReportGadget extends React.Component {

    constructor(props){
        super(props);
    }

    render(){
        return(
            <Col span={6}>
                <Card key={this.props.reportName} bordered={true} hoverable={true} style={{borderRadius: 10, marginBottom: 16}}>
                    <div align='center'>
                        <Icon type="desktop" style={{ fontSize: '25px', color: '#08c' }}/>
                        <h2><b>{this.props.reportName}</b></h2>
                        <p>Specific language governing permissions and limitations</p>
                    </div>
                </Card>
            </Col>
        )
    }
}

export default ReportGadget;