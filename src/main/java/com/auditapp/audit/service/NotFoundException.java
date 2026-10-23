package com.auditapp.audit.service;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String type, String id) {
        super(type + " " + id + " not found");
    }
}
