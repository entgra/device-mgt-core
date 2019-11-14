import React from 'react';
import {Button, Form, Input, message, Modal, notification, Select, Tree} from "antd";
import {withConfigContext} from "../../context/ConfigContext";
import axios from "axios";

const {Option} = Select;
const {TreeNode} = Tree;

class AddRole extends React.Component {

    constructor(props) {
        super(props);
        this.config = this.props.context;
        this.state = {
            isAddRoleModalVisible: false,
            isAddPermissionModalVisible: false,
            roleName: '',
            users: [],
            nodeList: [],
            expandedKeys: [],
            autoExpandParent: true,
            checkedKeys: [],
            selectedKeys: [],
            isNodeList: false,
        }
    }

    openAddModal = () => {
        this.setState({
            isAddRoleModalVisible: true
        });
    };

    onCancelHandler = e => {
        this.setState({
            isAddRoleModalVisible: false,
            isAddPermissionModalVisible: false,
        });
    };

    onAddRole = e => {
        this.props.form.validateFields((err, values) => {
            if (!err) {
                this.onConfirmAddRole(values);
            }
            console.log(values);
        });
    };

    onExpand = expandedKeys => {
        console.log('onExpand', expandedKeys);
        // if not set autoExpandParent to false, if children expanded, parent can not collapse.
        // or, you can remove all expanded children keys.
        this.setState({
            expandedKeys,
            autoExpandParent: false,
        });
    };

    onCheck = checkedKeys => {
        console.log('onCheck', checkedKeys);
        this.setState({checkedKeys});
    };

    onSelect = (selectedKeys, info) => {
        console.log('onSelect', info);
        this.setState({selectedKeys});
    };

    onConfirmAddRole = (value) => {
        const roleData = {
            roleName: value.roleName,
            users: value.users,
        };
        this.setState({
            roleName: value.roleName,
        });
        axios.post(
            window.location.origin + this.config.serverConfig.invoker.uri +
            this.config.serverConfig.invoker.deviceMgt +
            "/roles",
            roleData,
            {headers: {'Content-Type': 'application-json'}}
        ).then(res => {
            if (res.status === 201) {
                this.props.fetchUsers();
                this.setState({
                    isAddRoleModalVisible: false,
                    isAddPermissionModalVisible: true,

                });
                notification["success"]({
                    message: "Done",
                    duration: 4,
                    description:
                        "Successfully added the role.",
                });
                this.loadPermissionList();
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
                        "Error occurred while trying to add role.",
                });
            }
        });
    };

    renderTreeNodes = (data) => {
        console.log("rendering")
        data.map(item => {
            if (item !== null) {
                if (item.hasOwnProperty("nodeList")) {
                    // console.log(item.resourcePath);
                    return (
                        <TreeNode title={item.displayName} key={item.resourcePath} dataRef={item}>
                            {this.renderTreeNodes(item.nodeList)}
                        </TreeNode>
                    );
                }
                return <TreeNode key={item.resourcePath} {...item}/>;
            }
        });
    };

    loadPermissionList = () => {
        let apiURL = window.location.origin + this.config.serverConfig.invoker.uri +
            this.config.serverConfig.invoker.deviceMgt + "/roles/" + this.state.roleName + "/permissions";

        axios.get(apiURL).then(res => {
            if (res.status === 200) {
                this.setState({
                    nodeList: res.data.data.nodeList,
                    isNodeList: true,
                });
                // this.state.nodeList.push(res.data.data.nodeList);
                // console.log(this.state.nodeList);

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
                    description: "Error occurred while trying to load permission.",
                });
            }
        })
    };

    loadUsersList = (value) => {
        let apiURL = window.location.origin + this.config.serverConfig.invoker.uri +
            this.config.serverConfig.invoker.deviceMgt + "/users/search/usernames?filter=" + value + "&domain=Primary";
        axios.get(apiURL).then(res => {
            if (res.status === 200) {
                let user = JSON.parse(res.data.data);
                let users = [];
                for (let i = 0; i < user.length; i++) {
                    users.push(<Option key={user[i].username}>{user[i].username}</Option>);
                }
                this.setState({
                    users: users
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
                    description: "Error occurred while trying to load users.",
                });
            }
        })
    };

    render() {
        const {getFieldDecorator} = this.props.form;
        return (
            <div>
                <div>
                    <Button type="primary" icon="plus" size={"default"} onClick={this.openAddModal}
                            style={{marginBottom: '10px'}}>
                        Add Role
                    </Button>
                </div>
                <div>
                    <Modal
                        title="ADD NEW ROLE"
                        width="40%"
                        visible={this.state.isAddRoleModalVisible}
                        onOk={this.onAddRole}
                        onCancel={this.onCancelHandler}
                        footer={[
                            <Button key="cancel" onClick={this.onCancelHandler}>
                                Cancel
                            </Button>,
                            <Button key="submit" type="primary" onClick={this.onAddRole}>
                                Add Role
                            </Button>,
                        ]}>
                        <div style={{alignItems: "center"}}>
                            <p>Create new user on IoT Server.</p>
                            <Form
                                labelCol={{span: 5}}
                                wrapperCol={{span: 18}}>
                                <Form.Item label="User Store Domain" style={{display: "block"}}>
                                    {getFieldDecorator('userStoreDomain', {
                                        initialValue: 'PRIMARY'
                                    })(
                                        <Select>
                                            <Option key="PRIMARY">PRIMARY</Option>
                                        </Select>
                                    )}
                                </Form.Item>
                                <Form.Item label="Role Name" style={{display: "block"}}>
                                    {getFieldDecorator('roleName', {
                                        rules: [
                                            {
                                                pattern: new RegExp("^(((?!(\\@|\\/|\\s)).){3,})*$"),
                                                message: 'Role name should be in minimum 3 characters long and not ' +
                                                    'include any whitespaces or @ or /',
                                            },
                                            {
                                                required: true,
                                                message: 'This field is required.',
                                            },

                                        ],
                                    })(<Input/>)}
                                </Form.Item>
                                <Form.Item label="User List" style={{display: "block"}}>
                                    {getFieldDecorator('users', {})(<Select
                                        mode="multiple"
                                        style={{width: '100%'}}
                                        onSearch={this.loadUsersList}>
                                        {this.state.users}
                                    </Select>)}
                                </Form.Item>
                            </Form>
                        </div>
                    </Modal>
                </div>
                <div>
                    <Modal
                        title="CHANGE ROLE PERMISSION"
                        width="40%"
                        visible={this.state.isAddPermissionModalVisible}
                        onOk={this.onSubmitHandler}
                        onCancel={this.onCancelHandler}
                        footer={[
                            <Button key="cancel" onClick={this.onCancelHandler}>
                                Cancel
                            </Button>,
                            <Button key="submit" type="primary" onClick={this.onSubmitHandler}>
                                Assign
                            </Button>,
                        ]}>
                        <div style={{alignItems: "center"}}>
                            <div>
                                {(this.state.isNodeList) && (
                                    <Tree
                                        checkable
                                        onExpand={this.onExpand}
                                        expandedKeys={this.state.expandedKeys}
                                        autoExpandParent={this.state.autoExpandParent}
                                        onCheck={this.onCheck}
                                        checkedKeys={this.state.checkedKeys}
                                        onSelect={this.onSelect}
                                        selectedKeys={this.state.selectedKeys}>
                                        {this.renderTreeNodes(this.state.nodeList)}
                                    </Tree>
                                )}
                            </div>
                        </div>
                    </Modal>
                </div>
            </div>
        );
    }
}

export default withConfigContext(Form.create({name: 'add-role'})(AddRole))