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
import {Button, Col, Divider, Icon, message, notification, Row, Spin, Tooltip, Typography} from "antd";

import "./Cluster.css";
import axios from "axios";
import {withConfigContext} from "../../../../../context/ConfigContext";

const {Title} = Typography;

class Cluster extends React.Component {

    constructor(props) {
        super(props);

        const {cluster, pageId} = this.props;
        this.originalCluster = Object.assign({}, cluster);
        const {name, products, clusterId, orderInPage} = cluster;
        this.clusterId = clusterId;
        this.pageId = pageId;
        this.orderInPage = orderInPage;
        this.state = {
            name,
            products,
            isSaveable: false,
            loading: false
        }
    }

    handleNameChange = (name) => {
        this.setState({
            name
        });
        if (name !== this.originalCluster.name) {
            this.setState({
                isSaveable: true
            });
        }
    };

    isProductsChanged = (currentProducts) => {
        let isChanged = false;
        const originalProducts = this.originalCluster.products;
        if (currentProducts.length === originalProducts.length) {
            for (let i = 0; i < currentProducts.length; i++) {
                if (currentProducts[i].packageId !== originalProducts[i].packageId) {
                    isChanged = true;
                    break;
                }
            }
        } else {
            isChanged = true;
        }
        return isChanged;
    };

    swapProduct = (index, swapIndex) => {
        const products = [...this.state.products];
        if (swapIndex !== -1 && index < products.length) {
            // swap elements
            [products[index], products[swapIndex]] = [products[swapIndex], products[index]];

            this.setState({
                products,
            });

            this.setState({
                isSaveable: this.isProductsChanged(products)
            })
        }
    };

    removeProduct = (index) => {
        const products = [...this.state.products];
        products.splice(index, 1);
        this.setState({
            products,
            isSaveable: true
        });

    };

    getCurrentCluster = () => {
        const {products, name} = this.state;
        return {
            pageId: this.pageId,
            clusterId: this.clusterId,
            name: name,
            products: products,
            orderInPage: this.orderInPage
        };
    };

    resetChanges = () => {
        const cluster = this.originalCluster;
        const {name, products} = cluster;

        this.setState({
            loading: false,
            name,
            products,
            isSaveable: false
        });
    };

    updateCluster = () => {
        const config = this.props.context;

        const cluster = this.getCurrentCluster();
        this.setState({loading: true});

        axios.put(
            window.location.origin + config.serverConfig.invoker.uri +
            "/device-mgt/android/v1.0/enterprise/store-layout/cluster",
            cluster
        ).then(res => {
            if (res.status === 200) {
                notification["success"]({
                    message: 'Saved!',
                    description: 'Cluster updated successfully!'
                });
                const cluster = res.data.data;
                const {name, products} = cluster;

                this.originalCluster = Object.assign({}, cluster);

                this.setState({
                    loading: false,
                    name,
                    products,
                    isSaveable: false
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
                        "Error occurred while trying to update the cluster.",
                });
            }

            this.setState({loading: false});
        });

    };

    render() {
        const {name, products, loading} = this.state;

        const Product = ({product, index}) => {
            const {iconUrl, packageId} = product;
            return (
                <div className="product">
                    <div className="arrow">
                        <button disabled={index === 0} className="btn"
                                onClick={() => {
                                    this.swapProduct(index, index - 1);
                                }}
                        ><Icon type="caret-left" theme="filled"/></button>
                    </div>
                    <div className="product-icon">
                        <img
                            src={`https://lh3.googleusercontent.com/${iconUrl}=s240-rw`}/>
                        <Tooltip title={packageId}>
                            <div className="title">
                                {packageId}
                            </div>
                        </Tooltip>
                    </div>
                    <div className="arrow">
                        <button
                            disabled={index === products.length - 1}
                            onClick={() => {
                                this.swapProduct(index, index + 1);
                            }} className="btn btn-right"><Icon type="caret-right" theme="filled"/></button>
                    </div>
                    <div className="delete-btn">
                        <button className="btn"
                                onClick={() => {
                                    this.removeProduct(index)
                                }}>
                            <Icon type="close-circle" theme="filled"/>
                        </button>
                    </div>
                </div>
            );
        };


        return (
            <div className="cluster">
                <Spin spinning={loading}>
                    <Title editable={{onChange: this.handleNameChange}} level={4}>{name}</Title>
                    <div className="products-row">
                        <div className="btn-add-new-wrapper">
                            <div className="btn-add-new">
                                <button className="btn">
                                    {/*<div className="btn-bg"></div>*/}
                                    <Icon style={{position: "relative"}} type="plus"/>
                                </button>
                            </div>
                            <div className="title">
                                Add app
                            </div>
                        </div>

                        <div className="product-icon">


                        </div>
                        {
                            products.map((product, index) => {
                                return (
                                    <Product
                                        key={product.packageId}
                                        product={product}
                                        index={index}/>
                                );
                            })
                        }
                    </div>
                    <Row>
                        <Col>
                            {/*<Button>Cancel</Button>*/}
                            {/*<Button>Save</Button>*/}
                            <div>
                                <Button
                                    onClick={this.resetChanges}
                                    disabled={!this.state.isSaveable}>
                                    Cancel
                                </Button>
                                <Divider type="vertical"/>
                                <Button
                                    onClick={this.updateCluster}
                                    htmlType="button" type="primary"
                                    disabled={!this.state.isSaveable}>
                                    Save
                                </Button>

                            </div>
                        </Col>
                    </Row>
                </Spin>
            </div>
        );
    }
}

export default withConfigContext(Cluster);