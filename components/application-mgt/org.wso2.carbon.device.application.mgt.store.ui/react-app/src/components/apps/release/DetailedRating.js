import React from "react";
import {Row, Col, Typography, Icon} from "antd";
import StarRatings from "react-star-ratings";
import "./DetailedRating.css";

const { Text } = Typography;

class DetailedRating extends React.Component{
    render() {
        return (
            <Row className="d-rating">
                <div className="numeric-data">
                    <div className="rate">4.1</div>
                    <StarRatings
                        rating={4.1}
                        starRatedColor="#777"
                        starDimension = "16px"
                        starSpacing = "2px"
                        numberOfStars={5}
                        name='rating'
                    />
                    <br/>
                    <Text type="secondary" className="people-count"><Icon type="team" /> 1,568 total</Text>
                </div>
                <div className="bar-containers">
                    <div className="bar-container">
                        <span className="number">5</span>
                        <span className="bar rate-5" style={{width: "100%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">4</span>
                        <span className="bar rate-4" style={{width: "80%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">3</span>
                        <span className="bar rate-3" style={{width: "30%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">2</span>
                        <span className="bar rate-2" style={{width: "20%"}}> </span>
                    </div>
                    <div className="bar-container">
                        <span className="number">1</span>
                        <span className="bar rate-1" style={{width: "10%"}}> </span>
                    </div>
                </div>
            </Row>
        );
    }
}

export default DetailedRating;