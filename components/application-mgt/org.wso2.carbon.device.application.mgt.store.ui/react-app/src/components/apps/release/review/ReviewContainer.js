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
import CurrentUsersReview from "./CurrentUsersReview";
import {Col, Divider, Row, Typography} from "antd";
import DetailedRating from "../DetailedRating";
import Reviews from "./Reviews";
import axios from "axios";
import {handleApiError} from "../../../../js/Utils";
import {withConfigContext} from "../../../../context/ConfigContext";

const {Text} = Typography;

class ReviewContainer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            currentUserReviews: [],
            detailedRating: null,
            forbiddenErrors: {
                currentReview: false,
                reviews: false,
                rating: false
            }
        }
    }

    componentDidMount() {
        this.fetchCurrentUserReviews();
        this.fetchDetailedRating("app", this.props.uuid);
    }

    fetchCurrentUserReviews = () => {
        const {uuid} = this.props;
        const config = this.props.context;

        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.store + "/reviews/app/user/" + uuid,
        ).then(res => {
            if (res.status === 200) {
                const currentUserReviews = res.data.data.data;
                this.setState({currentUserReviews});
            }

        }).catch((error) => {
            handleApiError(error, "Error occurred while trying to get your review.", true);
            if (error.hasOwnProperty("response") && error.response.status === 403) {
                const {forbiddenErrors} = this.state;
                forbiddenErrors.currentReview = true;
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

    deleteCurrentUserReviewCallback = () => {
        this.setState({
            currentUserReviews: []
        });
        this.fetchDetailedRating("app", this.props.uuid);
    };

    fetchDetailedRating = (type, uuid) => {
        const config = this.props.context;

        axios.get(
            window.location.origin + config.serverConfig.invoker.uri + config.serverConfig.invoker.store + "/reviews/" + uuid + "/" + type + "-rating",
        ).then(res => {
            if (res.status === 200) {
                let detailedRating = res.data.data;
                this.setState({
                    detailedRating
                })
            }

        }).catch(function (error) {
            handleApiError(error, "Error occurred while trying to load ratings.", true);
        });
    };

    onUpdateReview = () => {
        this.fetchCurrentUserReviews();
        this.fetchDetailedRating("app", this.props.uuid);
    };

    render() {
        const {uuid} = this.props;
        const {currentUserReviews,detailedRating, forbiddenErrors} = this.state;
        return (
            <div>
                <CurrentUsersReview
                    forbidden={forbiddenErrors.currentReview}
                    uuid={uuid}
                    currentUserReviews={currentUserReviews}
                    onUpdateReview={this.onUpdateReview}
                    deleteCallback={this.deleteCurrentUserReviewCallback}/>
                <Divider dashed={true}/>
                <Text>REVIEWS</Text>
                <Row>
                    <Col lg={18} md={24}>
                        <DetailedRating
                            type="app"
                            detailedRating={detailedRating}/>
                    </Col>
                </Row>
                <Reviews
                    type="app"
                    uuid={uuid}
                    deleteCallback={this.onUpdateReview}/>
            </div>
        )
    }
}

export default withConfigContext(ReviewContainer);