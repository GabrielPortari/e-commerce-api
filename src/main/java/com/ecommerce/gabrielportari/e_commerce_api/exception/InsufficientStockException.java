package com.ecommerce.gabrielportari.e_commerce_api.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String message) {
        super(message);
    }
}
