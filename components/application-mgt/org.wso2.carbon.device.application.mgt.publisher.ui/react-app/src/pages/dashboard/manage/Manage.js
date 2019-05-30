import React from "react";
import "antd/dist/antd.css";
import {PageHeader, Typography, Input, Button, Row, Col} from "antd";
import Categories from "../../../components/manage/categories/Categories";

const {Paragraph} = Typography;

const routes = [
    {
        path: 'index',
        breadcrumbName: 'Publisher',
    },
    {
        path: 'first',
        breadcrumbName: 'Dashboard',
    },
    {
        path: 'second',
        breadcrumbName: 'Manage',
    },
];


class Manage extends React.Component {
    routes;

    constructor(props) {
        super(props);
        this.routes = props.routes;

    }

    render() {
        return (
            <div>
                <PageHeader
                    breadcrumb={{routes}}
                    title = "Manage"
                >
                    <div className="wrap">
                        <div className="content">
                            <Paragraph>
                                Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempo.
                            </Paragraph>
                        </div>
                    </div>
                </PageHeader>
                <div style={{background: '#f0f2f5', padding: 24, minHeight: 780}}>
                    <Categories/>
                </div>

            </div>

        );
    }
}

export default Manage;
