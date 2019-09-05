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
import {PageHeader, Typography, Breadcrumb, Divider, Button, Icon, Col, Row, notification, message, Spin} from "antd";
import {Link, withRouter} from "react-router-dom";
import {withConfigContext} from "../../../../../context/ConfigContext";
import axios from "axios";
import Cluster from "../../../../../components/manage/android-enterprise/Pages/Cluster/Cluster";

const {Title} = Typography;

class Page extends React.Component {
    routes;

    constructor(props) {
        super(props);
        const {pageName, pageId} = this.props.match.params;
        this.pageId = pageId;
        this.routes = props.routes;
        this.config = this.props.context;
        this.state = {
            pageName,
            clusters: [],
            loading: false,
            applications: [],
            isAddNewClusterVisible: false
        };
    }

    componentDidMount() {
        this.fetchClusters();
        this.fetchApplications();
    }

    removeLoadedCluster = (clusterId) => {
        const clusters = [...this.state.clusters];
        let index = -1;
        for (let i = 0; i < clusters.length; i++) {
            if (clusters[i].clusterId === clusterId) {
                index = i;
                break;
            }
        }
        clusters.splice(index, 1);
    };

    updatePageName = pageName => {
        const config = this.props.context;
        if (pageName !== this.state.pageName && pageName !== "") {
            const data = {
                locale: "en",
                pageName: pageName,
                pageId: this.pageId
            };
            axios.put(
                window.location.origin + config.serverConfig.invoker.uri +
                "/device-mgt/android/v1.0/enterprise/store-layout/page",
                data
            ).then(res => {
                if (res.status === 200) {
                    notification["success"]({
                        message: 'Saved!',
                        description: 'Page name updated successfully!'
                    });
                    this.setState({
                        loading: false,
                        pageName: res.data.data.pageName,
                    });

                    this.props.history.push(`/publisher/manage/android-enterprise/pages/${pageName}/${this.pageId}`);

                }
            }).catch((error) => {
                if (error.hasOwnProperty("response") && error.response.status === 401) {
                    message.error('You are not logged in');
                    window.location.href = window.location.origin + '/publisher/login';
                } else {
                    notification["error"]({
                        message: "There was a problem",
                        duration: 0,
                        description:
                            "Error occurred while trying to save the page name.",
                    });
                }

                this.setState({loading: false});
            });
        }
    };

    fetchClusters = () => {
        const config = this.props.context;
        axios.get(
            window.location.origin + config.serverConfig.invoker.uri +
            `/device-mgt/android/v1.0/enterprise/store-layout/page/${this.pageId}/clusters`
        ).then(res => {
            if (res.status === 200) {
                let clusters = JSON.parse(res.data.data);

                // sort according to the orderInPage value
                clusters.sort((a, b) => (a.orderInPage > b.orderInPage) ? 1 : -1);

                this.setState({
                    clusters,
                    loading: false
                });
            }
        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = window.location.origin + '/publisher/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load clusters.",
                });
            }
            this.setState({
                loading: false
            });
        });
    };

    //fetch applications
    fetchApplications = () => {
        const config = this.props.context;
        this.setState({loading: true});

        const filters = {
            appType: "PUBLIC",
            deviceType: "android"
        };

        //send request to the invoker
        axios.post(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications",
            filters
        ).then(res => {
            if (res.status === 200) {
                const applications = res.data.data.applications.map(application => {
                    const release = application.applicationReleases[0];
                    return {
                        packageId: `app:${application.packageName}`,
                        iconUrl: release.iconPath,
                        name: application.name
                    }
                });

                console.log(applications);
                this.setState({
                    loading: false,
                    applications,
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                message.error('You are not logged in');
                window.location.href = window.location.origin + '/publisher/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to load pages.",
                });
            }

            this.setState({loading: false});
        });
    };

    toggleAddNewClusterVisibility = (isAddNewClusterVisible) => {
        this.setState({
            isAddNewClusterVisible
        });
    };

    addSavedClusterToThePage = (cluster) => {
        this.setState({
            clusters: [...this.state.clusters, cluster],
            isAddNewClusterVisible: false
        });
        window.scrollTo(0,document.body.scrollHeight);
    };

    render() {
        const {pageName, loading, clusters, applications, isAddNewClusterVisible} = this.state;
        return (
            <div>
                <PageHeader style={{paddingTop: 0}}>
                    <Breadcrumb style={{paddingBottom: 16}}>
                        <Breadcrumb.Item>
                            <Link to="/publisher/apps"><Icon type="home"/> Home</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>
                            Manage
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>
                            <Link to="/publisher/manage/android-enterprise">Android Enterprise</Link>
                        </Breadcrumb.Item>
                        <Breadcrumb.Item>Manage Page</Breadcrumb.Item>
                    </Breadcrumb>
                    <div className="wrap">
                        <h3>Manage Android Enterprise</h3>
                        {/*<Paragraph>Lorem ipsum</Paragraph>*/}
                    </div>
                </PageHeader>
                <Spin spinning={loading}>
                    <div style={{background: '#f0f2f5', padding: 24, minHeight: 720}}>
                        <Row>
                            <Col md={8} sm={18} xs={24}>
                                <Title editable={{onChange: this.updatePageName}} level={2}>{pageName}</Title>
                            </Col>
                        </Row>
                        <div hidden={isAddNewClusterVisible} style={{textAlign: "center"}}>
                            <Button
                                type="dashed"
                                shape="round"
                                icon="plus"
                                size="large"
                                onClick={() => {
                                    this.toggleAddNewClusterVisibility(true);
                                }}
                            >Add new cluster</Button>
                        </div>
                        <div hidden={!isAddNewClusterVisible}>
                            <Cluster
                                cluster={{
                                    clusterId: 0,
                                    name: "New Cluster",
                                    products: []
                                }}
                                orderInPage={clusters.length}
                                isTemporary={true}
                                pageId={this.pageId}
                                applications={applications}
                                addSavedClusterToThePage = {this.addSavedClusterToThePage}
                                toggleAddNewClusterVisibility={this.toggleAddNewClusterVisibility}/>
                        </div>

                        {
                            clusters.map((cluster) => {
                                return (
                                    <Cluster
                                        key={cluster.clusterId}
                                        orderInPage={cluster.orderInPage}
                                        isTemporary={false}
                                        cluster={cluster}
                                        pageId={this.pageId}
                                        applications={applications}
                                        removeLoadedCluster={this.removeLoadedCluster}/>
                                );
                            })
                        }
                    </div>
                </Spin>
            </div>

        );
    }
}

export default withConfigContext(withRouter(Page));
