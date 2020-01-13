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
import axios from "axios";
import {Icon, message, notification, Table, Tag, Tooltip, Typography} from "antd";
import TimeAgo from 'javascript-time-ago'
// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en'
import {withConfigContext} from "../../../context/ConfigContext";

const {Text} = Typography;

let config = null;

const columns = [
    {
        title: 'Device',
        dataIndex: 'deviceName',
        width: 100,
    },
    {
        title: 'Owner',
        dataIndex: 'owner',
        key: 'owner'
    },
    {
        title: 'Policy',
        dataIndex: 'policyId',
        key: 'policy'
    },
    {
        title: 'Last Requested Time',
        dataIndex: 'lastRequestedTime',
        key: 'lastRequestedTime'
    },
    {
        title: 'Attempts',
        dataIndex: 'attempts',
        key: 'attempts'
    }
];

const getTimeAgo = (time) => {
    const timeAgo = new TimeAgo('en-US');
    return timeAgo.format(time);
};


class PolicyDevicesTable extends React.Component {
    constructor(props) {
        super(props);
        config =  this.props.context;
        TimeAgo.addLocale(en);
        this.state = {
            data: [],
            pagination: {},
            loading: false,
            selectedRows: [],
            paramsObj:{}
        };
    }

    rowSelection = {
        onChange: (selectedRowKeys, selectedRows) => {
            this.setState({
                selectedRows: selectedRows
            })
        }
    };

    componentDidMount() {
        this.fetchData();
    }

    //Rerender component when parameters change
    componentDidUpdate(prevProps, prevState, snapshot) {
        if(prevProps.isCompliant !== this.props.isCompliant){
            this.fetchData();
        }
    }


    //fetch data from api
    fetchData = (params = {}) => {
        const config = this.props.context;
        // const policyReportData = this.props;
        this.setState({loading: true});
        // get current page
        const currentPage = (params.hasOwnProperty("page")) ? params.page : 1;

        const extraParams = {
            from: this.props.policyReportData.from,
            to: this.props.policyReportData.to,
            offset: 10 * (currentPage - 1), //calculate the offset
            limit: 10
        };

        const encodedExtraParams = Object.keys(extraParams)
            .map(key => key + '=' + extraParams[key]).join('&');

        let apiUrl;
        
        if(this.props.isCompliant){
            apiUrl = window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt +
                "/devices/compliance/true?" + encodedExtraParams;
        }else {
            apiUrl = window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt +
                "/devices/compliance/false?" + encodedExtraParams;
        }

        //send request to the invoker
        axios.get(apiUrl).then(res => {
            if (res.status === 200) {
                const pagination = {...this.state.pagination};
                this.setState({
                    loading: false,
                    data: res.data.data,
                    pagination
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = window.location.origin + '/entgra/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load devices.",
                });
            }

            this.setState({loading: false});
        });
    };

    handleTableChange = (pagination, filters, sorter) => {
        const pager = {...this.state.pagination};
        pager.current = pagination.current;
        this.setState({
            pagination: pager,
        });
        this.fetchData({
            results: pagination.pageSize,
            page: pagination.current,
            sortField: sorter.field,
            sortOrder: sorter.order,
            ...filters,
        });
    };

    render() {

        const {data, pagination, loading, selectedRows} = this.state;
        return (
            <div>
                <Table
                    columns={columns}
                    rowKey={record => (record.id)}
                    dataSource={data.complianceData}
                    pagination={{
                        ...pagination,
                        size: "small",
                        // position: "top",
                        total: data.count,
                        showTotal: (total, range) => `showing ${range[0]}-${range[1]} of ${total} devices`
                        // showQuickJumper: true
                    }}
                    loading={loading}
                    onChange={this.handleTableChange}
                    rowSelection={this.rowSelection}
                />
            </div>
        );
    }
}

export default withConfigContext(PolicyDevicesTable);