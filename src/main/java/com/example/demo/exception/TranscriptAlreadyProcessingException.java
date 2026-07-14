package com.example.demo.exception;

public class TranscriptAlreadyProcessingException extends RuntimeException {
    public TranscriptAlreadyProcessingException(String message) {
        super(message);
    }
}
