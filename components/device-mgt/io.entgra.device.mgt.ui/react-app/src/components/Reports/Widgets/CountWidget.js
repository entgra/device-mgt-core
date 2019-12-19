import React from "react";

import {Card} from "antd";

class CountWidget extends React.Component {

    constructor(props) {
        super(props);
        this.routes = props.routes;
    }

    render() {
        let count = this.props.allCount;
        const countObj = [{entity:"All",count:count},{entity:"Enrolled",count:80},{entity:"Unenrolled",count:20}];

        let card = countObj.map((data) =>
            <Card
                bordered={true}
                hoverable={true}
                key={data.entity}
                style={{borderRadius: 5, marginBottom: 5, width:"100%"}}>

                <h3>{data.entity} Devices: {data.count}</h3>

            </Card>
        )

        return(
            <div>
                {card}
            </div>
        )
    }
}

export default CountWidget;