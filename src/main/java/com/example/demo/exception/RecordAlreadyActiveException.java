package com.example.demo.exception;

public class RecordAlreadyActiveException extends RuntimeException {
    public RecordAlreadyActiveException(String message) {
        super(message);
    }
}
