/*
 * Copyright (c) 2018 - 2023, Entgra (Pvt) Ltd. (http://www.entgra.io) All Rights Reserved.
 *
 * Entgra (Pvt) Ltd. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.entgra.device.mgt.core.device.mgt.core.report.mgt.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents the Report Management Configuration.
 */
@XmlRootElement(name = "ReportManagementConfiguration")
public class ReportMgtConfiguration {

    private String datasourceName;
    private int threadPoolSize;
    private int maxConnections;
    private int MaxConnectionsPerRoute;

    @XmlElement(name = "DatasourceName", required = true)
    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    @XmlElement(name = "ThreadPoolSize", required = true)
    public int getThreadPoolSize(){
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize){
        this.threadPoolSize = threadPoolSize;
    }

    @XmlElement(name = "MaxConnections", required = true)
    public int getMaxConnections(){
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections){
        this.maxConnections = maxConnections;
    }

    @XmlElement(name = "MaxConnectionsPerRoute", required = true)
    public  int getMaxConnectionsPerRoute(){
        return MaxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute){
        this.MaxConnectionsPerRoute = maxConnectionsPerRoute;
    }

}

