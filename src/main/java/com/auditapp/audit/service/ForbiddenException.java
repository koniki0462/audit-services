package com.auditapp.audit.service;

/** The caller is signed in correctly but may not do this. Maps to 403. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
