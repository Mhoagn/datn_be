package com.example.demo.exception;

public class UserIsNotHostMeetingException extends RuntimeException {
    public UserIsNotHostMeetingException(String message) {
        super(message);
    }
}
