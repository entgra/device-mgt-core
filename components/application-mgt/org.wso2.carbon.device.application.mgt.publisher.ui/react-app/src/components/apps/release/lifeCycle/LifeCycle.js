import React from "react";
import {Typography, Tag, Divider, Select, Button, Modal, message, notification, Collapse} from "antd";
import axios from "axios";
import config from "../../../../../public/conf/config.json";

import ReactQuill from 'react-quill';
import 'react-quill/dist/quill.snow.css';
import './LifeCycle.css';

const {Text, Title, Paragraph} = Typography;
const {Option} = Select;
const Panel = Collapse.Panel;

const modules = {
    toolbar: [
        [{'header': [1, 2, false]}],
        ['bold', 'italic', 'underline', 'strike', 'blockquote', 'code-block'],
        [{'list': 'ordered'}, {'list': 'bullet'}],
        ['link', 'image']
    ],
};

const formats = [
    'header',
    'bold', 'italic', 'underline', 'strike', 'blockquote', 'code-block',
    'list', 'bullet',
    'link', 'image'
];

class LifeCycle extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            currentStatus: props.currentStatus,
            selectedStatus: null,
            lifecycle: [],
            reasonText: '',
            isReasonModalVisible: false,
            isConfirmButtonLoading: false
        }
    }

    componentDidMount() {
        this.fetchData();
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevProps.currentStatus !== this.props.currentStatus || prevProps.uuid !== this.props.uuid) {
            this.setState({
                currentStatus: this.props.currentStatus
            });
        }
    }


    fetchData = () => {
        axios.get(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/lifecycle-config"
        ).then(res => {
            if (res.status === 200) {
                const lifecycle = res.data.data;
                this.setState({
                    lifecycle: lifecycle
                })
            }

        }).catch(function (error) {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
            } else {
                message.error('Something went wrong when trying to load lifecycle configuration');
            }
        });
    };

    handleChange = (value) => {
        this.setState({reasonText: value})
    };

    handleSelectChange = (value) => {
        this.setState({selectedStatus: value})
    };

    showReasonModal = () => {
        this.setState({
            isReasonModalVisible: true
        });
    };

    closeReasonModal = () => {
        this.setState({
            isReasonModalVisible: false
        });
    };

    addLifeCycle = () => {
        const {selectedStatus, reasonText} = this.state;
        const {uuid} = this.props;
        const data = {
            action: selectedStatus,
            reason: reasonText
        };

        this.setState({
            isConfirmButtonLoading: true,
        });

        axios.post(
            config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + config.serverConfig.invoker.uri + config.serverConfig.invoker.publisher + "/applications/life-cycle/" + uuid,
            data
        ).then(res => {
            if (res.status === 201) {
                this.setState({
                    isReasonModalVisible: false,
                    isConfirmButtonLoading: false,
                    currentStatus: selectedStatus,
                    selectedStatus: null,
                    reasonText: ''
                });
                this.props.changeCurrentLifecycleStatus(selectedStatus);
                notification["success"]({
                    message: "Done!",
                    description:
                        "Lifecycle state updated successfully!",
                });
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                window.location.href = config.serverConfig.protocol + "://" + config.serverConfig.hostname + ':' + config.serverConfig.httpsPort + '/publisher/login';
            } else {
                notification["error"]({
                    message: "Error",
                    description:
                        "Something went wrong when trying to add lifecycle",
                });
            }
            this.setState({
                isConfirmButtonLoading: false
            });
        });


    };


    render() {
        const {currentStatus, lifecycle, selectedStatus} = this.state;
        const selectedValue = selectedStatus == null ? [] : selectedStatus;
        let proceedingStates = [];
        if((lifecycle.hasOwnProperty(currentStatus)) && lifecycle[currentStatus].hasOwnProperty("proceedingStates")){
            proceedingStates = lifecycle[currentStatus].proceedingStates;
        }
        console.log(lifecycle);
        return (
            <div>
                <Title level={4}>Manage Lifecycle</Title>
                <Divider/>
                <Paragraph>
                    Ensure that your security policies are not violated by the application. Have a thorough review and
                    approval process before directly publishing it to your app store. You can easily transition from one
                    state to another. <br/>Note: ‘Change State To’ displays only the next states allowed from the
                    current state
                </Paragraph>
                <Divider dashed={true}/>
                <Text strong={true}>Current State: </Text> <Tag color="blue">{currentStatus}</Tag><br/><br/>
                <Text>Change State to: </Text>
                <Select
                    placeholder="Select state"
                    style={{width: 120}}
                    size="small"
                    onChange={this.handleSelectChange}
                    value={selectedValue}
                    showSearch={true}
                >
                    {proceedingStates.map(lifecycleState => {
                            return (
                                <Option
                                    key={lifecycleState}
                                    value={lifecycleState}>
                                    {lifecycleState}
                                </Option>
                            )
                        })
                    }
                </Select>
                <Button
                    style={{marginLeft: 10}}
                    size="small"
                    type="primary"
                    htmlType="button"
                    onClick={this.showReasonModal}
                    disabled={selectedStatus == null}
                >
                    Change
                </Button>


                <Divider/>
                <Text strong={true}>Lorem Ipsum</Text>
                <Collapse defaultActiveKey={currentStatus}>
                    {
                        Object.keys(lifecycle).map(lifecycleState => {
                            return (
                                <Panel header={lifecycleState} key={lifecycleState}>
                                    {
                                        Object.keys(lifecycle).map(state => {
                                            // console.log(lifecycle[lifecycleState].proceedingStates);
                                            const isEnabled = lifecycle[lifecycleState].hasOwnProperty("proceedingStates") && (lifecycle[lifecycleState].proceedingStates.includes(state));
                                            const color = isEnabled ? "green" : "";
                                            return (
                                                <Tag
                                                    disabled={!isEnabled}
                                                    key={state} style={{marginBottom: 5}} color={color}>{state}</Tag>
                                            )
                                        })
                                    }
                                </Panel>
                            )
                        })
                    }

                </Collapse>

                <Modal
                    title="Confirm changing lifecycle state"
                    visible={this.state.isReasonModalVisible}
                    onOk={this.addLifeCycle}
                    onCancel={this.closeReasonModal}
                    okText="confirm"
                >
                    <Text>
                        You are going to change the lifecycle state from,<br/>
                        <Tag color="blue">{currentStatus}</Tag>to <Tag
                        color="blue">{selectedStatus}</Tag>
                    </Text>
                    <br/><br/>
                    <ReactQuill
                        theme="snow"
                        value={this.state.reasonText}
                        onChange={this.handleChange}
                        modules={modules}
                        formats={formats}
                        placeholder="Leave a comment (optional)"
                    />
                </Modal>

            </div>
        );
    }

}

export default LifeCycle;