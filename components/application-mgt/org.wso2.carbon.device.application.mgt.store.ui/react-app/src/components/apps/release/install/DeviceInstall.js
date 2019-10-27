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
import {Button, message, DatePicker, Table, Typography, Alert} from "antd";
import TimeAgo from 'javascript-time-ago'

// Load locale-specific relative date/time formatting rules.
import en from 'javascript-time-ago/locale/en'
import {withConfigContext} from "../../../../context/ConfigContext";
import {handleApiError} from "../../../../js/Utils";
import InstallModalFooter from "./installModalFooter/InstallModalFooter";

const {Text} = Typography;
const columns = [
    {
        title: 'Device',
        dataIndex: 'name',
        fixed: 'left',
        width: 100,
    },
    {
        title: 'Modal',
        dataIndex: 'deviceInfo',
        key: 'modal',
        render: deviceInfo => `${deviceInfo.vendor} ${deviceInfo.deviceModel}`
        // todo add filtering options
    },
    {
        title: 'Owner',
        dataIndex: 'enrolmentInfo',
        key: 'owner',
        render: enrolmentInfo => enrolmentInfo.owner
        // todo add filtering options
    },
    {
        title: 'Last Updated',
        dataIndex: 'enrolmentInfo',
        key: 'dateOfLastUpdate',
        render: (data) => {
            return (getTimeAgo(data.dateOfLastUpdate));
        }
        // todo add filtering options
    },
    {
        title: 'Status',
        dataIndex: 'enrolmentInfo',
        key: 'status',
        render: enrolmentInfo => enrolmentInfo.status
        // todo add filtering options
    },
    {
        title: 'Ownership',
        dataIndex: 'enrolmentInfo',
        key: 'ownership',
        render: enrolmentInfo => enrolmentInfo.ownership
        // todo add filtering options
    },
    {
        title: 'OS Version',
        dataIndex: 'deviceInfo',
        key: 'osVersion',
        render: deviceInfo => deviceInfo.osVersion
        // todo add filtering options
    },
    {
        title: 'IMEI',
        dataIndex: 'properties',
        key: 'imei',
        render: properties => {
            let imei = "not-found";
            for (let i = 0; i < properties.length; i++) {
                if (properties[i].name === "IMEI") {
                    imei = properties[i].value;
                }
            }
            return imei;
        }
        // todo add filtering options
    },
];

const getTimeAgo = (time) => {
    const timeAgo = new TimeAgo('en-US');
    return timeAgo.format(time);
};


class DeviceInstall extends React.Component {
    constructor(props) {
        super(props);
        TimeAgo.addLocale(en);
        this.state = {
            data: [],
            pagination: {},
            loading: false,
            selectedRows: [],
            scheduledTime: null,
            isScheduledInstallVisible: false,
            isForbidden: false
        };
    }

    rowSelection = {
        onChange: (selectedRowKeys, selectedRows) => {
            this.setState({
                selectedRows: selectedRows
            })
        },
        getCheckboxProps: record => ({
            disabled: record.name === 'Disabled User', // Column configuration not to be checked
            name: record.name,
        }),
    };

    componentDidMount() {
        this.fetch();
    }

    //fetch data from api
    fetch = (params = {}) => {
        const config = this.props.context;
        this.setState({loading: true});
        const {deviceType} = this.props;
        // get current page
        const currentPage = (params.hasOwnProperty("page")) ? params.page : 1;

        const extraParams = {
            offset: 10 * (currentPage - 1), //calculate the offset
            limit: 10,
            status: "ACTIVE",
            requireDeviceInfo: true,
        };

        if (deviceType !== 'ANY') {
            extraParams.type = deviceType;
        }

        // note: encode with '%26' not '&'
        const encodedExtraParams = Object.keys(extraParams).map(key => key + '=' + extraParams[key]).join('&');

        //send request to the invoker
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt +
            "/devices?" + encodedExtraParams,
        ).then(res => {
            if (res.status === 200) {
                const pagination = {...this.state.pagination};
                this.setState({
                    loading: false,
                    data: res.data.data.devices,
                    pagination,
                });
            }

        }).catch((error) => {
            handleApiError(error, "Error occurred while trying to load devices.", true);
            if (error.hasOwnProperty("response") && error.response.status === 403) {
                this.setState({
                    isForbidden: true,
                    loading: false
                })
            } else {
                this.setState({
                    loading: false
                });
            }
        });
    };

    handleTableChange = (pagination, filters, sorter) => {
        const pager = {...this.state.pagination};
        pager.current = pagination.current;
        this.setState({
            pagination: pager,
        });
        this.fetch({
            results: pagination.pageSize,
            page: pagination.current,
            sortField: sorter.field,
            sortOrder: sorter.order,
            ...filters,
        });
    };

    install = (timestamp=null) => {
        const {selectedRows} = this.state;
        const payload = [];
        selectedRows.map(device => {
            payload.push({
                id: device.deviceIdentifier,
                type: device.type
            });
        });
        this.props.onInstall("devices", payload, "install",timestamp);
    };

    render() {
        const {data, pagination, loading, selectedRows, scheduledTime,isScheduledInstallVisible} = this.state;
        return (
            <div>
                <Text>
                    Start installing the application for one or more users by entering the corresponding user name.
                    Select install to automatically start downloading the application for the respective user/users.
                </Text>
                {(this.state.isForbidden) && (
                    <Alert
                        message="You don't have permission to view devices."
                        type="warning"
                        banner
                        closable/>
                )}
                <Table
                    style={{paddingTop: 20}}
                    columns={columns}
                    rowKey={record => record.deviceIdentifier}
                    dataSource={data}
                    pagination={{
                        ...pagination,
                        size: "small",
                        // position: "top",
                        showTotal: (total, range) => `showing ${range[0]}-${range[1]} of ${total} devices`
                        // showQuickJumper: true
                    }}
                    loading={loading}
                    onChange={this.handleTableChange}
                    rowSelection={this.rowSelection}
                    scroll={{x: 1000}}
                />
                <InstallModalFooter type="Install" operation={this.install} disabled={selectedRows.length === 0}/>
            </div>
        );
    }
}

export default withConfigContext(DeviceInstall);