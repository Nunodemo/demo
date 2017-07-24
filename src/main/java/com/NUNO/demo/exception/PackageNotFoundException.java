package com.NUNO.demo.exception;

public class PackageNotFoundException extends ResourceNotFoundException {
    public PackageNotFoundException(String id) {
        super("Package with ID not found: " + id);
    }
}
