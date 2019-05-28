import React from "react";
import {Avatar} from "antd";
import {List,Typography} from "antd";
import StarRatings from "react-star-ratings";

const {Text, Paragraph} = Typography;
const colorList = ['#f56a00', '#7265e6', '#ffbf00', '#00a2ae'];

class SingleReview extends React.Component {


    render() {
        const review = this.props.review;
        const randomColor = colorList[Math.floor(Math.random() * (colorList.length))];
        // const avatarLetter =
        const content = (
            <div style={{marginTop: -5}}>
                <StarRatings
                    rating={review.rating}
                    starRatedColor="#777"
                    starDimension = "12px"
                    starSpacing = "2px"
                    numberOfStars={5}
                    name='rating'
                />
                <Text style={{fontSize: 12, color: "#aaa"}} type="secondary"> {review.createdAt}</Text><br/>
                <Paragraph ellipsis={{ rows: 3, expandable: true }} style={{color: "#777"}}>{review.content}</Paragraph>
            </div>
        );

        return (
            <div>
                <List.Item.Meta
                    avatar={
                        <Avatar style={{ backgroundColor: randomColor, verticalAlign: 'middle' }} size="large">
                            A
                        </Avatar>
                    }
                    title={<a href="https://ant.design">admin</a>}
                    description={content}
                />
            </div>
        );
    }
}

export default SingleReview;