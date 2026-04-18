package com.example.demo.exception;

public class UserNotSendMessageException extends RuntimeException {
    public UserNotSendMessageException(String message) {
        super(message);
    }
}
