package io.entgra.device.mgt.core.dynamic.task.mgt.common.exception.api;

public class ForbiddenException extends Exception {
    public ForbiddenException(String msg) {
        super(msg);
    }

    public ForbiddenException(String msg, Throwable t) {
        super(msg, t);
    }
}
