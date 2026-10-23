package com.auditapp.audit.service;

/** The request clashes with existing data, for example an email that is already registered. Maps to 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
