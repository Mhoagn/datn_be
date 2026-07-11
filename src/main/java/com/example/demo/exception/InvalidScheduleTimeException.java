package com.example.demo.exception;

public class InvalidScheduleTimeException extends RuntimeException {
    public InvalidScheduleTimeException(String message) {
        super(message);
    }
}
