import React from "react";
import {Avatar, Card, Col, Row, Table, Typography, Tag, Icon, message} from "antd";
import {connect} from "react-redux";
import {getApps} from "../../js/actions";
import AppsTable from "./AppsTable";

const {Title} = Typography;

// connecting state.apps with the component
const mapStateToProps = state => {
    return {apps: state.apps}
};


class ConnectedListApps extends React.Component {
    constructor(props) {
        super(props);
        this.state = {};
    }

    render() {
        console.log(this.props.apps);
        return (
            <Row gutter={32}>
                <Col md={18}>
                    <Card>
                        <AppsTable/>
                    </Card>
                </Col>

                <Col md={6}>
                    <Title level={4}>#Search</Title>
                </Col>
            </Row>
        );
    }
}

const ListApps = connect(mapStateToProps, {getApps})(ConnectedListApps);

export default ListApps;