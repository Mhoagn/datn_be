package com.example.demo.exception;

public class NotificationIsNotOfUser extends RuntimeException {
    public NotificationIsNotOfUser(String message) {
        super(message);
    }
}
