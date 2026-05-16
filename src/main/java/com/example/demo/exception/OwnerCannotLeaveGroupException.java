package com.example.demo.exception;

public class OwnerCannotLeaveGroupException extends RuntimeException {
    public OwnerCannotLeaveGroupException(String message) {
        super(message);
    }
}
