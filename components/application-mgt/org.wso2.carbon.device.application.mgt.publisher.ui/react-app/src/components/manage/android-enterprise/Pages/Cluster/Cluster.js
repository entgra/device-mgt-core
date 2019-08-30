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
import {Col, Row, Typography, Icon} from "antd";

import "./Cluster.css";

const {Title} = Typography;

const Product = ({product}) => {
    console.log(product);
    return (
        <div className="product">
            <div className="arrow">
                <button className="btn"><Icon type="caret-left" theme="filled"/></button>
            </div>
            <div className="product-icon">
                <img
                    src={`https://lh3.googleusercontent.com/${product.iconUrl}=s240-rw`}/>
                <div className="title">
                    Slack
                </div>
            </div>
            <div className="arrow">
                <button className="btn btn-right"><Icon type="caret-right" theme="filled"/></button>
            </div>
            <div className="delete-btn">
                <button className="btn"><Icon type="close-circle" theme="filled"/></button>
            </div>
        </div>
    );
};

class Cluster extends React.Component {

    render() {
        const {cluster} = this.props;
        const {name, products} = cluster;
        return (
            <div className="cluster">
                <Title level={4}>{name}</Title>
                {/*<Row>*/}
                <div className="products-row">
                    <div className="btn-add-new-wrapper">
                        <div className="btn-add-new">
                            <button className="btn">
                                {/*<div className="btn-bg"></div>*/}
                                <Icon style={{position: "relative"}} class="add-icon" type="plus"/>
                            </button>
                        </div>
                        <div className="title">
                            Add app
                        </div>
                    </div>

                    <div className="product-icon">


                    </div>
                    {
                        products.map((product) => {
                            return (
                                <Product key={product.packageId} product={product}/>
                            );
                        })
                    }
                </div>
                {/*</Row>*/}
            </div>
        );
    }
}

export default Cluster;