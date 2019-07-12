import React from 'react';
import {
    Drawer,
    Select,
    Avatar,
    Typography,
    Divider,
    Tag,
    notification,
    List,
    Button,
    Spin,
    message,
    Icon,
    Card
} from 'antd';

const {Meta} = Card;
import "../../../../App.css";
import DetailedRating from "../../detailed-rating/DetailedRating";
import {Link} from "react-router-dom";
import axios from "axios";
import config from "../../../../../public/conf/config.json";
import ReactQuill from "react-quill";
import ReactHtmlParser, {processNodes, convertNodeToElement, htmlparser2} from 'react-html-parser';
import "./AppDetailsDrawer.css";
import pSBC from "shade-blend-color";

const {Text, Title} = Typography;
const {Option} = Select;

const IconText = ({type, text}) => (
    <span>
    <Icon type={type} style={{marginRight: 8}}/>
        {text}
  </span>
);

const modules = {
    toolbar: [
        ['bold', 'italic', 'underline', 'strike', 'blockquote'],
        [{'list': 'ordered'}, {'list': 'bullet'}],
        ['link']
    ],
};

const formats = [
    'bold', 'italic', 'underline', 'strike', 'blockquote',
    'list', 'bullet',
    'link'
];

class AppDetailsDrawer extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            name: null,
            description: null,
            globalCategories: [],
            globalTags: [],
            categories: [],
            tags: [],
            temporaryDescription: null,
            temporaryCategories: [],
            temporaryTags: [],
            isDescriptionEditEnabled: false,
            isCategoriesEditEnabled: false,
            isTagsEditEnabled: false,
        };
    }

    componentDidMount() {
        this.getCategories();
        this.getTags();
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevProps.app !== this.props.app) {
            const {name, description, tags, categories} = this.props.app;
            this.setState({
                name,
                description,
                tags,
                categories,
                isDescriptionEditEnabled: false,
                isCategoriesEditEnabled: false,
                isTagsEditEnabled: false,
            });
        }
    }

    getCategories = () => {
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/categories"
        ).then(res => {
            if (res.status === 200) {
                const categories = JSON.parse(res.data.data);

                const globalCategories = categories.map(category => {
                    return (
                        <Option
                            key={category.categoryName}>
                            {category.categoryName}
                        </Option>
                    )
                });

                this.setState({
                    globalCategories,
                    loading: false
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
            } else {
                message.warning('Something went wrong while trying to load app details... :(');

            }
            this.setState({
                loading: false
            });
        });
    };

    getTags = () => {
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/tags"
        ).then(res => {
            if (res.status === 200) {
                const tags = JSON.parse(res.data.data);

                const globalTags = tags.map(tag => {
                    return (
                        <Option
                            key={tag.tagName}>
                            {tag.tagName}
                        </Option>
                    )
                });

                this.setState({
                    globalTags,
                    loading: false
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
            } else {
                message.warning('Something went wrong when trying to load tags.');

            }
            this.setState({
                loading: false
            });
        });
    };


    // change the app name
    handleNameSave = name => {
        const {id} = this.props.app;
        if (name !== this.state.name && name !== "") {
            const data = {name: name};
            axios.put(
                config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/" + id,
                data
            ).then(res => {
                if (res.status === 200) {
                    notification["success"]({
                        message: 'Saved!',
                        description: 'App name updated successfully!'
                    });
                    this.setState({
                        loading: false,
                        name: name,
                    });

                }
            }).catch((error) => {
                if (error.hasOwnProperty("response") && error.response.status === 401) {
                    message.error('You are not logged in');
                    window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
                } else {
                    message.error('Something went wrong when trying to save the app name... :(');
                }

                this.setState({loading: false});
            });
        }
    };

    // handle description change
    handleDescriptionChange = (temporaryDescription) => {
        this.setState({temporaryDescription})
    };

    enableDescriptionEdit = () => {
        this.setState({
            isDescriptionEditEnabled: true,
            temporaryDescription: this.state.description
        });
    };

    disableDescriptionEdit = () => {
        this.setState({
            isDescriptionEditEnabled: false,
        });
    };

    enableCategoriesEdit = () => {
        this.setState({
            isCategoriesEditEnabled: true,
            temporaryCategories: this.state.categories
        });
    };

    disableCategoriesEdit = () => {
        this.setState({
            isCategoriesEditEnabled: false,
        });
    };

    // handle description change
    handleCategoryChange = (temporaryCategories) => {
        this.setState({temporaryCategories})
    };

    // change app categories
    handleCategorySave = () => {
        const {id} = this.props.app;
        const {temporaryCategories, categories} = this.state;

        const difference = temporaryCategories
            .filter(x => !categories.includes(x))
            .concat(categories.filter(x => !temporaryCategories.includes(x)));

        if (difference.length !== 0 && temporaryCategories.length !== 0) {
            const data = {categories: temporaryCategories};
            axios.put(
                config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/" + id,
                data
            ).then(res => {
                if (res.status === 200) {
                    notification["success"]({
                        message: 'Saved!',
                        description: 'App categories updated successfully!'
                    });
                    this.setState({
                        loading: false,
                        categories: temporaryCategories,
                        isCategoriesEditEnabled: false
                    });

                }
            }).catch((error) => {
                if (error.hasOwnProperty("response") && error.response.status === 401) {
                    message.error('You are not logged in');
                    window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
                } else {
                    message.error('Something went wrong when trying to updating categories');
                }

                this.setState({loading: false});
            });
        }
    };

    enableTagsEdit = () => {
        this.setState({
            isTagsEditEnabled: true,
            temporaryTags: this.state.tags
        });
    };

    disableTagsEdit = () => {
        this.setState({
            isTagsEditEnabled: false,
        });
    };

    // handle description change
    handleTagsChange = (temporaryTags) => {
        this.setState({temporaryTags})
    };

    // change app tags
    handleTagsSave = () => {
        const {id} = this.props.app;
        const {temporaryTags, tags} = this.state;


        const difference = temporaryTags
            .filter(x => !tags.includes(x))
            .concat(tags.filter(x => !temporaryTags.includes(x)));

        if (difference.length !== 0 && temporaryTags.length !== 0) {
            const data = {tags: temporaryTags};
            axios.put(
                config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/" + id,
                data
            ).then(res => {
                if (res.status === 200) {
                    notification["success"]({
                        message: 'Saved!',
                        description: 'App tags updated successfully!'
                    });
                    this.setState({
                        loading: false,
                        tags: temporaryTags,
                        isTagsEditEnabled: false
                    });
                }
            }).catch((error) => {
                if (error.hasOwnProperty("response") && error.response.status === 401) {
                    message.error('You are not logged in');
                    window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
                } else {
                    message.error('Something went wrong when trying to update tags');
                }

                this.setState({loading: false});
            });
        }
    };

    //handle description save
    handleDescriptionSave = () => {

        const {id} = this.props.app;
        const {description, temporaryDescription} = this.state;

        if (temporaryDescription !== description && temporaryDescription !== "<p><br></p>") {
            const data = {description: temporaryDescription};
            axios.put(
                config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/" + id,
                data
            ).then(res => {
                if (res.status === 200) {
                    notification["success"]({
                        message: 'Saved!',
                        description: 'App description updated successfully!'
                    });
                    this.setState({
                        loading: false,
                        description: temporaryDescription,
                        isDescriptionEditEnabled: false
                    });
                }
            }).catch((error) => {
                if (error.hasOwnProperty("response") && error.response.status === 401) {
                    message.error('You are not logged in');
                    window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
                } else {
                    message.error('Something went wrong... :(');
                }

                this.setState({loading: false});
            });
        } else {
            this.setState({isDescriptionEditEnabled: false});
        }
    };


    render() {
        const {app, visible, onClose} = this.props;
        const {
            name, loading, description, isDescriptionEditEnabled, isCategoriesEditEnabled,
            isTagsEditEnabled, temporaryDescription, temporaryCategories, temporaryTags,
            globalCategories, globalTags, categories, tags
        } = this.state;
        if (app == null) {
            return null;
        }
        return (
            <div>

                <Drawer
                    placement="right"
                    width={640}
                    closable={false}
                    onClose={onClose}
                    visible={visible}
                >
                    <Spin spinning={loading} delay={500}>
                        <div style={{textAlign: "center"}}>
                            <img
                                style={{
                                    marginBottom: 10,
                                    width: 100,
                                    borderRadius: "28%",
                                    border: "1px solid #ddd"
                                }}
                                src={app.applicationReleases[0].iconPath}
                            />
                            <Title editable={{onChange: this.handleNameSave}} level={2}>{name}</Title>
                        </div>

                        <Divider/>

                        <Text strong={true}>Releases </Text>
                        {/*display add new release only if app type is enterprise*/}
                        {(app.type === "ENTERPRISE") && (
                            <Link to={`/publisher/apps/${app.id}/add-release`}><Button htmlType="button" size="small">Add
                                new release</Button></Link>)}
                        <br/>
                        <List
                            grid={{gutter: 16, column: 2}}
                            dataSource={app.applicationReleases}
                            renderItem={release => (
                                <List.Item>
                                    <a href={"apps/releases/" + release.uuid}>
                                        <Card className="release-card">
                                            <Meta
                                                avatar={
                                                    <Avatar size="large" shape="square" src={release.iconPath}/>
                                                }
                                                title={release.version}
                                                description={
                                                    <div style={{
                                                        fontSize: "0.7em"
                                                    }}>
                                                        <IconText type="check" text={release.currentStatus}/>
                                                        <Divider type="vertical"/>
                                                        <IconText type="upload" text={release.releaseType}/>
                                                        <Divider type="vertical"/>
                                                        <IconText type="star-o" text={release.rating.toFixed(1)}/>
                                                    </div>
                                                }
                                            />
                                        </Card>
                                    </a>
                                </List.Item>
                            )}
                        />
                        <Divider dashed={true}/>

                        <Text strong={true}>Description </Text>
                        {!isDescriptionEditEnabled && (
                            <Text
                                style={{
                                    color: config.theme.primaryColor,
                                    cursor: "pointer"
                                }}
                                onClick={this.enableDescriptionEdit}>
                                <Icon type="edit"/>
                            </Text>
                        )}

                        {!isDescriptionEditEnabled && (
                            <div>{ReactHtmlParser(description)}</div>
                        )}

                        {isDescriptionEditEnabled && (
                            <div>
                                <ReactQuill
                                    theme="snow"
                                    value={temporaryDescription}
                                    onChange={this.handleDescriptionChange}
                                    modules={modules}
                                    formats={formats}
                                    placeholder="Add description"
                                    style={{
                                        marginBottom: 10,
                                        marginTop: 10
                                    }}
                                />
                                <Button style={{marginRight: 10}} size="small" htmlType="button"
                                        onClick={this.disableDescriptionEdit}>Cancel</Button>
                                <Button size="small" type="primary" htmlType="button"
                                        onClick={this.handleDescriptionSave}>Save</Button>
                            </div>
                        )}

                        <Divider dashed={true}/>
                        <Text strong={true}>Categories </Text>
                        {!isCategoriesEditEnabled && (<Text
                                style={{color: config.theme.primaryColor, cursor: "pointer"}}
                                onClick={this.enableCategoriesEdit}>
                                <Icon type="edit"/>
                            </Text>
                        )}
                        <br/>
                        <br/>
                        {isCategoriesEditEnabled && (
                            <div>
                                <Select
                                    mode="multiple"
                                    style={{width: '100%'}}
                                    placeholder="Please select categories"
                                    onChange={this.handleCategoryChange}
                                    value={temporaryCategories}
                                >
                                    {globalCategories}
                                </Select>
                                <div style={{marginTop: 10}}>
                                    <Button style={{marginRight: 10}} size="small" htmlType="button"
                                            onClick={this.disableCategoriesEdit}>Cancel</Button>
                                    <Button
                                        size="small"
                                        type="primary"
                                        htmlType="button"
                                        onClick={this.handleCategorySave}>Save</Button>
                                </div>
                            </div>
                        )}
                        {!isCategoriesEditEnabled && (
                            <span>{
                                categories.map(category => {
                                    return (
                                        <Tag color={pSBC(0.30, config.theme.primaryColor)} key={category}
                                             style={{marginBottom: 5}}>
                                            {category}
                                        </Tag>
                                    );
                                })
                            }</span>
                        )}


                        <Divider dashed={true}/>
                        <Text strong={true}>Tags </Text>
                        {!isTagsEditEnabled && (<Text
                                style={{color: config.theme.primaryColor, cursor: "pointer"}}
                                onClick={this.enableTagsEdit}>
                                <Icon type="edit"/>
                            </Text>
                        )}
                        <br/>
                        <br/>
                        {isTagsEditEnabled && (
                            <div>
                                <Select
                                    mode="tags"
                                    style={{width: '100%'}}
                                    placeholder="Please select categories"
                                    onChange={this.handleTagsChange}
                                    value={temporaryTags}
                                >
                                    {globalTags}
                                </Select>
                                <div style={{marginTop: 10}}>
                                    <Button style={{marginRight: 10}} size="small" htmlType="button"
                                            onClick={this.disableTagsEdit}>Cancel</Button>
                                    <Button
                                        size="small"
                                        type="primary"
                                        htmlType="button"
                                        onClick={this.handleTagsSave}>Save</Button>
                                </div>
                            </div>
                        )}
                        {!isTagsEditEnabled && (
                            <span>{
                                tags.map(tag => {
                                    return (
                                        <Tag color="#34495e" key={tag} style={{marginBottom: 5}}>
                                            {tag}
                                        </Tag>
                                    );
                                })
                            }</span>
                        )}

                        <Divider dashed={true}/>

                        <DetailedRating type="app" uuid={app.applicationReleases[0].uuid}/>
                    </Spin>
                </Drawer>
            </div>
        );
    }
}

export default AppDetailsDrawer;