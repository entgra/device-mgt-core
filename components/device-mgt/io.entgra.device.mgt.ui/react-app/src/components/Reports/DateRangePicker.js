import React from "react";
import { DatePicker } from 'antd';
import moment from 'moment';

class DateRangePicker extends React.Component {

    constructor(props){
        super(props);
    }

    onChange = (dates, dateStrings) => {
        this.props.updateDurationValue(dateStrings[0],dateStrings[1]);
    }

    render(){
        const { RangePicker } = DatePicker;
        return(
            <RangePicker
                ranges={{
                    'Today': [moment(), moment()],
                    'Yesterday': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                    'Last 7 Days': [moment().subtract(6, 'days'), moment()],
                    'Last 30 Days': [moment().subtract(29, 'days'), moment()],
                    'This Month': [moment().startOf('month'), moment().endOf('month')],
                    'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
                }}
                format="YYYY-MM-DD"
                onChange={this.onChange}
            />  
        )
    }
}

export default DateRangePicker;