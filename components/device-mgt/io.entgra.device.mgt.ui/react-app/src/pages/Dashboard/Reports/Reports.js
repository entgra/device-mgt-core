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
    Card
} from "antd";
import {Link} from "react-router-dom";
import ReportDeviceTable from "../../../components/Devices/ReportDevicesTable";
import DurationDropDown from "../../../components/Reports/DurationDropDown";
import FiltersDropDown from "../../../components/Reports/FiltersDropDown";

const {Paragraph} = Typography;

class Reports extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state = {
            durationFromDate: '',
            durationToDate:'',
            filterDropdownStatusValue: 'ALL',
            filterDropdownOwnershipValue:'ALL'
        }
    }

    updateDurationValue = (modifiedFromDate,modifiedToDate) => {
        this.setState({durationFromDate: modifiedFromDate, durationToDate:modifiedToDate});

    }

    updateFiltersValue = (modifiedValue,filterType) => {

            if(filterType=="Device Status"){
                this.setState({filterDropdownStatusValue: modifiedValue});
                if(modifiedValue=="ALL"){
                    this.setState({filterDropdownStatusValue: 'ALL'});
                }
                //console.log("asdsdf"+modifiedValue);
            }else{
                this.setState({filterDropdownOwnershipValue:modifiedValue});
                if(modifiedValue=="ALL"){
                    this.setState({filterDropdownOwnershipValue: 'ALL'});
                }
            }


    }

    render() {
        var reportParams;
        if(this.state.durationFromDate==""){
            reportParams = {
                //   duration: this.state.durationDropdownValue,
                from: null
            }
        }else if(this.state.filterDropdownStatusValue=="ALL" && this.state.filterDropdownOwnershipValue=="ALL"){
            reportParams = {
                //   duration: this.state.durationDropdownValue,
                from:this.state.durationFromDate,
                to:this.state.durationToDate
            }
        }else if(this.state.filterDropdownStatusValue!="ALL" && this.state.filterDropdownOwnershipValue=="ALL"){
            reportParams = {
                status: this.state.filterDropdownStatusValue,
                from:this.state.durationFromDate,
                to:this.state.durationToDate
            }
        }else if(this.state.filterDropdownOwnershipValue!="ALL" && this.state.filterDropdownStatusValue=="ALL"){
            reportParams = {
                ownership: this.state.filterDropdownOwnershipValue,
                from:this.state.durationFromDate,
                to:this.state.durationToDate
            }
        }else{
            reportParams = {
                status: this.state.filterDropdownStatusValue,
                ownership: this.state.filterDropdownOwnershipValue,
                from:this.state.durationFromDate,
                to:this.state.durationToDate
            }
        }
        const statusObj = [
            {
                id: '1',
                item: 'ALL'
            },
            {
                id: '2',
                item: 'ACTIVE'
            },
            {
                id: '3',
                item: 'INACTIVE'
            },
            {
                id: '4',
                item: 'REMOVED'
            }
        ];
        const ownershipObj = [
            {
                id: '1',
                item: 'ALL'
            },
            {
                id: '2',
                item: 'BYOD'
            },
            {
                id: '3',
                item: 'COPE'
            }
        ];
        return (
            <div>
                <PageHeader style={{paddingTop: 0}}>
                    <Breadcrumb style={{paddingBottom: 16}}>
                        <Breadcrumb.Item>
                            <Link to="/entgra"><Icon type="home"/> Home</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>Reports</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap">
                        <h3>Reports</h3>
                        <Paragraph>Lorem ipsum dolor sit amet, est similique constituto at, quot inermis id mel, an
                            illud incorrupte nam.</Paragraph>
                        <DurationDropDown updateDurationValue={this.updateDurationValue}/>
                        <FiltersDropDown updateFiltersValue={this.updateFiltersValue} dropDownItems={statusObj} dropDownName={"Device Status"}/>
                        <FiltersDropDown updateFiltersValue={this.updateFiltersValue} dropDownItems={ownershipObj} dropDownName={"Device Ownership"}/>
                        <ReportDeviceTable reportParams={reportParams}/>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 720}}>

                </div>
            </div>
        );
    }
}

export default Reports;
