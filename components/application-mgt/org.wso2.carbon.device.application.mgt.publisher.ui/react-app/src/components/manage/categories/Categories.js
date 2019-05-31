import React from "react";
import {Card, Col, Row, Table, Typography, Tag, message, Icon, Input, notification, Divider, Button} from "antd";
import Highlighter from 'react-highlight-words';
import axios from "axios";
import config from "../../../../public/conf/config.json";
import {TweenOneGroup} from 'rc-tween-one';

const {Title} = Typography;


class Categories extends React.Component {

    state = {
        searchText: '',
        categories: [],
        tempElements: [],
        inputVisible: false,
        inputValue: '',
    };

    componentDidMount() {
        const request = "method=get&content-type=application/json&payload={}&api-endpoint=/application-mgt-publisher/v1.0/applications/categories";
        axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
        ).then(res => {
            if (res.status === 200) {
                let categories = JSON.parse(res.data.data);
                this.setState({
                    categories: categories
                });
            }

        }).catch(function (error) {
            if (error.response.status === 401) {
                window.location.href = 'https://localhost:9443/publisher/login';
            } else {
                message.warning('Something went wrong');

            }
        });
    }

    // handleClose = removedTag => {
    //     const tags = this.state.tags.filter(tag => tag !== removedTag);
    //     console.log(tags);
    //     this.setState({ tags });
    // };

    renderElement = (category) => {
        const tagElem = (
            <Tag
                color="blue"
                closable
                onClose={e => {
                    e.preventDefault();
                    // this.handleClose(category.categoryName);
                }}
            >
                {category.categoryName}
            </Tag>
        );
        return (
            <span key={category.categoryName} style={{display: 'inline-block'}}>
                {tagElem}
            </span>
        );
    };

    renderTempElement = (category) => {
        const tagElem = (
            <Tag
                closable
                onClose={e => {
                    e.preventDefault();
                    // this.handleClose(category.categoryName);
                }}
            >
                {category.categoryName}
            </Tag>
        );
        return (
            <span key={category.categoryName} style={{display: 'inline-block'}}>
                {tagElem}
            </span>
        );
    };

    showInput = () => {
        this.setState({inputVisible: true}, () => this.input.focus());
    };

    handleInputChange = e => {
        this.setState({inputValue: e.target.value});
    };

    handleInputConfirm = () => {
        const {inputValue,categories} = this.state;
        let {tempElements} = this.state;
        console.log(inputValue);
        if (inputValue) {
            if ((categories.findIndex(i => i.categoryName === inputValue) === -1) && (tempElements.findIndex(i => i.categoryName === inputValue) === -1)) {
                tempElements = [...tempElements, {categoryName:inputValue, isCategoryDeletable: true}];
                console.log(tempElements);
                // notification["success"]({
                //     message: "Success!",
                //     description:
                //         "New Category " + inputValue + " was added successfully",
                // });
            }else {
                message.warning('Category already exists');
            }
        }

        this.setState({
            tempElements,
            inputVisible: false,
            inputValue: '',
        });
    };

    handleSave = () => {
        const {tempElements, categories} = this.state;

        const stringifiedArray = JSON.stringify(tempElements);

        const request = "method=get&content-type=application/json&payload={"+stringifiedArray+"}&api-endpoint=/application-mgt-publisher/v1.0/admin/applications/categories";
        axios.post('https://' + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invokerUri, request
        ).then(res => {
            if (res.status === 200) {
                let categories = JSON.parse(res.data.data);
                this.setState({
                    categories: categories
                });
            }

        }).catch(function (error) {
            if (error.response.status === 401) {
                window.location.href = 'https://localhost:9443/publisher/login';
            } else {
                message.warning('Something went wrong');

            }
        });

        this.setState({
            categories : [...categories, ...tempElements],
            tempElements: [],
            inputVisible: false,
            inputValue: '',
        });
    };

    saveInputRef = input => (this.input = input);

    render() {
        const {categories, inputVisible, inputValue, tempElements} = this.state;
        const categoriesElements = categories.map(this.renderElement);
        const temporaryElements = tempElements.map(this.renderTempElement);
        return (
            <div>
                <Card title="Manage Categories">
                    <div style={{marginBottom: 16}}>
                        <TweenOneGroup
                            enter={{
                                scale: 0.8,
                                opacity: 0,
                                type: 'from',
                                duration: 100,
                                onComplete: e => {
                                    e.target.style = '';
                                },
                            }}
                            leave={{opacity: 0, width: 0, scale: 0, duration: 200}}
                            appear={false}
                        >
                            {temporaryElements}

                            {inputVisible && (
                                <Input
                                    ref={this.saveInputRef}
                                    type="text"
                                    size="small"
                                    style={{width: 78}}
                                    value={inputValue}
                                    onChange={this.handleInputChange}
                                    onBlur={this.handleInputConfirm}
                                    onPressEnter={this.handleInputConfirm}
                                />
                            )}
                            {!inputVisible && (
                                <Tag onClick={this.showInput} style={{background: '#fff', borderStyle: 'dashed'}}>
                                    <Icon type="plus"/> New Category
                                </Tag>
                            )}
                        </TweenOneGroup>
                    </div>
                    {tempElements.length>0 && (<div><Button onClick={this.handleSave} htmlType="button">Save</Button> <Divider/></div>)}



                    <div style={{marginTop: 16}}>
                        <TweenOneGroup
                            enter={{
                                scale: 0.8,
                                opacity: 0,
                                type: 'from',
                                duration: 100,
                                onComplete: e => {
                                    e.target.style = '';
                                },
                            }}
                            leave={{opacity: 0, width: 0, scale: 0, duration: 200}}
                            appear={false}
                        >
                            {categoriesElements}
                        </TweenOneGroup>
                    </div>
                </Card>
            </div>
        );
    }
}

export default Categories;