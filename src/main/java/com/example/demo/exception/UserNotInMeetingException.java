package com.example.demo.exception;

public class UserNotInMeetingException extends RuntimeException {
    public UserNotInMeetingException(String message) {
        super(message);
    }
}
