package com.example.demo.exception;

public class MeetingNotStartedException extends RuntimeException {
    public MeetingNotStartedException(String message) {
        super(message);
    }
}
