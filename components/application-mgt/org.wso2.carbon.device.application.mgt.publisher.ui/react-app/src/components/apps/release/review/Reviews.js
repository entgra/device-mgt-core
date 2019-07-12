import React from "react";
import {List, message, Avatar, Spin, Button} from 'antd';
import "./Reviews.css";

import InfiniteScroll from 'react-infinite-scroller';
import SingleReview from "./SingleReview";
import axios from "axios";
import config from "../../../../../public/conf/config.json";

const limit = 5;

class Reviews extends React.Component {
    state = {
        data: [],
        loading: false,
        hasMore: false,
        loadMore: false
    };


    componentDidMount() {
        this.fetchData(0, limit, res => {
            this.setState({
                data: res,
            });
        });
    }

    fetchData = (offset, limit, callback) => {

        const {uuid, type} = this.props;

        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/admin/reviews/" + type + "/" + uuid
        ).then(res => {
            if (res.status === 200) {
                let reviews = res.data.data.data;
                callback(reviews);
            }

        }).catch(function (error) {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
            } else {
                message.warning('Something went wrong when trying to load reviews');

            }
        });
    };

    handleInfiniteOnLoad = (count) => {
        const offset = count * limit;
        let data = this.state.data;
        this.setState({
            loading: true,
        });
        if (data.length > 149) {
            this.setState({
                hasMore: false,
                loading: false,
            });
            return;
        }
        this.fetchData(offset, limit, res => {
            if (res.length > 0) {
                data = data.concat(res);
                this.setState({
                    data,
                    loading: false,
                });
            } else {
                this.setState({
                    hasMore: false,
                    loading: false
                });
            }
        });
    };

    enableLoading = () => {
        this.setState({
            hasMore: true,
            loadMore: true
        });
    };

    render() {
        return (
            <div className="demo-infinite-container">
                <InfiniteScroll
                    initialLoad={false}
                    pageStart={0}
                    loadMore={this.handleInfiniteOnLoad}
                    hasMore={!this.state.loading && this.state.hasMore}
                    useWindow={true}
                >
                    <List
                        dataSource={this.state.data}
                        renderItem={item => (
                            <List.Item key={item.id}>
                                <SingleReview review={item}/>
                            </List.Item>
                        )}
                    >
                        {this.state.loading && this.state.hasMore && (
                            <div className="demo-loading-container">
                                <Spin/>
                            </div>
                        )}
                    </List>
                </InfiniteScroll>
                {!this.state.loadMore && (this.state.data.length >= limit) && (<div style={{textAlign: "center"}}>
                    <Button type="dashed" htmlType="button" onClick={this.enableLoading}>Read All Reviews</Button>
                </div>)}
            </div>
        );
    }
}

export default Reviews;