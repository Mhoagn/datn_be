package com.example.demo.exception;

public class MeetingRecordNotFoundException extends RuntimeException {
    public MeetingRecordNotFoundException(String message) {
        super(message);
    }
}
