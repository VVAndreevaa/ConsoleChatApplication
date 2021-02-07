package com.chat.exception;

// When number of arguments expected to perform a command is not provided

public class InvalidCommandParametersException extends RuntimeException {
    public InvalidCommandParametersException(String message) {
        super(message);
    }
}