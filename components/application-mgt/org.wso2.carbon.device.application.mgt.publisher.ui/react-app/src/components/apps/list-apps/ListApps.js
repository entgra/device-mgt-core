import React from "react";
import {Avatar, Card, Col, Row, Table, Typography, Input, Divider, Checkbox, Select, Button} from "antd";
import {connect} from "react-redux";
import {getApps} from "../../../js/actions";
import AppsTable from "./AppsTable";
import Filters from "./Filters";

const {Option} = Select;
const {Title, Text} = Typography;
const Search = Input.Search;
// connecting state.apps with the component
const mapStateToProps = state => {
    return {apps: state.apps}
};


class ConnectedListApps extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            count: 1
        }
    }

    render() {
        return (
            <Row gutter={28}>
                <Col md={6}>
                    <Filters/>
                </Col>
                <Col md={18}>
                    <Card>
                        <Row>
                            <Col span={6}>
                                <Title level={4}>Apps</Title>
                            </Col>
                            <Col span={18} style={{textAlign: "right"}}>
                                <Search
                                    placeholder="input search text"
                                    onSearch={value => console.log(value)}
                                    style={{width: 200}}
                                />
                            </Col>
                        </Row>
                        <Divider dashed={true}/>
                        <AppsTable/>
                    </Card>
                </Col>
            </Row>
        );
    }
}

const ListApps = connect(mapStateToProps, {getApps})(ConnectedListApps);

export default ListApps;