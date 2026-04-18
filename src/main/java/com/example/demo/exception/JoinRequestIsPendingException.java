package com.example.demo.exception;

public class JoinRequestIsPendingException extends RuntimeException {
    public JoinRequestIsPendingException(String message) {
        super(message);
    }
}
