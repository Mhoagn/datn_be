package com.example.demo.exception;

public class UserIsNotCommentAuthorException extends RuntimeException {
    public UserIsNotCommentAuthorException(String message) {
        super(message);
    }
}
