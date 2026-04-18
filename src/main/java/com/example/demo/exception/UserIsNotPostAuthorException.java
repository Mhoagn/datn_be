package com.example.demo.exception;

public class UserIsNotPostAuthorException extends RuntimeException {
    public UserIsNotPostAuthorException(String message) {
        super(message);
    }
}
