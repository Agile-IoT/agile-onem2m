package com.srcsolution.things.onem2m_client;

public class ResourceException extends RuntimeException {

    private Integer statusCode;

    public ResourceException(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public ResourceException(Throwable cause, Integer statusCode) {
        super(cause);
        this.statusCode = statusCode;
    }

    public ResourceException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ResourceException(String message, Throwable cause, Integer statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public ResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Integer statusCode) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.statusCode = statusCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
