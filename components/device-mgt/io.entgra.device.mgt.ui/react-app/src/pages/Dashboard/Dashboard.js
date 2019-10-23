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
import {Layout, Menu, Icon} from 'antd';
import {Switch, Link} from "react-router-dom";
import RouteWithSubRoutes from "../../components/RouteWithSubRoutes"
import {Redirect} from 'react-router'
import "./Dashboard.css";
import {withConfigContext} from "../../context/ConfigContext";
import Logout from "./Logout/Logout";

const {Header, Content, Footer, Sider} = Layout;
const {SubMenu} = Menu;

class Dashboard extends React.Component {
    constructor(props) {
        super(props);

        const mobileWidth = (window.innerWidth<=768 ? '0' : '80');

        this.state = {
            routes: props.routes,
            selectedKeys: [],
            deviceTypes: [],
            isNavBarCollapsed: false,
            mobileWidth
        };
        this.logo = this.props.context.theme.logo;
        this.config = this.props.context;
    }

    toggle = () => {
        console.log(this.config)
        this.setState({
            isNavBarCollapsed: !this.state.isNavBarCollapsed,
        });
    };

    render() {
        return (
            <div>
                <Layout className="layout" >

                    <Sider
                        trigger={null}
                        collapsible
                        collapsed={this.state.isNavBarCollapsed}
                        collapsedWidth={this.state.mobileWidth}
                    >

                        <div className="logo-image">
                            <Link to="/entgra/devices"><img alt="logo" src={this.logo}/></Link>
                        </div>
                        <Menu theme="dark" mode="inline" defaultSelectedKeys={['devices']}>
                           <Menu.Item key="devices">
                                <Link to="/entgra/devices">
                                    <Icon type="appstore"/>
                                    <span>Devices</span>
                                </Link>
                            </Menu.Item>
                            <SubMenu
                                    key="geo"
                                    title={
                                        <span>
                                        <Icon type="environment"/>
                                        <span>Geo</span>
                                        </span>}
                            >
                                <Menu.Item key="singleDevice">
                                    <Link to="/entgra/geo">
                                    <span>Single Device View</span>
                                    </Link>
                                </Menu.Item>
                                <Menu.Item key="deviceGroup">
                                    <Link to="#">
                                        <span>Device Group View</span>
                                    </Link>
                                </Menu.Item>
                            </SubMenu>
                            <Menu.Item key="reports">
                                <Link to="/entgra/reports">
                                    <Icon type="bar-chart"/>
                                    <span>Reports</span>
                                </Link>
                            </Menu.Item>
                            <Menu.Item key="groups">
                                <Link to="/entgra/groups">
                                    <Icon type="deployment-unit"/>
                                    <span>Groups</span>
                                </Link>
                            </Menu.Item>
                            <Menu.Item key="users">
                                <Link to="/entgra/users">
                                    <Icon type="user"/>
                                    <span>Users</span>
                                </Link>
                            </Menu.Item>
                            <Menu.Item key="policies">
                                <Link to="/entgra/policies">
                                    <Icon type="audit"/>
                                    <span>Policies</span>
                                </Link>
                            </Menu.Item>
                            <Menu.Item key="roles">
                                <Link to="/entgra/roles">
                                    <Icon type="book"/>
                                    <span>Roles</span>
                                </Link>
                            </Menu.Item>
                            <Menu.Item key="devicetypes">
                                <Link to="/entgra/devicetypes">
                                    <Icon type="desktop"/>
                                    <span>Device Types</span>
                                </Link>
                            </Menu.Item>
                        </Menu>

                    </Sider>

                    <Layout>
                        <Header style={{background: '#fff', padding: 0}}>
                            <div className="trigger">
                            <Icon
                                type={this.state.isNavBarCollapsed ? 'menu-unfold' : 'menu-fold'}
                                onClick={this.toggle}
                            />
                            </div>

                            <Menu
                                theme="light"
                                mode="horizontal"
                                style={{lineHeight: '64px'}}
                            >
                                <Menu.Item key="trigger">
                                </Menu.Item>
                                <SubMenu className="profile"
                                         title={
                                             <span className="submenu-title-wrapper">
                                     <Icon type="user"/>
                                                 {this.config.user}
                                     </span> }>
                                    <Logout/>
                                </SubMenu>

                            </Menu>
                        </Header>

                        <Content style={{marginTop: 2}}>
                            <Switch>
                                <Redirect exact from="/entgra" to="/entgra/devices"/>
                                {this.state.routes.map((route) => (
                                    <RouteWithSubRoutes key={route.path} {...route} />
                                ))}
                            </Switch>
                        </Content>

                        <Footer style={{textAlign: 'center'}}>
                            ©2019 entgra.io
                        </Footer>

                    </Layout>
                </Layout>
            </div>
        );
    }
}

export default withConfigContext(Dashboard);
