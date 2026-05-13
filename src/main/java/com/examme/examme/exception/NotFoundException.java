package com.examme.examme.exception;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message);
    }
}