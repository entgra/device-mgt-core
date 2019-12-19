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
import {Button, Form, Input, message, Modal, notification, Select, Typography, DatePicker} from "antd";
import axios from "axios";
import {withConfigContext} from "../../context/ConfigContext";
import moment from 'moment';

const {Text} = Typography;
let config = null;

const { RangePicker } = DatePicker;

class ReportFilterModal extends React.Component {

    constructor(props) {
        super(props);
        config =  this.props.context;
        this.state = {
            addModalVisible: false,
            name:'',
            description:'',
            deviceTypes: [{id:1,name:"android"},{id:2,name:"IOS"},{id:3,name:"Windows"}]
        }
    }

    openAddModal = () => {
        this.setState({
            addModalVisible:true
        })
    };

    handleAddOk = e => {
        this.props.form.validateFields(err => {
            if (!err) {
                this.setState({
                    addModalVisible: false,
                });
            }
        });
    };

    handleAddCancel = e => {
        this.setState({
            addModalVisible: false,
        });
    };


    handleChange = (value) => {
        console.log(`selected ${value}`);
    }

    onChange = (dates, dateStrings) => {
        this.props.updateDurationValue(dateStrings[0],dateStrings[1]);
    }

    render() {
        const { getFieldDecorator } = this.props.form;

        let deviceTypes = this.state.deviceTypes.map((data) =>
            <Option value={data.name} key={data.id}>{data.name}</Option>
        );
        return(
            <div>
                <div>
                    <Button type="primary" icon="plus" size={"default"} onClick={this.openAddModal}>
                        Set Parameters
                    </Button>
                </div>
                <div>
                    <Modal
                        title="GENERATE REPORT"
                        width="45%"
                        visible={this.state.addModalVisible}
                        onOk={this.handleAddOk}
                        onCancel={this.handleAddCancel}
                        footer={[
                            <Button key="cancel" onClick={this.handleAddCancel}>
                                Cancel
                            </Button>,
                            <Button key="submit" type="primary" onClick={this.handleAddOk}>
                                Submit
                            </Button>,
                        ]}
                    >
                        <div style={{alignContent:"center"}}>

                                <p>Set parameters to generate report</p>
                                <Select mode="tags" style={{ width: '100%',margin:'10px' , display:'block'}} placeholder="Device Type" onChange={this.handleChange}>
                                    {deviceTypes}
                                </Select>



                                <RangePicker
                                    style={{ width: '100%' , margin:'10px', display:'block'}}
                                    ranges={{
                                        'Today': [
                                            moment(),
                                            moment()],
                                        'Yesterday': [
                                            moment().subtract(1, 'days'),
                                            moment().subtract(1, 'days')],
                                        'Last 7 Days': [
                                            moment().subtract(6, 'days'),
                                            moment()],
                                        'Last 30 Days': [
                                            moment().subtract(29, 'days'),
                                            moment()],
                                        'This Month': [
                                            moment().startOf('month'),
                                            moment().endOf('month')],
                                        'Last Month': [
                                            moment().subtract(1, 'month').startOf('month'),
                                            moment().subtract(1, 'month').endOf('month')]
                                    }}
                                    format="YYYY-MM-DD"
                                    onChange={this.onChange}
                                />


                        </div>
                    </Modal>
                </div>
            </div>
        )
    }
}

export default withConfigContext(Form.create({name: 'report-filter'})(ReportFilterModal));