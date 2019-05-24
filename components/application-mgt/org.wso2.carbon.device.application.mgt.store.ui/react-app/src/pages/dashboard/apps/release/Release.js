import React from "react";
import '../../../../App.css';
import {PageHeader, Typography, Row, Col, Card} from "antd";
import {connect} from "react-redux";
import ReleaseView from "../../../../components/apps/release/ReleaseView";
import {getRelease} from "../../../../js/actions";

const {Title} = Typography;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'store',
    },
    {
        path: 'first',
        breadcrumbName: 'Dashboard',
    },
    {
        path: 'second',
        breadcrumbName: 'Apps',
    },
];

const mapStateToProps = state => {
    return {release: state.release}
};

const mapDispatchToProps = dispatch => ({
    getRelease: (uuid) => dispatch(getRelease(uuid))
});

class ConnectedRelease extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;

    }

    componentDidMount() {
        const {uuid} = this.props.match.params;
        this.props.getRelease(uuid);
    }

    render() {

        const release = this.props.release;
        if (release == null) {
            return (
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    <Title level={3}>No Releases Found</Title>
                </div>
            );
        }

        //todo remove uppercase
        return (
            <div>
                {/*<PageHeader*/}
                    {/*breadcrumb={{routes}}*/}
                {/*/>*/}
                <div className="main-container">
                    <Row style={{padding: 10}}>
                        <Col lg={4}>

                        </Col>
                        <Col lg={16} md={24} style={{padding: 3}}>
                            <Card>
                                <ReleaseView release={release}/>
                            </Card>
                        </Col>
                    </Row>
                </div>
            </div>

        );
    }
}

const Release = connect(mapStateToProps,mapDispatchToProps)(ConnectedRelease);

export default Release;
