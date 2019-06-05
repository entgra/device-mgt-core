import React from "react";
import {Avatar, Card, Col, Row, Table, Typography, Input, Divider, Checkbox, Select} from "antd";
import {connect} from "react-redux";
import {getApps} from "../../js/actions";
import AppsTable from "./AppsTable";

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
        this.state = {};
    }

    render() {
        console.log(this.props.apps);
        return (
            <Row gutter={28}>

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
                <Col md={6}>
                    <Card>
                        <Row>
                            <Col span={6}>
                                <Title level={4}>Filter</Title>
                            </Col>
                        </Row>
                        <Divider/>

                        <Text strong={true}>Category</Text>
                        <br/><br/>
                        <Select
                            mode="multiple"
                            style={{width: '100%'}}
                            placeholder="All Categories"
                        >
                            <Option key={1}>IoT</Option>
                            <Option key={2}>EMM</Option>
                        </Select>
                        <Divider/>

                        <Text strong={true}>Platform</Text>
                        <br/><br/>
                        <Checkbox>Android</Checkbox><br/>
                        <Checkbox>iOS</Checkbox><br/>
                        <Checkbox>Windows</Checkbox><br/>
                        <Checkbox>Default</Checkbox><br/>
                        <Divider/>

                        <Text strong={true}>Tags</Text>
                        <br/><br/>
                        <Select
                            mode="multiple"
                            style={{width: '100%'}}
                            placeholder="All Tags"
                        >
                            <Option key={1}>test tag</Option>
                        </Select>

                        <Divider/>
                        <Text strong={true}>Type</Text>
                        <br/><br/>
                        <Checkbox>Enterprise</Checkbox><br/>
                        <Checkbox>Public</Checkbox><br/>
                        <Checkbox>Web APP</Checkbox><br/>
                        <Checkbox>Web Clip</Checkbox><br/>
                        <Divider/>

                        <Text strong={true}>Subscription</Text>
                        <br/><br/>
                        <Checkbox>Free</Checkbox><br/>
                        <Checkbox>Paid</Checkbox><br/>
                        <Divider/>
                    </Card>
                </Col>
            </Row>
        );
    }
}

const ListApps = connect(mapStateToProps, {getApps})(ConnectedListApps);

export default ListApps;