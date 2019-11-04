import React, {Component} from 'react';
import {Button, Form, Select, Input, message, Modal, notification, Typography} from "antd";
import axios from "axios";
import {withConfigContext} from "../../context/ConfigContext";
const { Option } = Select;
const {Text} = Typography;
let config = null;
let roleSelector = [];

class AddUser extends Component {

    constructor(props) {
        super(props);
        config =  this.props.context;
        this.state = {
            addModalVisible: false,
            userStoreDomain:'PRIMARY',
            userName: '',
            firstName: '',
            lastName: '',
            emailAddress: '',
            userRole: [],
        }
    }
    openAddModal = () => {

        this.setState({
            addModalVisible:true
        });
        this.getRole();
    };

    handleAddOk = e => {
        this.props.form.validateFields(err => {
            if (!err) {
                this.onConfirmAddUser();
                this.setState({
                    addModalVisible: false,
                });
            }
        });
    };

    onConfirmAddUser = () =>{
        const config = this.props.context;
        const userData = {
            username : this.state.userStoreDomain +"/"+this.state.userName,
            firstname : this.state.firstName,
            lastname : this.state.lastName,
            emailAddress : this.state.emailAddress,
            roles : this.state.userRole
        };
        axios.post(
            window.location.origin + config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt +
            "/users",
            userData,
            {headers: {'Content-Type' : 'application-json'}}
        ).then(res => {
            if (res.status === 201) {
                this.props.fetchUsers();
                notification["success"]({
                    message: "Done",
                    duration: 4,
                    description:
                        "Successfully added the user.",
                });
            }
        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error
                message.error('You are not logged in');
                window.location.href = window.location.origin + '/entgra/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:
                        "Error occurred while trying to add user.",
                });
            }
        });
    };

    handleAddCancel = e => {
        this.setState({
            addModalVisible: false,
        });
    };

    onChangeInputs = (e) =>{
        let name = e.target.name;
        let value = e.target.value;
        this.setState({
            [name] : value
        });
    };

    onSelectInputs = value =>{
        this.setState({
            userRole: value
        });
    };

    getRole = () => {
        const config = this.props.context;

        let apiURL = window.location.origin + config.serverConfig.invoker.uri +
            config.serverConfig.invoker.deviceMgt + "/roles?user-store="+this.state.userStoreDomain+"&limit=100";

        axios.get(apiURL).then(res => {
            if (res.status === 200) {
                for(let i=0; i<res.data.data.roles.length ; i++){
                    roleSelector.push(<Option key={res.data.data.roles[i]}>{res.data.data.roles[i]}</Option>);
                }
            }

        }).catch((error) => {
            if (error.hasOwnProperty("response") && error.response.status === 401) {
                //todo display a popop with error

                message.error('You are not logged in');
                window.location.href = window.location.origin + '/entgra/login';
            } else {
                notification["error"]({
                    message: "There was a problem",
                    duration: 0,
                    description:"Error occurred while trying to load roles.",
                });
            }

        })
    };

    render() {
        const { getFieldDecorator } = this.props.form;
        return (
            <div>
                <div>
                    <Button type="primary" icon="plus" size={"default"} onClick={this.openAddModal}>
                        Add User
                    </Button>
                </div>
                <div>
                    <Modal
                        title="ADD NEW USER"
                        width="40%"
                        visible={this.state.addModalVisible}
                        onOk={this.handleAddOk}
                        onCancel={this.handleAddCancel}
                        footer={[
                            <Button key="cancel" onClick={this.handleAddCancel}>
                                Cancel
                            </Button>,
                            <Button key="submit" type="primary" onClick={this.handleAddOk}>
                                Submit
                            </Button>,
                        ]}
                    >
                        <div style={{alignItems:"center"}}>
                            <p>Create new user on IoT Server.</p>
                            <Form
                                labelCol={{ span: 5 }}
                                wrapperCol={{ span: 18 }}
                            >
                                <Form.Item label="User Store Domain" style={{display:"block"}}>
                                    <Select defaultValue="PRIMARY">
                                        <Option value="PRIMARY">PRIMARY</Option>
                                    </Select>
                                </Form.Item>
                                <Form.Item label="User Name" style={{display:"block"}}>
                                    {getFieldDecorator('userName', {
                                        rules: [
                                            {
                                                required: true,
                                                message: 'This field is required. Username should be at least 3 characters long with no white spaces.',
                                            },
                                        ],
                                    })(<Input name="userName" onChange={this.onChangeInputs}/>)}
                                </Form.Item>
                                <Form.Item label="First Name" style={{display:"block"}}>
                                    {getFieldDecorator('firstName', {
                                        rules: [
                                            {
                                                required: true,
                                                message: 'This field is required',
                                            },
                                        ],
                                    })(<Input name="firstName" onChange={this.onChangeInputs}/>)}
                                </Form.Item>
                                <Form.Item label="Last Name" style={{display:"block"}}>
                                    {getFieldDecorator('lastName', {
                                        rules: [
                                            {
                                                required: true,
                                                message: 'This field is required',
                                            },
                                        ],
                                    })(<Input name="lastName" onChange={this.onChangeInputs}/>)}
                                </Form.Item>
                                <Form.Item label="Email Address" style={{display:"block"}}>
                                    {getFieldDecorator('email', {
                                        rules: [
                                            {
                                                required: true,
                                                message: 'This field is required',
                                            },
                                        ],
                                    })(<Input name="emailAddress" onChange={this.onChangeInputs}/>)}
                                </Form.Item>

                                <Form.Item label="User Roles" style={{display:"block"}}>
                                    {getFieldDecorator('userRoles', {
                                    })(<Select
                                            mode="multiple"
                                            style={{ width: '100%' }}
                                            onChange={this.onSelectInputs}
                                        >
                                        {roleSelector}
                                    </Select>)}
                                </Form.Item>
                            </Form>
                        </div>
                    </Modal>
                </div>
            </div>
        );
    }
}

export default withConfigContext(Form.create({name: 'add-user'})(AddUser))