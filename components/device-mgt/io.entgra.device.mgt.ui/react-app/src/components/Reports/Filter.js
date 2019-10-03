import React from "react";
import { Select } from 'antd';

class Filter extends React.Component {

    constructor(props){
        super(props);
        const { Option } = Select;
        this.state = {
            selectedItem:null
        }
    }

    selected = null;

    onChange = value => {
        this.setState({selectedItem:value},() => {
            if(this.props.dropDownName=="Device Status"){
                this.props.updateFiltersValue(this.state.selectedItem,"Device Status");
            }else{
                this.props.updateFiltersValue(this.state.selectedItem, "Device Ownership");
            }
        });
    }

    onBlur = () => {
    }

    onFocus = () => {
    }

    onSearch = (val) => {
    }

    render(){
        let item = this.props.dropDownItems.map((data) => 
            <Select.Option 
                value={data} 
                key={data}>
                {data}
            </Select.Option>);
        return(
            <Select
                showSearch
                style={{ width: 200 }}
                placeholder={this.props.dropDownName}
                optionFilterProp="children"
                onChange={this.onChange}
                onFocus={this.onFocus}
                onBlur={this.onBlur}
                onSearch={this.onSearch}
                filterOption={(input, option) =>
                option.props.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
                }
            >
                {item}
            </Select>
        )
    }
}

export default Filter;