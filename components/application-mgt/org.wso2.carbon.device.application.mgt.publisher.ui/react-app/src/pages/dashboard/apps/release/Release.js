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
import '../../../../App.css';
import {Typography, Row, Col, message, Card, notification, Skeleton} from "antd";
import axios from 'axios';
import ReleaseView from "../../../../components/apps/release/ReleaseView";
import LifeCycle from "../../../../components/apps/release/lifeCycle/LifeCycle";
import {withConfigContext} from "../../../../context/ConfigContext";
import {handleApiError} from "../../../../js/Utils";
import NewAppUploadForm from "../../../../components/new-app/subForms/NewAppUploadForm";

const {Title} = Typography;

class Release extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;
        this.state = {
            loading: true,
            app: null,
            uuid: null,
            release: null,
            currentLifecycleStatus: null,
            lifecycle: null,
            supportedOsVersions: [],
            forbiddenErrors: {
                supportedOsVersions: false,
                lifeCycle: false
            }
        };
    }

    componentDidMount() {
        const {uuid} = this.props.match.params;
        this.fetchData(uuid);
        this.getLifecycle();

    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevState.uuid !== this.state.uuid) {
            const {uuid} = this.props.match.params;
            this.fetchData(uuid);
        }
    }

    changeCurrentLifecycleStatus = (status) => {
        this.setState({
            currentLifecycleStatus: status
        });
    };

    updateRelease = (release) => {
        this.setState({
            release
        });
    };

    fetchData = (uuid) => {
        const config = this.props.context;

        //send request to the invoker
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/release/" + uuid,
        ).then(res => {
            if (res.status === 200) {
                const app = res.data.data;
                const release = (app !== null) ? app.applicationReleases[0] : null;
                const currentLifecycleStatus = (release !== null) ? release.currentStatus : null;
                this.setState({
                    app: app,
                    release: release,
                    currentLifecycleStatus: currentLifecycleStatus,
                    loading: false,
                    uuid: uuid
                });
                if (config.deviceTypes.mobileTypes.includes(app.deviceType)) {
                    this.getSupportedOsVersions(app.deviceType);
                }
            }

        }).catch((error) => {
            handleApiError(error, "Error occurred while trying to load the release.");
            this.setState({loading: false});
        });
    };

    getLifecycle = () => {
        const config = this.props.context;
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/lifecycle-config"
        ).then(res => {
            if (res.status === 200) {
                const lifecycle = res.data.data;
                this.setState({
                    lifecycle: lifecycle
                })
            }

        }).catch((error) => {
            handleApiError(error, "Error occurred while trying to load lifecycle configuration.", true);
            if (error.hasOwnProperty("response") && error.response.status === 403) {
                const {forbiddenErrors} = this.state;
                forbiddenErrors.lifeCycle = true;
                this.setState({
                    forbiddenErrors
                })
            }
        });
    };

    getSupportedOsVersions = (deviceType) => {
        const config = this.props.context;
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.deviceMgt +
            `/admin/device-types/${deviceType}/versions`
        ).then(res => {
            if (res.status === 200) {
                let supportedOsVersions = JSON.parse(res.data.data);
                this.setState({
                    supportedOsVersions
                });
            }
        }).catch((error) => {
            handleApiError(error, "Error occurred while trying to load supported OS versions.", true);
            if (error.hasOwnProperty("response") && error.response.status === 403) {
                const {forbiddenErrors} = this.state;
                forbiddenErrors.supportedOsVersions = true;
                this.setState({
                    forbiddenErrors,
                    loading: false
                })
            } else {
                this.setState({
                    loading: false
                });
            }
        });
    };

    render() {
        const {app, release, currentLifecycleStatus, lifecycle, loading, forbiddenErrors} = this.state;

        if (release == null && loading === false) {
            return (
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    <Title level={3}>No Apps Found</Title>
                </div>
            );
        }

        //todo remove uppercase
        return (
            <div>
                <div className="main-container">
                    <Row style={{padding: 10}}>
                        <Col lg={16} md={24} style={{padding: 3}}>
                            <Card>
                                <Skeleton loading={loading} avatar={{size: 'large'}} active paragraph={{rows: 18}}>
                                    {(release !== null) && (
                                        <ReleaseView
                                            forbiddenErrors={forbiddenErrors}
                                            app={app}
                                            release={release}
                                            currentLifecycleStatus={currentLifecycleStatus}
                                            lifecycle={lifecycle}
                                            updateRelease={this.updateRelease}
                                            supportedOsVersions={[...this.state.supportedOsVersions]}
                                        />)
                                    }
                                </Skeleton>
                            </Card>
                        </Col>
                        <Col lg={8} md={24} style={{padding: 3}}>
                            <Card lg={8} md={24}>
                                <Skeleton loading={loading} active paragraph={{rows: 8}}>
                                    {(release !== null) && (
                                        <LifeCycle
                                            uuid={release.uuid}
                                            currentStatus={release.currentStatus.toUpperCase()}
                                            changeCurrentLifecycleStatus={this.changeCurrentLifecycleStatus}
                                            lifecycle={lifecycle}
                                        />)
                                    }
                                </Skeleton>
                            </Card>
                        </Col>
                    </Row>
                </div>
            </div>

        );
    }
}

export default withConfigContext(Release);
