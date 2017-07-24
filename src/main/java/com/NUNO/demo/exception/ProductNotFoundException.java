package com.NUNO.demo.exception;

public class ProductNotFoundException extends ResourceNotFoundException {
    public ProductNotFoundException(String id) {
        super("Product with ID not found: " + id);
    }
}
