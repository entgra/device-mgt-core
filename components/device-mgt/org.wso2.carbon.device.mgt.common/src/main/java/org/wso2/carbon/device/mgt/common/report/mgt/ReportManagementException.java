package org.wso2.carbon.device.mgt.common.report.mgt;

public class ReportManagementException extends Exception {
    private String errorMessage;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ReportManagementException(String msg, Exception nestedEx) {
        super(msg, nestedEx);
        setErrorMessage(msg);
    }

    public ReportManagementException(String message, Throwable cause) {
        super(message, cause);
        setErrorMessage(message);
    }

    public ReportManagementException(String msg) {
        super(msg);
        setErrorMessage(msg);
    }

    public ReportManagementException() {
        super();
    }

    public ReportManagementException(Throwable cause) {
        super(cause);
    }
}
