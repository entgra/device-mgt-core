import React from "react";
import {Button, Select, Icon} from "antd";

class BulkActionBar extends React.Component {

    constructor(props){
        super(props);
        this.state = {
            hideDeleteButton:true,
            deleteRequest:false,
            selected:"Actions"
        }
    }

    onChange = value => {
        this.setState({hideDeleteButton:false});
        this.props.enableDelete();
    }

    //This method is used to trigger delete request on selected devices
    deleteDevice = () => {
        this.props.deleteDevice();
    }

    //This method is used to cancel deletion
    cancelDelete = () => {
        this.setState({hideDeleteButton:true})
        this.props.disableDelete();
        console.log("Disabled");
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if(prevProps.selectedRows !== this.props.selectedRows){
            console.log(this.props.selectedRows);
            if(this.props.selectedRows.length>1){
                console.log("Multiple");
            }else if(this.props.selectedRows.length==1){
                console.log("Single");
            }else{
                console.log("Empty");
            }
        }
    }

    render() {
        return(
                <div style={{paddingBottom:'5px'}}>

                                <Button type="normal" icon="delete" size={'default'} />

                                <Button type="primary" icon="delete"
                                        onClick={this.deleteDevice}
                                        style={{display:!this.state.hideDeleteButton ? "inline" : "none"}}>
                                    Delete Selected Devices
                                </Button>.

                                <Button type="danger"
                                        onClick={this.cancelDelete}
                                        style={{display:!this.state.hideDeleteButton ? "inline" : "none"}}>
                                    Cancel
                                </Button>

                </div>
        )
    }
}

export default BulkActionBar;