package com.example.demo.exception;

public class UserAlreadyInMeetingException extends RuntimeException {
    public UserAlreadyInMeetingException(String message) {
        super(message);
    }
}
