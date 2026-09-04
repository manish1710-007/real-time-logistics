package com.logistics.shared.exception;


public class NotFoundException extends BusinessException {
    public NotFoundException(String resource, String id) {
        super("NOT_FOUND", resource + " not found with id: " + id);
    }
}