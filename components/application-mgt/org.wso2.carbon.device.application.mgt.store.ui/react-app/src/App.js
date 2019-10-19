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
import "antd/dist/antd.less";
import RouteWithSubRoutes from "./components/RouteWithSubRoutes";
import {
    BrowserRouter as Router,
    Redirect, Switch,
} from 'react-router-dom';
import axios from "axios";
import {Layout, Spin, Result} from "antd";
import ConfigContext from "./context/ConfigContext";

const {Content} = Layout;
const loadingView = (
    <Layout>
        <Content style={{
            padding: '0 0',
            paddingTop: 300,
            backgroundColor: '#fff',
            textAlign: 'center'
        }}>
            <Spin tip="Loading..."/>
        </Content>
    </Layout>
);

const errorView = (
    <Result
        style={{
            paddingTop: 200
        }}
        status="500"
        title="Error occurred while loading the configuration"
        subTitle="Please refresh your browser window"
    />
);

class App extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            loading: true,
            error: false,
            config: {}
        }
    }

    componentDidMount() {
        this.updateFavicon();
        axios.get(
            window.location.origin + "/store/public/conf/config.json",
        ).then(res => {
            const config = res.data;
            this.checkUserLoggedIn(config);
        }).catch((error) => {
            this.setState({
                loading: false,
                error: true
            })
        });
    }

    updateFavicon = () =>{
        const link = document.querySelector("link[rel*='icon']") || document.createElement('link');
        link.type = 'image/x-icon';
        link.rel = 'shortcut icon';
        link.href = window.location.origin+'/devicemgt/public/uuf.unit.favicon/img/favicon.png';
        document.getElementsByTagName('head')[0].appendChild(link);
    };

    checkUserLoggedIn = (config) => {
        axios.post(
            window.location.origin + "/store-ui-request-handler/user",
            "platform=publisher"
        ).then(res => {
            config.user = res.data.data;
            const pageURL = window.location.pathname;
            const lastURLSegment = pageURL.substr(pageURL.lastIndexOf('/') + 1);
            if (lastURLSegment === "login") {
                window.location.href = window.location.origin + `/store/`;
            } else {
                this.setState({
                    loading: false,
                    config: config
                });
            }
        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                const redirectUrl = encodeURI(window.location.href);
                const pageURL = window.location.pathname;
                const lastURLSegment = pageURL.substr(pageURL.lastIndexOf('/') + 1);
                if (lastURLSegment !== "login") {
                    window.location.href = window.location.origin + `/store/login?redirect=${redirectUrl}`;
                } else {
                    this.setState({
                        loading: false,
                        config: config
                    })
                }
            } else {
                this.setState({
                    loading: false,
                    error: true
                })
            }
        });
    };

    render() {
        const {loading, error} = this.state;

        const applicationView = (
            <Router>
                <ConfigContext.Provider value={this.state.config}>
                    <div>
                        <Switch>
                            <Redirect exact from="/store" to="/store/android"/>
                            {this.props.routes.map((route) => (
                                <RouteWithSubRoutes key={route.path} {...route} />
                            ))}
                        </Switch>
                    </div>
                </ConfigContext.Provider>
            </Router>
        );

        return (
            <div>
                {loading && loadingView}
                {!loading && !error && applicationView}
                {error && errorView}
            </div>
        );
    }
}

export default App;
