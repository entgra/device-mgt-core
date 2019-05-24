import React from "react";
import {Divider, Row, Col, Typography, Button,Rate} from "antd";
import "../../../App.css";

const {Title, Text, Paragraph} = Typography;

class ReleaseView extends React.Component {
    render() {
        const release = this.props.release;
        console.log(release);
        return (
            <div>
                <div className="release">
                    <Row>
                        <Col xl={4} sm={6} xs={8} className="release-icon">
                            <img src={release.iconPath} alt="icon"/>
                        </Col>
                        <Col xl={10} sm={11} className="release-title">
                            <Title level={2}>App Name</Title>
                            <Text>Version : {release.version}</Text><br/>
                            <Rate disabled allowHalf defaultValue={release.rating} />
                        </Col>
                        <Col xl={8} md={10} sm={24} xs={24} style={{float: "right"}}>
                            <div>
                                <Button.Group style={{float: "right"}}>
                                    <Button htmlType="button" icon="usergroup-add">Enterprise Install</Button>
                                    <Button htmlType="button" type="primary" icon="download">Install</Button>
                                </Button.Group>
                            </div>
                        </Col>
                    </Row>
                    <Divider />
                    <Row>
                        {release.screenshots.map((screenshotUrl) => {
                            return (
                                <Col key={"col-" + screenshotUrl} lg={6} md={8} xs={8} className="release-screenshot">
                                    <img key={screenshotUrl} src={screenshotUrl}/>
                                </Col>
                            )
                        })}
                    </Row>
                    <Divider />
                    <Paragraph type="secondary" ellipsis={{ rows: 3, expandable: true }}>
                        {release.description}
                    </Paragraph>
                </div>
            </div>
        );
    }
}

export default ReleaseView;