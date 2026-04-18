package com.example.demo.exception;

public class UserIsMemberException extends RuntimeException {
    public UserIsMemberException(String message) {
        super(message);
    }
}
