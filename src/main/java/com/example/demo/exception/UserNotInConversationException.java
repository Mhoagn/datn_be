package com.example.demo.exception;

public class UserNotInConversationException extends RuntimeException {
    public UserNotInConversationException(String message) {
        super(message);
    }
}
