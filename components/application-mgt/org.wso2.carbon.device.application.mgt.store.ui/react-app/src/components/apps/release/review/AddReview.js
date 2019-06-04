import React from "react";
import {Drawer, Button, Icon, Row, Col, Typography, Divider,Input} from 'antd';
import StarRatings from "react-star-ratings";

const {Title} = Typography;
const {TextArea} = Input;
class AddReview extends React.Component {
    state = {visible: false};

    showDrawer = () => {
        this.setState({
            visible: true,
        });
    };

    onClose = () => {
        this.setState({
            visible: false,
        });
    };
    changeRating = (newRating, name) => {
        this.setState({
            rating: newRating
        });
    }

    render() {
        return (
            <div style={{paddingTop: 20}}>
                <Button style={{float: "right"}} type="dashed" onClick={this.showDrawer}>
                    <Icon type="star"/> Add a review
                </Button>

                <Drawer
                    // title="Basic Drawer"
                    placement="bottom"
                    closable={false}
                    onClose={this.onClose}
                    visible={this.state.visible}
                    mask={true}
                    height={400}
                ><Row>
                    <Col lg={8}/>
                    <Col lg={8}>
                        <Title level={4}>Add review</Title>
                        <Divider/>
                        <TextArea
                            placeholder="Tell others what you think about this app. Would you recommend it, and why?"
                            autosize={{ minRows: 6, maxRows: 12 }}
                            style={{marginBottom:20}}
                        />
                        <StarRatings
                            rating={this.state.rating}
                            changeRating={this.changeRating}
                            starRatedColor="#777"
                            starHoverColor = "#444"
                            starDimension="20px"
                            starSpacing="2px"
                            numberOfStars={5}
                            name='rating'
                        />
                        <br/><br/>
                        <Button onClick={this.onClose} style={{marginRight: 8}}>
                            Cancel
                        </Button>
                        <Button onClick={this.onClose} type="primary">
                            Submit
                        </Button>
                    </Col>
                </Row>
                </Drawer>


            </div>
        );
    }
}

export default AddReview;