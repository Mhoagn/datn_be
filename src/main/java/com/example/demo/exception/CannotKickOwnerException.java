package com.example.demo.exception;

public class CannotKickOwnerException extends RuntimeException {
    public CannotKickOwnerException(String message) {
        super(message);
    }
}
