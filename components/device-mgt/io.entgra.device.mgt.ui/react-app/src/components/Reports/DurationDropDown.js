import React from "react";
import {
    DatePicker,
    Menu,
    Dropdown,
    Icon,
    Button
} from "antd";

class DurationDropDown extends React.Component {

    constructor(props){
        super(props);
    }

    state = {
        startValue: null,
        endValue: null,
        endOpen: false,
        visible: false,
    };

    //Date Picker
    selectedFromDate = "";
    selectedToDate = "";
    disabledStartDate = startValue => {
        const {endValue} = this.state;
        if (!startValue || !endValue) {
            return false;
        }
        return startValue.valueOf() > endValue.valueOf();
    };

    disabledEndDate = endValue => {
        const {startValue} = this.state;
        if (!endValue || !startValue) {
            return false;
        }
        return endValue.valueOf() <= startValue.valueOf();
    };

    onChange = (field, value) => {
        this.setState({
            [field]: value,
        });
    };

    onStartChange = value => {
        this.onChange('startValue', value);
       // console.log("Start date changed to : " + value);
        var date = new Date(value);
        this.selectedFromDate = date.toDateString();
        this.setState({ visible: true });
    };

    onEndChange = value => {
        this.onChange('endValue', value);
        //console.log("End date changed to : " + value);
        var date = new Date(value);
        this.selectedToDate = date.toDateString();
        this.setState({ visible: false });
    };

    handleStartOpenChange = open => {
        if (!open) {
            this.setState({endOpen: true});
        }
    };

    handleEndOpenChange = open => {
        this.setState({endOpen: open});
    };

    //DropDown
    selectedItem = null;
    handleMenuClick = e => {
        if (e.key === '7') {
            //this.selectedItem = "Select duration";
            this.setState({ visible: true });
            this.selectedItem = "from " + this.selectedFromDate + " to " + this.selectedToDate;
            if(this.selectedFromDate != "" && this.selectedToDate != ""){
                var formattedFromDate = this.formatDate(new Date(this.selectedFromDate));
                var formattedToDate = this.formatDate(new Date(this.selectedToDate))
                this.props.updateDurationValue(formattedFromDate,formattedToDate);
            }
        }else{
            this.setState({ visible: false });
            this.selectedItem = "for " + e.item.props.children;
            this.props.updateDurationValue(e.item.props.children);
        }
        //console.log(this.selectedItem);
    };

    handleVisibleChange = flag => {
        this.setState({ visible: flag });

    };

    formatDate = (date) => {
        var dateString = date.getFullYear() + "-" + ( date.getMonth() + 1 )+ "-" + date.getDate();
        return dateString;
    }

    render() {
        const {startValue, endValue, endOpen} = this.state;
        const menu = (
            <Menu onClick={this.handleMenuClick}>
                <Menu.Item key="1">Today</Menu.Item>
                <Menu.Item key="2">Yesterday</Menu.Item>
                <Menu.Item key="3">Last 7 Days</Menu.Item>
                <Menu.Item key="4">Last 30 Days</Menu.Item>
                <Menu.Item key="5">This Month</Menu.Item>
                <Menu.Item key="6">Last Month</Menu.Item>
                <Menu.Item key="7">
                    <div>
                        <DatePicker
                            disabledDate={this.disabledStartDate}
                            format="YYYY-MM-DD"
                            value={startValue}
                            placeholder="Start"
                            onChange={this.onStartChange}
                            onOpenChange={this.handleStartOpenChange}
                        />
                        <DatePicker
                            disabledDate={this.disabledEndDate}
                            format="YYYY-MM-DD"
                            value={endValue}
                            placeholder="End"
                            onChange={this.onEndChange}
                            open={endOpen}
                            onOpenChange={this.handleEndOpenChange}
                        />
                    </div>
                </Menu.Item>
            </Menu>
        );

        return(
            <Dropdown
                overlay={menu}
                trigger={['click']}
                onVisibleChange={this.handleVisibleChange}
                visible={this.state.visible}>
                <Button>
                    Generate Report {this.selectedItem}<Icon type="down" />
                </Button>
            </Dropdown>
        );
    }

}

export default DurationDropDown;