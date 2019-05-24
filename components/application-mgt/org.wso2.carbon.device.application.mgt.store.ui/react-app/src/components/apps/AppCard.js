import {
    Skeleton, Switch, Card, Icon, Avatar, Typography, Col, Row, Rate
} from 'antd';
import React from "react";
import {openReleasesModal} from "../../js/actions";
import {connect} from "react-redux";
import {Link} from "react-router-dom";

const { Meta } = Card;
const { Text, Title } = Typography;

const mapDispatchToProps = dispatch => ({
    openReleasesModal: (app) => dispatch(openReleasesModal(app))
});

class ConnectedAppCard extends React.Component {

    constructor(props){
        super(props);
        this.handleReleasesClick = this.handleReleasesClick.bind(this);
    }

    handleReleasesClick(){
        this.props.openReleasesModal(this.props.app);
    }

    render() {
        const app = this.props.app;
        const release = this.props.app.applicationReleases[0];
        console.log(this.props);

        const description = (
            <Link to={"/store/apps/"+release.uuid}>
                <Row>
                    <Col span={8}>
                        <Avatar shape="square" size={64} src={release.iconPath} />
                    </Col>
                    <Col span={16}>
                        <Text strong level={4}>{app.name}</Text><br/>
                        <Text type="secondary" level={4}>{app.deviceType}</Text><br/>
                        <Rate disabled allowHalf defaultValue={app.rating} />
                    </Col>
                </Row>
            </Link>
        );

        return (
                <Card style={{marginTop: 16 }} >
                    <Meta
                        description={description}
                    />
                </Card>
        );
    }
}

const AppCard = connect(null,mapDispatchToProps)(ConnectedAppCard);

export default AppCard;