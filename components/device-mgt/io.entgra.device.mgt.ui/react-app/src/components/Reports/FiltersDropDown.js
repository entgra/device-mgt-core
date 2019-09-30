import React from "react";
import {
    Menu,
    Dropdown,
    Icon,
    Button
} from "antd";

class FiltersDropDown extends React.Component {

    constructor(props){
        super(props);
    }

    state = {
        startValue: null,
        endValue: null,
        endOpen: false,
        visible: false,
        itemValue:''
    };

    selectedItem = null;

    handleMenuClick = e => {
        this.setState({ visible: false });
        this.selectedItem = e.item.props.children;
        if(this.props.dropDownName=="Device Status"){
            this.props.updateFiltersValue(e.item.props.children,"Device Status");
        }else{
                this.props.updateFiltersValue(e.item.props.children);
        }
    //    console.log(this.selectedItem);
    };

    handleVisibleChange = flag => {
        this.setState({ visible: flag });
    };

    render() {
        const { SubMenu } = Menu;


        let item = this.props.dropDownItems.map((data) => <Menu.Item key={data.id}>{data.item}</Menu.Item>);
        const menu = (
            <Menu onClick={this.handleMenuClick}>
                {item}
            </Menu>
        );

        return(
            <Dropdown
                overlay={menu}
                trigger={['click']}
                onVisibleChange={this.handleVisibleChange}
                visible={this.state.visible}>
                <Button>
                    {this.props.dropDownName} {this.selectedItem}<Icon type="down" />
                </Button>
            </Dropdown>
        );
    }

}

export default FiltersDropDown;