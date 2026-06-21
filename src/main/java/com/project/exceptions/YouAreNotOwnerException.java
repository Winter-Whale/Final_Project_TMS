package com.project.exceptions;

public class YouAreNotOwnerException extends RuntimeException {
    public YouAreNotOwnerException(String message) {
        super(message);
    }
}
