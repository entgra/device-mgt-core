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
import {Typography, Select, Spin, message, notification, Button, Alert} from "antd";
import debounce from 'lodash.debounce';
import axios from "axios";
import {withConfigContext} from "../../../../context/ConfigContext";
import {handleApiError} from "../../../../js/Utils";
import InstallModalFooter from "./installModalFooter/InstallModalFooter";

const {Text} = Typography;
const {Option} = Select;


class GroupInstall extends React.Component {

    constructor(props) {
        super(props);
        this.lastFetchId = 0;
        this.fetchUser = debounce(this.fetchUser, 800);
    }

    state = {
        data: [],
        value: [],
        fetching: false,
        isForbidden: false
    };

    fetchUser = value => {
        this.lastFetchId += 1;
        const fetchId = this.lastFetchId;
        const config = this.props.context;
        this.setState({data: [], fetching: true});

        axios.get(
            window.location.origin+ config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt+"/groups?name=" + value,

        ).then(res => {
            if (res.status === 200) {
                if (fetchId !== this.lastFetchId) {
                    // for fetch callback order
                    return;
                }

                const data = res.data.data.deviceGroups.map(group => ({
                    text: group.name,
                    value: group.name,
                }));

                this.setState({data, fetching: false});
            }

        }).catch((error) => {
            handleApiError(error,"Error occurred while trying to load groups.", true);
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

    handleChange = value => {
        this.setState({
            value,
            data: [],
            fetching: false,
        });
    };

    install = () =>{
        const {value} = this.state;
        const data = [];
        value.map(val=>{
            data.push(val.key);
        });
        this.props.onInstall("group", data, "install");
    };

    render() {

        const {fetching, data, value} = this.state;

        return (
            <div>
                <Text>Start installing the application for one or more groups by entering the corresponding group name. Select install to automatically start downloading the application for the respective device group/ groups.</Text>
                {(this.state.isForbidden) && (
                    <Alert
                        message="You don't have permission to view groups."
                        type="warning"
                        banner
                        closable/>
                )}
                <br/>
                <br/>
                <Select
                    mode="multiple"
                    labelInValue
                    value={value}
                    placeholder="Search groups"
                    notFoundContent={fetching ? <Spin size="small"/> : null}
                    filterOption={false}
                    onSearch={this.fetchUser}
                    onChange={this.handleChange}
                    style={{width: '100%'}}
                >
                    {data.map(d => (
                        <Option key={d.value}>{d.text}</Option>
                    ))}
                </Select>
                <InstallModalFooter type="Install" operation={this.install} disabled={value.length===0}/>
            </div>
        );
    }
}

export default withConfigContext(GroupInstall);