package com.chat.exception;

public class InvalidCommandParametersException extends RuntimeException {
    public InvalidCommandParametersException(String message) {
        super(message);
    }
}
