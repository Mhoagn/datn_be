package com.example.demo.exception;

public class CommentIsDeletedException extends RuntimeException {
    public CommentIsDeletedException(String message) {
        super(message);
    }
}
