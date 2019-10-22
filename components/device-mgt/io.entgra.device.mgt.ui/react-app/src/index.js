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

import React from 'react';
import ReactDOM from 'react-dom';
import * as serviceWorker from './serviceWorker';
import App from "./App";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard/Dashboard";
import './index.css';
import Devices from "./pages/Dashboard/Devices/Devices";
import Reports from "./pages/Dashboard/Reports/Reports";
import Geo from "./pages/Dashboard/Geo/Geo";
import Groups from "./pages/Dashboard/Groups/Groups";
import Users from "./pages/Dashboard/Users/Users";
import Policies from "./pages/Dashboard/Policies/Policies";
import Roles from "./pages/Dashboard/Roles/Roles";
import DeviceTypes from "./pages/Dashboard/DeviceTypes/DeviceTypes";

const routes = [
    {
        path: '/entgra/login',
        exact: true,
        component: Login
    },
    {
        path: '/entgra',
        exact: false,
        component: Dashboard,
        routes: [
            {
                path: '/entgra/devices',
                component: Devices,
                exact: true
            },
            {
                path: '/entgra/geo',
                component: Geo,
                exact: true
            },
            {
                path: '/entgra/reports',
                component: Reports,
                exact: true
            },
            {
                path: '/entgra/groups',
                component: Groups,
                exact: true
            },
            {
                path: '/entgra/users',
                component: Users,
                exact: true
            },
            {
                path: '/entgra/policies',
                component: Policies,
                exact: true
            },
            {
                path: '/entgra/roles',
                component: Roles,
                exact: true
            },
            {
                path: '/entgra/devicetypes',
                component: DeviceTypes,
                exact: true
            }
        ]
    }
];


ReactDOM.render(
    <App routes={routes}/>,
    document.getElementById('root'));

// If you want your app e and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
