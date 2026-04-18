package com.example.demo.exception;

public class UserCannotStopRecordException extends RuntimeException {
    public UserCannotStopRecordException(String message) {
        super(message);
    }
}
