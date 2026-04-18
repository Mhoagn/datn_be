package com.example.demo.exception;

public class PostIsDeletedException extends RuntimeException {
  public PostIsDeletedException(String message) {
    super(message);
  }
}
