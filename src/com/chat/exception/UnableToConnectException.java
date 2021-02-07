package com.chat.exception;

public class UnableToConnectException extends RuntimeException {
    private static final String CONNECTION_FAILURE_ERROR_MSG = "Connecting to the server failed: ";

    public UnableToConnectException(String cause) {
        super(CONNECTION_FAILURE_ERROR_MSG + cause);
    }
}