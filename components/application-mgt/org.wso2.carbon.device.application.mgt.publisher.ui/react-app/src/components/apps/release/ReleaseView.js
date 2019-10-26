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
import {Divider, Row, Col, Typography, Button, Drawer, Icon, Tooltip, Empty} from "antd";
import StarRatings from "react-star-ratings";
import Reviews from "./review/Reviews";
import "../../../App.css";
import DetailedRating from "../detailed-rating/DetailedRating";
import EditRelease from "./edit-release/EditRelease";
import {withConfigContext} from "../../../context/ConfigContext";
import NewAppUploadForm from "../../new-app/subForms/NewAppUploadForm";

const {Title, Text, Paragraph} = Typography;

class ReleaseView extends React.Component {
    constructor(props) {
        super(props);
        this.state = {

        }
    }

    componentDidMount() {
        console.log("mounted: Release view");
    }

    render() {
        const {app, release} = this.props;
        const config = this.props.context;
        const {lifecycle, currentLifecycleStatus} = this.props;

        if (release == null || lifecycle == null) {
            return null;
        }

        const {isAppUpdatable, isAppInstallable} = lifecycle[currentLifecycleStatus];

        const platform = app.deviceType;
        const defaultPlatformIcons = config.defaultPlatformIcons;
        let icon = defaultPlatformIcons.default.icon;
        let color = defaultPlatformIcons.default.color;
        let theme = defaultPlatformIcons.default.theme;

        if (defaultPlatformIcons.hasOwnProperty(platform)) {
            icon = defaultPlatformIcons[platform].icon;
            color = defaultPlatformIcons[platform].color;
            theme = defaultPlatformIcons[platform].theme;
        }
        let metaData = [];
        try{
            metaData = JSON.parse(release.metaData);
        }catch (e) {

        }

        return (
            <div>
                <div className="release">
                    <Row>
                        <Col xl={4} sm={6} xs={8} className="release-icon">
                            <img src={release.iconPath} alt="icon"/>
                        </Col>
                        <Col xl={10} sm={11} className="release-title">
                            <Title level={2}>{app.name}</Title>
                            <StarRatings
                                rating={release.rating}
                                starRatedColor="#777"
                                starDimension="20px"
                                starSpacing="2px"
                                numberOfStars={5}
                                name='rating'
                            />
                            <br/>
                            <Text>Platform : </Text>
                            <span style={{fontSize: 20, color: color, textAlign: "center"}}>
                                <Icon
                                    type={icon}
                                    theme={theme}
                                />
                            </span>
                            <Divider type="vertical"/>
                            <Text>Version : {release.version}</Text><br/>

                            <EditRelease
                                forbiddenErrors={this.props.forbiddenErrors}
                                isAppUpdatable={isAppUpdatable}
                                type={app.type}
                                deviceType={app.deviceType}
                                release={release}
                                updateRelease={this.props.updateRelease}
                                supportedOsVersions={[...this.props.supportedOsVersions]}
                            />

                        </Col>
                        <Col xl={8} md={10} sm={24} xs={24} style={{float: "right"}}>
                            <div>
                                <Tooltip
                                    title={isAppInstallable ? "Open this app in store" : "This release isn't in an installable state"}>
                                    <Button
                                        style={{float: "right"}}
                                        htmlType="button"
                                        type="primary"
                                        icon="shop"
                                        disabled={!isAppInstallable}
                                        onClick={() => {
                                            window.open(window.location.origin + "/store/" + app.deviceType + "/apps/" + release.uuid)
                                        }}>
                                        Open in store
                                    </Button>
                                </Tooltip>
                            </div>
                        </Col>
                    </Row>
                    <Divider/>
                    <Row className="release-images">
                        {release.screenshots.map((screenshotUrl, index) => {
                            return (
                                <div key={index} className="release-screenshot">
                                    <img key={screenshotUrl} src={screenshotUrl}/>
                                </div>
                            )
                        })}
                    </Row>
                    <Divider/>
                    <Paragraph type="secondary" ellipsis={{rows: 3, expandable: true}}>
                        {release.description}
                    </Paragraph>
                    <Divider/>
                    <Text>META DATA</Text>
                        <Row>
                            {
                                metaData.map((data, index)=>{
                                    return (
                                        <Col key={index} lg={8} md={6} xs={24} style={{marginTop:15}}>
                                            <Text>{data.key}</Text><br/>
                                            <Text type="secondary">{data.value}</Text>
                                        </Col>
                                    )
                                })
                            }
                            {(metaData.length===0) && (<Text type="secondary">No meta data available.</Text>)}
                        </Row>
                    <Divider/>
                    <Text>REVIEWS</Text>
                    <Row>
                        <Col lg={18}>
                            <DetailedRating type="release" uuid={release.uuid}/>
                        </Col>
                    </Row>
                    <Reviews type="release" uuid={release.uuid}/>
                </div>
            </div>
        );
    }
}

export default withConfigContext(ReleaseView);
