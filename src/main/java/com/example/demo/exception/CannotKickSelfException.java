package com.example.demo.exception;

public class CannotKickSelfException extends RuntimeException {
    public CannotKickSelfException(String message) {
        super(message);
    }
}
