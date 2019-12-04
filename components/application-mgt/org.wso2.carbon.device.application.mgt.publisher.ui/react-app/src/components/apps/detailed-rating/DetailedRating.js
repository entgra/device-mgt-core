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
import {Row, Typography, Icon, notification} from "antd";
import StarRatings from "react-star-ratings";
import "./DetailedRating.css";
import axios from "axios";
import {withConfigContext} from "../../../context/ConfigContext";
import {handleApiError} from "../../../js/Utils";

const { Text } = Typography;


class DetailedRating extends React.Component{

    constructor(props){
        super(props);
        this.state={
            detailedRating: null
        }
    }

    componentDidMount() {
        const {type,uuid} = this.props;
        this.getData(type,uuid);
    }

    componentDidUpdate(prevProps, prevState) {
        if (prevProps.uuid !== this.props.uuid) {
            const {type,uuid} = this.props;
            this.getData(type,uuid);
        }
    }

    getData = (type, uuid)=>{
        const config = this.props.context;
        return axios.get(
            window.location.origin+ config.serverConfig.invoker.uri +config.serverConfig.invoker.publisher+"/admin/reviews/"+uuid+"/"+type+"-rating",
            ).then(res => {
            if (res.status === 200) {
                let detailedRating = res.data.data;
                this.setState({
                    detailedRating
                })
            }

        }).catch(function (error) {
            handleApiError(error, "Error occurred while trying to load rating for the release.", true);
        });
    };

    render() {
        const detailedRating = this.state.detailedRating;


        if(detailedRating ==null){
            return null;
        }

        const totalCount = detailedRating.noOfUsers;
        const ratingVariety = detailedRating.ratingVariety;

        const ratingArray = [];

        for (let [key, value] of Object.entries(ratingVariety)) {
            ratingArray.push(value);
        }

        const maximumRating = Math.max(...ratingArray);

        const ratingBarPercentages = [0,0,0,0,0];

        if(maximumRating>0){
            for(let i = 0; i<5; i++){
                ratingBarPercentages[i] = (ratingVariety[(i+1).toString()])/maximumRating*100;
            }
        }


        return (
            <Row className="d-rating">
                <div className="numeric-data">
                    <div className="rate">{detailedRating.ratingValue.toFixed(1)}</div>
                    <StarRatings
                        rating={detailedRating.ratingValue}
                        starRatedColor="#777"
                        starDimension = "16px"
                        starSpacing = "2px"
                        numberOfStars={5}
                        name='rating'
                    />
                    <br/>
                    <Text type="secondary" className="people-count"><Icon type="team" /> {totalCount} total</Text>
                </div>
                <div className="bar-containers">
                    <div className="bar-container">
                        <span className="number">5</span>
                        <span className="bar rate-5" style={{width: ratingBarPercentages[4]+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">4</span>
                        <span className="bar rate-4" style={{width: ratingBarPercentages[3]+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">3</span>
                        <span className="bar rate-3" style={{width: ratingBarPercentages[2]+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">2</span>
                        <span className="bar rate-2" style={{width: ratingBarPercentages[1]+"%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">1</span>
                        <span className="bar rate-1" style={{width: ratingBarPercentages[0]+"%"}}> </span>
                    </div>
                </div>
            </Row>
        );
    }
}


export default withConfigContext(DetailedRating);